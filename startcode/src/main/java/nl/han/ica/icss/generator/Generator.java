package nl.han.ica.icss.generator;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class Generator {

	public String generate(AST ast) {
		StringBuilder stringBuilder = new StringBuilder();
		generateNodes(ast.root.body, stringBuilder, 0);
		return stringBuilder.toString();
	}

	private void generateNodes(Iterable<ASTNode> nodes, StringBuilder stringBuilder, int indentLevel) {
		for (ASTNode node : nodes) {
			if (node instanceof Stylerule) {
				generateStylerule((Stylerule) node, stringBuilder, indentLevel);
			} else if (node instanceof Declaration) {
				generateDeclaration((Declaration) node, stringBuilder, indentLevel);
			}
		}
	}

	private void zgenerateStylerule(Stylerule rule, StringBuilder stringBuilder, int indentLevel) {
		String indent = "  ".repeat(indentLevel);

		String selectorString = rule.selectors.stream()
				.map(Object::toString)
				.reduce((a, b) -> a + ", " + b)
				.orElse("");
		stringBuilder.append(indent).append(selectorString).append(" {\n");

		Map<String, Declaration> finalDeclarations = new LinkedHashMap<>();
		for (ASTNode node : rule.body) {
			if (node instanceof Declaration) {
				finalDeclarations.put(((Declaration) node).property.name, (Declaration) node);
			}
		}

		for (Declaration declaration : finalDeclarations.values()) {
			generateDeclaration(declaration, stringBuilder, indentLevel + 1);
		}

		for (ASTNode node : rule.body) {
			if (node instanceof Stylerule) {
				generateStylerule((Stylerule) node, stringBuilder, indentLevel + 1);
			}
		}

		stringBuilder.append(indent).append("}\n\n");
	}

	private void generateDeclaration(Declaration declaration, StringBuilder stringBuilder, int indentLevel) {
		String indent = "  ".repeat(indentLevel);
		stringBuilder.append(indent)
				.append(declaration.property.name)
				.append(": ")
				.append(literalToString(declaration.expression))
				.append(";\n");
	}

	private String literalToString(Expression expression) {
		if (expression instanceof PixelLiteral) {
			return ((PixelLiteral) expression).value + "px";
		} else if (expression instanceof PercentageLiteral) {
			return ((PercentageLiteral) expression).value + "%";
		} else if (expression instanceof ScalarLiteral) {
			return String.valueOf(((ScalarLiteral) expression).value);
		} else if (expression instanceof BoolLiteral) {
			return String.valueOf(((BoolLiteral) expression).value);
		} else if (expression instanceof ColorLiteral) {
			return ((ColorLiteral) expression).value;
		}
		return "";
	}
}
