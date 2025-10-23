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
import java.util.LinkedList;

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
        for (ASTNode node : stylesheet.body) {
            evaluateNode(node);
        }
    }

    private void evaluateNode(ASTNode node) {
        if (node instanceof VariableAssignment) {
            evaluateVariableAssignment((VariableAssignment) node);
        } else if (node instanceof Stylerule) {
            evaluateStylerule((Stylerule) node);
        } else if (node instanceof Declaration) {
            evaluateDeclaration((Declaration) node);
//        } else if (node instanceof IfClause) {
//            evaluateIfClause((IfClause) node);
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

            // altijd scalarliteral returnen aangezien 1 van de twee scalair is.. eerst kijken welke en die dan vermenigvuldigen met de andere
        }
        return null;
    }


    private void evaluateStylerule(Stylerule rule) {
        variableValues.addFirst(new HashMap<>(variableValues.getFirst())); // nieuwe scope met kopie
        for (ASTNode node : rule.body) {
            evaluateNode(node);
        }
        variableValues.removeFirst();
    }

}
