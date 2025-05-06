package nl.han.ica.icss.parser;


import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;
import nl.han.ica.datastructures.HANStack;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {

    private AST ast;

    private IHANStack<ASTNode> currentContainer;

    public ASTListener() {
        ast = new AST();
		currentContainer = new HANStack<>();
    }

    public AST getAST() {
        return ast;
    }

    @Override
    public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
        Stylesheet stylesheet = new Stylesheet();
        currentContainer.push(stylesheet);
    }

    @Override
    public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
        ast.root = (Stylesheet) currentContainer.pop();
    }

    @Override
    public void enterStylerule(ICSSParser.StyleruleContext ctx) {
        Stylerule stylerule = new Stylerule();
        currentContainer.push(stylerule);
    }

    @Override
    public void exitStylerule(ICSSParser.StyleruleContext ctx) {
        Stylerule stylerule = (Stylerule) currentContainer.pop();
        currentContainer.peek().addChild(stylerule);
    }

    @Override
    public void enterSelector(ICSSParser.SelectorContext ctx) {
        Selector selector = null;
        if (ctx.ID_IDENT() != null) {
            selector = new IdSelector(ctx.getText());
        } else if (ctx.CLASS_IDENT() != null) {
            selector = new ClassSelector(ctx.getText());
        } else if (ctx.LOWER_IDENT() != null) {
            selector = new TagSelector(ctx.getText());
        } else {

        }

        currentContainer.push(selector);
    }

    @Override
    public void exitSelector(ICSSParser.SelectorContext ctx) {
        Selector selector = (Selector) currentContainer.pop();

        currentContainer.peek().addChild(selector);
    }

    @Override
    public void enterDeclaratie(ICSSParser.DeclaratieContext ctx) {
        Declaration declaration = new Declaration();
        currentContainer.push(declaration);
    }


    @Override
    public void exitDeclaratie(ICSSParser.DeclaratieContext ctx) {
        Declaration declaration = (Declaration) currentContainer.pop();
        currentContainer.peek().addChild(declaration);
    }

    @Override
    public void enterProperty(ICSSParser.PropertyContext ctx) {
        PropertyName property = new PropertyName(ctx.getText());
        currentContainer.push(property);
    }

    @Override
    public void exitProperty(ICSSParser.PropertyContext ctx) {
        PropertyName property = (PropertyName) currentContainer.pop();
        currentContainer.peek().addChild(property);
    }

    @Override
    public void enterLiteral(ICSSParser.LiteralContext ctx) {
        if(ctx.getText().startsWith("#")){
            currentContainer.push(new ColorLiteral(ctx.getText()));
        }
        else if(ctx.getText().endsWith("px")) {
            currentContainer.push(new PixelLiteral(ctx.getText()));
        }
        else if(ctx.getText().endsWith("%")) {
            currentContainer.push(new PercentageLiteral(ctx.getText()));
        }
        else if(ctx.getText().matches("[0-9]+(\\.[0-9]+)?")) {
            currentContainer.push(new ScalarLiteral(ctx.getText()));
        }
        else if(ctx.getText().matches("TRUE") || ctx.getText().matches("FALSE")) {
            currentContainer.push(new BoolLiteral(ctx.getText()));
        }
        else if(ctx.getText().matches("[a-zA-Z]+")) {
            currentContainer.push(new VariableReference(ctx.getText()));
        }
    }

    @Override
    public void exitLiteral(ICSSParser.LiteralContext ctx) {
        if(currentContainer.peek() instanceof VariableReference) {
            VariableReference variableReference = (VariableReference) currentContainer.pop();
            currentContainer.peek().addChild(variableReference);
        } else {
            Literal literal = (Literal) currentContainer.pop();
            currentContainer.peek().addChild(literal);
        }
    }

    @Override
    public void enterExpression(ICSSParser.ExpressionContext ctx) {
        if(ctx.getChildCount() < 3) {
            return;
        }

        if (ctx.getChild(1).getText().equals("*")) {
            currentContainer.push(new MultiplyOperation());
        } else if (ctx.getChild(1).getText().equals("+")) {
            currentContainer.push(new AddOperation());
        } else if (ctx.getChild(1).getText().equals("-")) {
            currentContainer.push(new SubtractOperation());
        }
    }

    @Override
    public void exitExpression(ICSSParser.ExpressionContext ctx) {
        if(ctx.getChildCount() < 3) {
            return;
        }

        Expression expression = (Expression) currentContainer.pop();
        currentContainer.peek().addChild(expression);
    }

    @Override
    public void enterVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
        VariableAssignment variableAssignment = new VariableAssignment();
        currentContainer.push(variableAssignment);
    }

    @Override
    public void exitVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
        VariableAssignment variableAssignment = (VariableAssignment) currentContainer.pop();
        currentContainer.peek().addChild(variableAssignment);
    }

    @Override
    public void enterVariableReference(ICSSParser.VariableReferenceContext ctx) {
        VariableReference variableReference = new VariableReference(ctx.getText());
        currentContainer.push(variableReference);
    }

    @Override
    public void exitVariableReference(ICSSParser.VariableReferenceContext ctx) {
        VariableReference variableReference = (VariableReference) currentContainer.pop();
        currentContainer.peek().addChild(variableReference);
    }
}