/*
 * ConcatenationExpression.java
 */

package nsl.expression;

import java.io.IOException;
import nsl.*;

/**
 * Describes a concatenation of expressions.
 *
 * @author Stuart
 */
public class ConcatenationExpression extends LogicalExpression {
	/** Class constructor */
	protected ConcatenationExpression() {
		super(null, null, null);
	}

	/**
	 * Class constructor specifying the left and right operands.
	 *
	 * @param leftOperand the left operand
	 * @param rightOperand the right operand
	 */
	public ConcatenationExpression(Expression leftOperand, Expression rightOperand) {
		super(leftOperand, ".", rightOperand);

		if (leftOperand.type.equals(ExpressionType.StringSpecial)
				|| rightOperand.type.equals(ExpressionType.StringSpecial))
			this.type = ExpressionType.StringSpecial;
		else this.type = ExpressionType.String;
	}

	/**
	 * Assembles the given expression into a string.
	 *
	 * @param expression the expression to assemble if necessary
	 * @return a string representation of the expression
	 * @throws IOException
	 */
	private static String assembleConcat(Expression expression) throws IOException {
		if (expression instanceof ConcatenationExpression) {
			return ((ConcatenationExpression) expression).assembleConcat();
		}

		if (expression instanceof AssembleExpression) {
			Register varLeft = RegisterList.getCurrent().getNext();
			((AssembleExpression) expression).assemble(varLeft);
			String value = varLeft.toString();
			varLeft.setInUse(false);
			return value;
		}

		if (ExpressionType.isString(expression)) return expression.stringValue;
		return expression.toString();
	}

	/**
	 * Assembles this expression into a string.
	 *
	 * @return a string representation of the expression
	 * @throws IOException
	 */
	private String assembleConcat() throws IOException {
		return assembleConcat(this.leftOperand) + assembleConcat(this.rightOperand);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		throw new UnsupportedOperationException("Not supported.");
	}

	/**
	 * Assembles the source code.
	 *
	 * @param var the variable to assign the value to
	 */
	@Override
	public void assemble(Register var) throws IOException {
		var.substitute(this.assembleConcat());
	}
}
