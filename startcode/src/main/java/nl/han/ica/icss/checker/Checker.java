package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;

public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> scopeMap;
    private CheckExpression checkExpression;

    public void check(AST ast) {
        scopeMap = new HANLinkedList<>();
        scopeMap.addFirst(new HashMap<>());

        checkExpression = new CheckExpression();

        walkThroughAst(ast.root);
    }

    private void walkThroughAst(ASTNode astNode) {
        if (isNewScopeNode(astNode)) enterScope();

        if (astNode instanceof VariableAssignment) pushAssignment((VariableAssignment) astNode);

        if (astNode instanceof VariableReference) checkVariableReference((VariableReference) astNode);
        if (astNode instanceof IfClause) checkIfClause((IfClause) astNode);

        if (astNode instanceof Operation) {
            Operation operation = (Operation) astNode;
            checkOperationColours(operation);
            checkOperationTypeAndScalar(operation);
        }

        if (astNode instanceof Declaration) checkDeclaration((Declaration)  astNode);

        for (ASTNode child : astNode.getChildren()) walkThroughAst(child);

        if (isNewScopeNode(astNode)) exitScope();
    }

    // CH04 - Controleer of bij declaraties het type van de value klopt met de property. Declaraties zoals width: #ff0000 of color: 12px zijn natuurlijk onzin.
    private void checkDeclaration(Declaration astNode) {
        String propertyName = astNode.property.name;
        ExpressionType expressionType = checkExpression.getExpressionType(astNode.expression, scopeMap);

        switch (propertyName.toLowerCase()) {
            case "color":
            case "background-color":
                if (expressionType != ExpressionType.COLOR) {
                    astNode.setError("Declaration should be of type color for property " + propertyName);
                }
                break;

            case "width":
            case "height":
                if (!expressionType.equals(ExpressionType.PIXEL) && !expressionType.equals(ExpressionType.PERCENTAGE)) {
                    astNode.setError("Declaration should be of type pixel or percentage for property " + propertyName);
                }
                break;
            default:
                break;
        }
    }

    private void pushAssignment(VariableAssignment astnode) {
        ExpressionType expressionTypeVariable = checkExpression.getExpressionType(astnode.expression, scopeMap);
        scopeMap.getFirst().put(astnode.name.name, expressionTypeVariable);
    }

    private void checkVariableReference(VariableReference node) {
        String variableName = node.name;

        // CH01: Controleer of er geen variabelen worden gebruikt die niet gedefinieerd zijn.
        // CH06: Controleer of variabelen enkel binnen hun scope gebruikt worden
        if (!scopeContainsVariable(variableName)) {
            node.setError("Variable '" + variableName + "' is not defined in the current scope");
        }
    }

    private boolean scopeContainsVariable(String variableName) {
        for (int i = 0; i < scopeMap.getSize(); i++) {
            HashMap<String, ExpressionType> scope = scopeMap.get(i);
            if (scope.containsKey(variableName)) return true;
        }
        return false;
    }

    // CH02 - Controleer of de operanden van de operaties plus en min van gelijk type zijn. & Een operand scalair bij vermenigvuldigen
    private void checkOperationTypeAndScalar(Operation operation) {
        ExpressionType expressionTypeLhs = checkExpression.getExpressionType(operation.lhs, scopeMap);
        ExpressionType expressionTypeRhs = checkExpression.getExpressionType(operation.rhs, scopeMap);

        if (operation instanceof AddOperation ||  operation instanceof SubtractOperation) {
            if (!expressionTypeLhs.equals(expressionTypeRhs)) {
                operation.setError("Type mismatch in add or subtract operation");
            }
        }

        if (operation instanceof MultiplyOperation) {
            if (!expressionTypeLhs.equals(ExpressionType.SCALAR) && !expressionTypeRhs.equals(ExpressionType.SCALAR)) {
                operation.setError("Multiply operation without at least one scalar operand");
            }
        }
    }

    // CH03 - Controleer of er geen kleuren worden gebruikt in operaties (plus, min en keer).
    private void checkOperationColours(Operation operation) {
        ExpressionType expressionTypeLhs = checkExpression.getExpressionType(operation.lhs, scopeMap);
        ExpressionType expressionTypeRhs = checkExpression.getExpressionType(operation.rhs, scopeMap);

        if (expressionTypeLhs.equals(ExpressionType.COLOR) || expressionTypeRhs.equals(ExpressionType.COLOR) ) {
            operation.setError("Operation " + operation.lhs + " " +  operation.rhs + " contains  a color expression");
        }
    }

    // CH05 - Controleer of de conditie bij een if-statement van het type boolean is.
    private void checkIfClause(IfClause ifClause) {
        Expression conditionalExpression = ifClause.getConditionalExpression();
        ExpressionType expressionType = checkExpression.getExpressionType(conditionalExpression, scopeMap);
        if (
                !expressionType.equals(ExpressionType.BOOL)
                        &&
                !(conditionalExpression instanceof BoolLiteral)
        ) {
            ifClause.setError("IfClause " + ifClause + " is not of type boolean");
        }
    }

    //// Scoping
    private void enterScope() {
        this.scopeMap.addFirst(new HashMap<>());
    }

    private void exitScope() {
        scopeMap.removeFirst();
    }

    private boolean isNewScopeNode(ASTNode astNode) {
        return ( astNode instanceof Stylerule ||
                astNode instanceof IfClause ||
                astNode instanceof ElseClause);
    }
}
