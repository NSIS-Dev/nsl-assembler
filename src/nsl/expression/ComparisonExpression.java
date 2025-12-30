/*
 * ComparisonExpression.java
 */

package nsl.expression;

import java.io.IOException;
import nsl.*;

/**
 * Describes a comparison expression.
 *
 * @author Stuart
 */
public class ComparisonExpression extends ConditionalExpression {
	private final ComparisonType comparisonType;

	/**
	 * Class constructor specifying the left and right operands and the operator.
	 *
	 * @param leftOperand the left operand
	 * @param operator the operator
	 * @param rightOperand the right operand
	 * @param comparisonType how the two operands should be compared
	 */
	public ComparisonExpression(
			Expression leftOperand,
			String operator,
			Expression rightOperand,
			ComparisonType comparisonType) {
		super(leftOperand, operator, rightOperand);
		this.comparisonType = comparisonType;
	}

	/** Class constructor. */
	public ComparisonExpression() {
		super(null, null, null);
		this.comparisonType = ComparisonType.Integer;
	}

	/**
	 * Assembles the source code for comparing the left and right operands.
	 *
	 * @param leftOperand the left operand
	 * @param operator the comparison operator
	 * @param rightOperand the right operand
	 * @param gotoA the first go-to label
	 * @param gotoB the second go-to label
	 * @param comparisonType how the two operands should be compared
	 */
	private static void assembleCmp(
			String leftOperand,
			String operator,
			String rightOperand,
			Label gotoA,
			Label gotoB,
			ComparisonType comparisonType)
			throws IOException {
		if (operator.equals("==")) {
			if (comparisonType == ComparisonType.String)
				ScriptParser.writeLine(
						String.format("StrCmp %s %s %s %s", leftOperand, rightOperand, gotoA, gotoB));
			else if (comparisonType == ComparisonType.StringCaseSensitive)
				ScriptParser.writeLine(
						String.format("StrCmpS %s %s %s %s", leftOperand, rightOperand, gotoA, gotoB));
			else if (comparisonType == ComparisonType.IntegerUnsigned)
				ScriptParser.writeLine(
						String.format(
								"IntCmpU %s %s %s %s %s", leftOperand, rightOperand, gotoA, gotoB, gotoB));
			else
				ScriptParser.writeLine(
						String.format("IntCmp %s %s %s %s %s", leftOperand, rightOperand, gotoA, gotoB, gotoB));
		} else if (operator.equals("!=")) {
			if (comparisonType == ComparisonType.String)
				ScriptParser.writeLine(
						String.format("StrCmp %s %s %s %s", leftOperand, rightOperand, gotoB, gotoA));
			else if (comparisonType == ComparisonType.StringCaseSensitive)
				ScriptParser.writeLine(
						String.format("StrCmpS %s %s %s %s", leftOperand, rightOperand, gotoB, gotoA));
			else if (comparisonType == ComparisonType.IntegerUnsigned)
				ScriptParser.writeLine(
						String.format(
								"IntCmpU %s %s %s %s %s", leftOperand, rightOperand, gotoB, gotoA, gotoA));
			else
				ScriptParser.writeLine(
						String.format("IntCmp %s %s %s %s %s", leftOperand, rightOperand, gotoB, gotoA, gotoA));
		} else if (operator.equals("<=")) {
			if (comparisonType == ComparisonType.IntegerUnsigned)
				ScriptParser.writeLine(
						String.format(
								"IntCmpU %s %s %s %s %s", leftOperand, rightOperand, gotoA, gotoA, gotoB));
			else
				ScriptParser.writeLine(
						String.format("IntCmp %s %s %s %s %s", leftOperand, rightOperand, gotoA, gotoA, gotoB));
		} else if (operator.equals(">=")) {
			if (comparisonType == ComparisonType.IntegerUnsigned)
				ScriptParser.writeLine(
						String.format(
								"IntCmpU %s %s %s %s %s", leftOperand, rightOperand, gotoA, gotoB, gotoA));
			else
				ScriptParser.writeLine(
						String.format("IntCmp %s %s %s %s %s", leftOperand, rightOperand, gotoA, gotoB, gotoA));
		} else if (operator.equals("<")) {
			if (comparisonType == ComparisonType.IntegerUnsigned)
				ScriptParser.writeLine(
						String.format(
								"IntCmpU %s %s %s %s %s", leftOperand, rightOperand, gotoB, gotoA, gotoB));
			else
				ScriptParser.writeLine(
						String.format("IntCmp %s %s %s %s %s", leftOperand, rightOperand, gotoB, gotoA, gotoB));
		} else if (operator.equals(">")) {
			if (comparisonType == ComparisonType.IntegerUnsigned)
				ScriptParser.writeLine(
						String.format(
								"IntCmpU %s %s %s %s %s", leftOperand, rightOperand, gotoB, gotoB, gotoA));
			else
				ScriptParser.writeLine(
						String.format("IntCmp %s %s %s %s %s", leftOperand, rightOperand, gotoB, gotoB, gotoA));
		} else throw new NslException("Unknown operator " + operator);
	}

	/**
	 * Assembles the source code.
	 *
	 * @param var the variable to assign the value to
	 */
	public void assemble(Register var) throws IOException {
		Label gotoA = LabelList.getCurrent().getNext();
		Label gotoB = LabelList.getCurrent().getNext();

		if (this.booleanValue) this.assemble(gotoB, gotoA);
		else this.assemble(gotoA, gotoB);
		gotoA.write();
		ScriptParser.writeLine("StrCpy " + var + " true");
		ScriptParser.writeLine("Goto +2");
		gotoB.write();
		ScriptParser.writeLine("StrCpy " + var + " false");
	}

	/**
	 * Assembles the source code.
	 *
	 * @param gotoA the first go-to label
	 * @param gotoB the second go-to label
	 */
	public void assemble(Label gotoA, Label gotoB) throws IOException {
		if (this.booleanValue) {
			Label gotoTemp = gotoA;
			gotoA = gotoB;
			gotoB = gotoTemp;
		}

		if (this.leftOperand instanceof AssembleExpression
				&& this.rightOperand instanceof AssembleExpression) {
			Register varLeft = RegisterList.getCurrent().getNext();
			Register varRight = RegisterList.getCurrent().getNext();
			((AssembleExpression) this.leftOperand).assemble(varLeft);
			((AssembleExpression) this.rightOperand).assemble(varRight);
			assembleCmp(
					varLeft.toString(),
					this.operator,
					varRight.toString(),
					gotoA,
					gotoB,
					this.comparisonType);
			varRight.setInUse(false);
			varLeft.setInUse(false);
		} else if (this.leftOperand instanceof AssembleExpression) {
			Register varLeft = RegisterList.getCurrent().getNext();
			((AssembleExpression) this.leftOperand).assemble(varLeft);
			assembleCmp(
					varLeft.toString(),
					this.operator,
					this.rightOperand.toString(),
					gotoA,
					gotoB,
					this.comparisonType);
			varLeft.setInUse(false);
		} else if (this.rightOperand instanceof AssembleExpression) {
			Register varRight = RegisterList.getCurrent().getNext();
			((AssembleExpression) this.rightOperand).assemble(varRight);
			assembleCmp(
					this.leftOperand.toString(),
					this.operator,
					varRight.toString(),
					gotoA,
					gotoB,
					this.comparisonType);
			varRight.setInUse(false);
		} else {
			assembleCmp(
					this.leftOperand.toString(),
					this.operator,
					this.rightOperand.toString(),
					gotoA,
					gotoB,
					this.comparisonType);
		}
	}
}
