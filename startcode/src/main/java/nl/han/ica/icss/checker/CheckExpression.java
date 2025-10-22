package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.Literal;
import nl.han.ica.icss.ast.Operation;
import nl.han.ica.icss.ast.VariableReference;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;

public class CheckExpression {
    HashMap<Class<? extends Literal>, ExpressionType> mappedExpressionTypes;

    public CheckExpression() {
        this.mappedExpressionTypes = new HashMap<>();

        mappedExpressionTypes.put(BoolLiteral.class, ExpressionType.BOOL);
        mappedExpressionTypes.put(ColorLiteral.class, ExpressionType.COLOR);
        mappedExpressionTypes.put(PercentageLiteral.class, ExpressionType.PERCENTAGE);
        mappedExpressionTypes.put(PixelLiteral.class,  ExpressionType.PIXEL);
        mappedExpressionTypes.put(ScalarLiteral.class, ExpressionType.SCALAR);
    }

    public ExpressionType getExpressionType(Expression expression, IHANLinkedList<HashMap<String, ExpressionType>> scopeMap) {
        if (expression instanceof VariableReference) {
            return getExpressionTypeFromVariableReference((VariableReference) expression, scopeMap);
        }

        if (expression instanceof Operation) {
            return getExpressionTypeFromOperation((Operation) expression, scopeMap);
        }
        return mappedExpressionTypes.getOrDefault(expression.getClass(), ExpressionType.UNDEFINED);
    }

    private ExpressionType getExpressionTypeFromVariableReference(VariableReference variableReference, IHANLinkedList<HashMap<String, ExpressionType>> scopeMap) {
        for (int i = 0; i < scopeMap.getSize(); i++) {
            if (scopeMap.get(i).containsKey(variableReference.name)) {
                return scopeMap.get(i).get(variableReference.name);
            }
        }
        return ExpressionType.UNDEFINED;
    }

    private ExpressionType getExpressionTypeFromOperation(Operation operation, IHANLinkedList<HashMap<String, ExpressionType>> scopeMap) {
        ExpressionType lhsType = getExpressionType(operation.lhs, scopeMap);
        ExpressionType rhsType = getExpressionType(operation.rhs, scopeMap);

        if (operation instanceof MultiplyOperation) {
            if (lhsType == ExpressionType.SCALAR && rhsType == ExpressionType.SCALAR) {
                return ExpressionType.SCALAR;
            }

            if (lhsType == ExpressionType.SCALAR) return rhsType;
            if (rhsType == ExpressionType.SCALAR) return lhsType;

        } else if (operation instanceof AddOperation || operation instanceof SubtractOperation) {
            if (lhsType == rhsType) return lhsType;
        }

        return ExpressionType.UNDEFINED;
    }
}
