/*
 * MathematicalExpression.java
 */

package nsl.expression;

import java.io.IOException;
import nsl.*;

/**
 * Describes a mathematical expression.
 *
 * @author Stuart
 */
public class MathematicalExpression extends LogicalExpression {
	/**
	 * Class constructor specifying the left and right operands and the operator.
	 *
	 * @param leftOperand the left operand
	 * @param operator the operator
	 * @param rightOperand the right operand
	 */
	public MathematicalExpression(Expression leftOperand, String operator, Expression rightOperand) {
		super(leftOperand, operator, rightOperand);

		/*if (!(leftOperand instanceof MathematicalExpression) && leftOperand.type != ExpressionType.Register && leftOperand.type != ExpressionType.Integer)
			throw new NslException("The left operand must be an integer or a variable", true);
		if (!(rightOperand instanceof MathematicalExpression) && rightOperand.type != ExpressionType.Register && rightOperand.type != ExpressionType.Integer)
			throw new NslException("The right operand must be an integer or a variable", true);*/
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
		this.assemble(var, 0);
	}

	/**
	 * Assembles the source code.
	 *
	 * @param var the variable to assign the value to
	 * @param counter the recursion counter
	 */
	protected void assemble(Register var, int counter) throws IOException {
		counter++;

		Register varLeft;
		if (this.leftOperand instanceof AssignmentExpression)
			varLeft = RegisterList.getCurrent().get(this.leftOperand.integerValue);
		else if (counter == 1) varLeft = RegisterList.getCurrent().getNext();
		else varLeft = var;

		if (this.leftOperand instanceof MathematicalExpression
				&& this.rightOperand instanceof MathematicalExpression) {
			((MathematicalExpression) this.leftOperand).assemble(varLeft, counter);
			Register varRight = RegisterList.getCurrent().getNext();
			((MathematicalExpression) this.rightOperand).assemble(varRight, counter);
			ScriptParser.writeLine(
					String.format("IntOp %s %s %s %s", var, varLeft, this.operator, varRight));
			varRight.setInUse(false);
		} else if (this.leftOperand instanceof MathematicalExpression) {
			((MathematicalExpression) this.leftOperand).assemble(varLeft, counter);
			if (this.operator.toString().equals("~"))
				ScriptParser.writeLine(String.format("IntOp %s %s %s", var, varLeft, this.operator));
			else
				ScriptParser.writeLine(
						String.format("IntOp %s %s %s %s", var, varLeft, this.operator, this.rightOperand));
		} else if (this.rightOperand instanceof MathematicalExpression) {
			((MathematicalExpression) this.rightOperand).assemble(varLeft, counter);
			ScriptParser.writeLine(
					String.format("IntOp %s %s %s %s", var, this.leftOperand, this.operator, varLeft));
		} else if (this.leftOperand instanceof AssembleExpression
				|| this.rightOperand instanceof AssembleExpression) {
			Expression varOrLeft = AssembleExpression.getRegisterOrExpression(this.leftOperand);
			if (this.operator.toString().equals("~")) {
				ScriptParser.writeLine(String.format("IntOp %s %s %s", var, varOrLeft, this.operator));
			} else {
				Expression varOrRight = AssembleExpression.getRegisterOrExpression(this.rightOperand);
				ScriptParser.writeLine(
						String.format("IntOp %s %s %s %s", var, varOrLeft, this.operator, varOrRight));
				varOrRight.setInUse(false);
			}
			varOrLeft.setInUse(false);
		} else {
			if (this.operator.toString().equals("~"))
				ScriptParser.writeLine(
						String.format("IntOp %s %s %s", var, this.leftOperand, this.operator));
			else
				ScriptParser.writeLine(
						String.format(
								"IntOp %s %s %s %s", var, this.leftOperand, this.operator, this.rightOperand));
		}

		if (counter == 1) varLeft.setInUse(false);
	}
}
