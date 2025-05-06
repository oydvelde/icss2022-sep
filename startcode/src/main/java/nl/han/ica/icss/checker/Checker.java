package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;


public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new HANLinkedList<>();

        walkThroughAst(ast.root);
    }

    public void walkThroughAst(ASTNode astNode) {
        if (isNewScopeNode(astNode)) enterScope();

        if (astNode instanceof VariableAssignment) {

        }

        System.out.println(astNode.getClass().getName() + " " + isNewScopeNode(astNode));

        for (ASTNode child : astNode.getChildren()) {
            walkThroughAst(child);
        }

        if (isNewScopeNode(astNode)) exitScope();
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
        for (int i = 0; i < variableTypes.getSize(); i++) {
            HashMap<String, ExpressionType> scope = variableTypes.get(i);
            if (scope.containsKey(variableName)) return true;
        }
        return false;
    }

    // CH02 - Controleer of de operanden van de operaties plus en min van gelijk type zijn. & Een operand scalair bij vermenigvuldigen

    // CH03 - Controleer of er geen kleuren worden gebruikt in operaties (plus, min en keer).

    // CH06 - Controleer of de conditie bij een if-statement van het type boolean is.


    //// Scoping
    private void enterScope() {
        this.variableTypes.addFirst(new HashMap<>());
    }

    private void exitScope() {
        this.variableTypes.removeFirst();
    }

    private boolean isNewScopeNode(ASTNode astNode) {
        return (astNode instanceof Stylerule ||
                astNode instanceof IfClause ||
                astNode instanceof ElseClause);
    }
}
