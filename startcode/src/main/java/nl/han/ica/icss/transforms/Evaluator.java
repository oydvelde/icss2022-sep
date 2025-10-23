package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.ArrayList;
import java.util.HashMap;

public class Evaluator implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new HANLinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        variableValues.addFirst(new HashMap<>());
        evaluateStylesheet(ast.root);
    }

    private void evaluateStylesheet(Stylesheet stylesheet) {
        ArrayList<ASTNode> nodes = stylesheet.body;

        evaluateNodesWithParent(nodes);

        removeVariableAssignments(nodes);
    }

    private  void evaluateNodesWithParent(ArrayList<ASTNode> nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            ASTNode node = nodes.get(i);

            if (node instanceof IfClause) {
                IfClause ifClause = (IfClause) node;
                Literal condition = evaluateExpression(ifClause.conditionalExpression);

                boolean cond = false;
                if (condition instanceof BoolLiteral) {
                    cond = ((BoolLiteral) condition).value;
                }

                ArrayList<ASTNode> chosenBody = new ArrayList<>();
                if (cond) {
                    chosenBody = ifClause.body;
                } else if (ifClause.elseClause != null) {
                    chosenBody = ifClause.elseClause.body;
                }

                ArrayList<ASTNode> copyOfChosen = new ArrayList<>(chosenBody);

                nodes.remove(i);
                nodes.addAll(i, copyOfChosen);

                evaluateNodesWithParent(copyOfChosen);
            } else if (node instanceof VariableAssignment) {
                evaluateVariableAssignment((VariableAssignment) node);
            } else if (node instanceof Stylerule) {
                evaluateStylerule((Stylerule) node);
            } else if (node instanceof Declaration) {
                evaluateDeclaration((Declaration) node);
            }
        }
    }

    private void evaluateDeclaration(Declaration node) {
        node.expression = evaluateExpression(node.expression);
    }

    private void evaluateVariableAssignment(VariableAssignment assignment) {
        Literal value = evaluateExpression(assignment.expression);
        variableValues.getFirst().put(assignment.name.name, value);
    }

    private Literal evaluateExpression(Expression expr) {
        if (expr instanceof Literal) {
            return (Literal) expr;
        }
        if (expr instanceof VariableReference) {
            return variableValues.getFirst().get(((VariableReference) expr).name);
        }
        if (expr instanceof AddOperation) {
            Literal left = evaluateExpression(((AddOperation) expr).lhs);
            Literal right = evaluateExpression(((AddOperation) expr).rhs);
            if (left instanceof ScalarLiteral && right instanceof ScalarLiteral) {
                return new ScalarLiteral(((ScalarLiteral) left).value + ((ScalarLiteral) right).value);
            } else if (left instanceof PixelLiteral && right instanceof PixelLiteral) {
                return new PixelLiteral(((PixelLiteral) left).value + ((PixelLiteral) right).value);
            } else if (left instanceof PercentageLiteral && right instanceof PercentageLiteral) {
                return new PercentageLiteral(((PercentageLiteral) left).value + ((PercentageLiteral) right).value);
            }
        }
        if (expr instanceof SubtractOperation) {
            Literal left = evaluateExpression(((SubtractOperation) expr).lhs);
            Literal right = evaluateExpression(((SubtractOperation) expr).rhs);
            if (left instanceof ScalarLiteral && right instanceof ScalarLiteral) {
                return new ScalarLiteral(((ScalarLiteral) left).value - ((ScalarLiteral) right).value);
            } else if (left instanceof PixelLiteral && right instanceof PixelLiteral) {
                return new PixelLiteral(((PixelLiteral) left).value - ((PixelLiteral) right).value);
            } else if (left instanceof PercentageLiteral && right instanceof PercentageLiteral) {
                return new PercentageLiteral(((PercentageLiteral) left).value - ((PercentageLiteral) right).value);
            }
        }
        if (expr instanceof MultiplyOperation) {
            Literal left = evaluateExpression(((MultiplyOperation) expr).lhs);
            Literal right = evaluateExpression(((MultiplyOperation) expr).rhs);

            ScalarLiteral scalar = left instanceof ScalarLiteral ? (ScalarLiteral) left : (ScalarLiteral) right;
            Literal multiplier = scalar == left ? right : left;

            if (multiplier instanceof PixelLiteral) {
                return new PixelLiteral(((PixelLiteral) multiplier).value * scalar.value);
            } else if (multiplier instanceof PercentageLiteral) {
                return new PercentageLiteral(((PercentageLiteral) multiplier).value * scalar.value);
            } else if (multiplier instanceof ScalarLiteral) {
                return new ScalarLiteral(((ScalarLiteral) multiplier).value * scalar.value);
            }
        }

        return null;
    }

    private void removeVariableAssignments(ArrayList<ASTNode> nodes) {
        nodes.removeIf(node -> node instanceof VariableAssignment);

        for (ASTNode node : nodes) {
            if (node instanceof Stylerule) {
                removeVariableAssignments(((Stylerule) node).body);
            }
        }
    }

    private void evaluateStylerule(Stylerule rule) {
        variableValues.addFirst(new HashMap<>(variableValues.getFirst()));
        evaluateNodesWithParent(rule.body);
        variableValues.removeFirst();
    }

}
