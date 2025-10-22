package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
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

    public void walkThroughAst(ASTNode astNode) {
        if (isNewScopeNode(astNode)) enterScope(astNode);

        if (astNode instanceof VariableAssignment) pushAssignment((VariableAssignment) astNode);

        if (astNode instanceof VariableReference) checkVariableReference((VariableReference) astNode);
        if (astNode instanceof IfClause) checkIfClause((IfClause) astNode);

        if (astNode instanceof Operation) {
            Operation operation = (Operation) astNode;
            checkOperationColours(operation);
            checkOperationTypeAndScalar(operation);
        }

        for (ASTNode child : astNode.getChildren()) walkThroughAst(child);

        if (isNewScopeNode(astNode)) exitScope();
    }

    private void pushAssignment(VariableAssignment astnode) {
        scopeMap.getFirst().put(astnode.name.name, checkExpression.getExpressionType(astnode.expression, scopeMap));
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

    // TODO: CH02 - Controleer of de operanden van de operaties plus en min van gelijk type zijn. & Een operand scalair bij vermenigvuldigen
    private void checkOperationTypeAndScalar(Operation operation) {
        // Ophalen van beiden helften

        // TODO: Operaties bij plus en min moeten van gelijk type zijn
            // Is operation plussen of minnen
                // If conditie dat als ze niet gelijk zijn aan elkaar de node als error wordt gezet.


        // TODO: Bij operaties die vermenigvuldigen moet een van de twee scalair zijn.
            // Is operation vermenigvuldingen
                // If conditie waarin als beiden geen scalair zijn de node als error wordt gezet.
    }

    // CH03 - Controleer of er geen kleuren worden gebruikt in operaties (plus, min en keer).
    private void checkOperationColours(Operation operation) {
        ExpressionType expressionTypeLhs = checkExpression.getExpressionType(operation.lhs, scopeMap);
        ExpressionType expressionTypeRhs = checkExpression.getExpressionType(operation.rhs, scopeMap);

        if (expressionTypeLhs.equals(ExpressionType.COLOR) || expressionTypeRhs.equals(ExpressionType.COLOR) ) {
            operation.setError("Operation " + operation.lhs + " " +  operation.rhs + " contains  a color expression");
        }
    }

    // CH06 - Controleer of de conditie bij een if-statement van het type boolean is.
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
    private void enterScope(ASTNode astNode) {
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
