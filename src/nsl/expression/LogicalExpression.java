/*
 * LogicalExpression.java
 */

package nsl.expression;

/**
 * Describes a logical expression.
 *
 * @author Stuart
 */
public abstract class LogicalExpression extends AssembleExpression {
	protected final Expression leftOperand;
	protected final String operator;
	protected final Expression rightOperand;

	/**
	 * Class constructor specifying the left and right operands and the operator.
	 *
	 * @param leftOperand the left operand
	 * @param operator the operator
	 * @param rightOperand the right operand
	 */
	public LogicalExpression(Expression leftOperand, String operator, Expression rightOperand) {
		this.leftOperand = leftOperand;
		this.operator = operator;
		this.rightOperand = rightOperand;
	}

	/**
	 * Gets the left operand
	 *
	 * @return the left operand
	 */
	public Expression getLeftOperand() {
		return this.leftOperand;
	}

	/**
	 * Gets the operator.
	 *
	 * @return the operator
	 */
	public String getOperator() {
		return this.operator;
	}

	/**
	 * Gets the right operand.
	 *
	 * @return the right operand
	 */
	public Expression getRightOperand() {
		return this.rightOperand;
	}

	/**
	 * Returns a string representation of the current object.
	 *
	 * @return a string representation of the current object
	 */
	@Override
	public String toString() {
		return "(" + this.leftOperand + " " + this.operator + " " + this.rightOperand + ")";
	}
}
