/*
 * ConditionalExpression.java
 */

package nsl.expression;

import java.io.IOException;
import nsl.*;

/**
 * Describes an equality expression.
 *
 * @author Stuart
 */
public abstract class ConditionalExpression extends LogicalExpression {
	/**
	 * Class constructor specifying the left and right operands and the operator.
	 *
	 * @param leftOperand the left operand
	 * @param operator the operator
	 * @param rightOperand the right operand
	 */
	public ConditionalExpression(Expression leftOperand, String operator, Expression rightOperand) {
		super(leftOperand, operator, rightOperand);
		this.type = ExpressionType.Boolean;
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
	public abstract void assemble(Register var) throws IOException;

	/**
	 * Assembles the source code.
	 *
	 * @param gotoA the first go-to label
	 * @param gotoB the second go-to label
	 */
	public abstract void assemble(Label gotoA, Label gotoB) throws IOException;
}
