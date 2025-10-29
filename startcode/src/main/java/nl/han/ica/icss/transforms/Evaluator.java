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

    private final IHANLinkedList<HashMap<String, Literal>> variableValues;

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

    private void evaluateNodesWithParent(ArrayList<ASTNode> nodes) {
        ArrayList<Integer> indicesToRemove = new ArrayList<>();
        ArrayList<ArrayList<ASTNode>> nodesToInsertAt = new ArrayList<>();

        for (int i = 0; i < nodes.size(); i++) {
            ASTNode node = nodes.get(i);

            if (node instanceof IfClause) {
                IfClause ifClause = (IfClause) node;
                Literal condition = evaluateExpression(ifClause.conditionalExpression);
                ArrayList<ASTNode> replacement = getAstNodes(condition, ifClause);

                indicesToRemove.add(i);
                nodesToInsertAt.add(replacement);

                evaluateNodesWithParent(replacement);

            } else if (node instanceof VariableAssignment) {
                evaluateVariableAssignment((VariableAssignment) node);
            } else if (node instanceof Stylerule) {
                evaluateStylerule((Stylerule) node);
            } else if (node instanceof Declaration) {
                evaluateDeclaration((Declaration) node);
            }
        }

        for (int idx = indicesToRemove.size() - 1; idx >= 0; idx--) {
            int removeIndex = indicesToRemove.get(idx);
            nodes.remove(removeIndex);
            nodes.addAll(removeIndex, nodesToInsertAt.get(idx));
        }
    }


    private static ArrayList<ASTNode> getAstNodes(Literal condition, IfClause ifClause) {
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

        return new ArrayList<>(chosenBody);
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
            return resolveVariable(((VariableReference) expr).name);
        }
        Literal left = evaluateExpression(((Operation) expr).lhs);
        Literal right = evaluateExpression(((Operation) expr).rhs);

        if (expr instanceof AddOperation || expr instanceof SubtractOperation) {
            double sign = (expr instanceof SubtractOperation) ? -1 : 1;

            if (left instanceof ScalarLiteral && right instanceof ScalarLiteral) {
                return new ScalarLiteral((int)(((ScalarLiteral) left).value + sign * ((ScalarLiteral) right).value));
            } else if (left instanceof PixelLiteral && right instanceof PixelLiteral) {
                return new PixelLiteral((int)(((PixelLiteral) left).value + sign * ((PixelLiteral) right).value));
            } else if (left instanceof PercentageLiteral && right instanceof PercentageLiteral) {
                return new PercentageLiteral((int)(((PercentageLiteral) left).value + sign * ((PercentageLiteral) right).value));
            }
        }
        if (expr instanceof MultiplyOperation) {
            ScalarLiteral scalar = left instanceof ScalarLiteral ? (ScalarLiteral) left : (ScalarLiteral) right;
            Literal multiplier = scalar == left ? right : left;

            assert scalar != null;

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

    private Literal resolveVariable(String name) {
        for (int i = 0; i < variableValues.getSize(); i++) {
            HashMap<String, Literal> scope = variableValues.get(i);
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        return null;
    }

}
