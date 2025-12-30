/*
 * BooleanExpression.java
 */

package nsl.expression;

import java.io.IOException;
import nsl.*;

/**
 * Describes a Boolean expression.
 *
 * @author Stuart
 */
public class BooleanExpression extends ConditionalExpression {
	/** Class constructor. */
	protected BooleanExpression() {
		super(null, null, null);
	}

	/**
	 * Class constructor specifying the left and right operands and the operator.
	 *
	 * @param leftOperand the left operand
	 * @param operator the operator
	 * @param rightOperand the right operand
	 */
	public BooleanExpression(Expression leftOperand, String operator, Expression rightOperand) {
		super(leftOperand, operator, rightOperand);

		if (!isBooleanExpression(leftOperand))
			throw new NslException("The left operand must be an equality or relational expression", true);
		if (!isBooleanExpression(rightOperand))
			throw new NslException(
					"The right operand must be an equality or relational expression", true);
	}

	/**
	 * Checks if the given expression is suitable for use as a left or right operand in a Boolean
	 * expression.
	 *
	 * @param expression the expression to check
	 * @return whether the given expression is a Boolean expression
	 */
	private static boolean isBooleanExpression(Expression expression) {
		return expression instanceof BooleanExpression
				|| expression instanceof ComparisonExpression
				|| expression.type.equals(ExpressionType.Boolean);
	}

	/**
	 * Assembles the source code.
	 *
	 * @param var the variable to assign the value to
	 */
	@Override
	public void assemble(Register var) throws IOException {
		this.assemble(RelativeJump.Zero, new RelativeJump("+3"));
		ScriptParser.writeLine("StrCpy " + var + " true");
		ScriptParser.writeLine("Goto +2");
		ScriptParser.writeLine("StrCpy " + var + " false");
	}

	/**
	 * Assembles the given boolean expression.
	 *
	 * @param expression the expression to assemble
	 * @param gotoA the first go-to label
	 * @param gotoB the second go-to label
	 */
	private static void assemble(Expression expression, Label gotoA, Label gotoB) throws IOException {
		if (expression.isLiteral()) {
			AssembleExpression.assembleIfRequired(expression);

			if (expression.booleanValue == true) ScriptParser.writeLine("Goto " + gotoA);
			else ScriptParser.writeLine("Goto " + gotoB);
		} else if (expression instanceof ConditionalExpression) {
			((ConditionalExpression) expression).assemble(gotoA, gotoB);
		} else {
			// Sanity check.
			throw new NslException("Expression is not a Boolean expression");
		}
	}

	/**
	 * Assembles the source code.
	 *
	 * @param gotoA the first go-to label
	 * @param gotoB the second go-to label
	 */
	public void assemble(Label gotoA, Label gotoB) throws IOException {
		// Switch the labels around if we the negate (!) operator was used.
		if (this.booleanValue) {
			Label gotoTemp = gotoA;
			gotoA = gotoB;
			gotoB = gotoTemp;
		}

		if (this.leftOperand instanceof ComparisonExpression
				&& this.rightOperand instanceof ComparisonExpression) {
			if (this.operator.equals("&&")) {
				((ConditionalExpression) this.leftOperand).assemble(RelativeJump.Zero, gotoB);
				((ConditionalExpression) this.rightOperand).assemble(gotoA, gotoB);
			} else {
				((ConditionalExpression) this.leftOperand).assemble(gotoA, RelativeJump.Zero);
				((ConditionalExpression) this.rightOperand).assemble(gotoA, gotoB);
			}
		} else if (this.leftOperand instanceof ComparisonExpression) {
			if (this.operator.equals("&&")) {
				((ConditionalExpression) this.leftOperand).assemble(RelativeJump.Zero, gotoB);
				assemble(this.rightOperand, gotoA, gotoB);
			} else {
				((ConditionalExpression) this.leftOperand).assemble(gotoA, RelativeJump.Zero);
				assemble(this.rightOperand, gotoA, gotoB);
			}
		} else if (this.rightOperand instanceof ComparisonExpression) {
			if (this.operator.equals("&&")) {
				Label gotoC = LabelList.getCurrent().getNext();
				assemble(this.leftOperand, gotoC, gotoB);
				gotoC.write();
				((ConditionalExpression) this.rightOperand).assemble(gotoA, gotoB);
			} else {
				Label gotoC = LabelList.getCurrent().getNext();
				assemble(this.leftOperand, gotoA, gotoC);
				gotoC.write();
				((ConditionalExpression) this.rightOperand).assemble(gotoA, gotoB);
			}
		} else {
			if (this.operator.equals("&&")) {
				Label gotoC = LabelList.getCurrent().getNext();
				assemble(this.leftOperand, gotoC, gotoB);
				gotoC.write();
				assemble(this.rightOperand, gotoA, gotoB);
			} else {
				Label gotoC = LabelList.getCurrent().getNext();
				Label gotoD = LabelList.getCurrent().getNext();
				assemble(this.leftOperand, gotoC, gotoD);
				gotoD.write();
				assemble(this.rightOperand, gotoC, gotoB);
				gotoC.write();
			}
		}
	}
}
