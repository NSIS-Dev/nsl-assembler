/*
 * StrCmpInstruction.java
 */

package nsl.instruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;
import nsl.statement.SwitchCaseStatement;

/**
 * @author Stuart
 */
public class StrCmpInstruction extends JumpExpression {
	public static final String name = "StrCmp";

	private final Expression str1;
	private final Expression str2;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of return values
	 */
	public StrCmpInstruction(int returns) {
		if (PageExInfo.in())
			throw new NslContextException(
					EnumSet.of(NslContext.Section, NslContext.Function, NslContext.Global), name);
		if (returns != 1) throw new NslReturnValueException(name, 1);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 2) throw new NslArgumentException(name, 2);

		this.str1 = paramsList.get(0);
		this.str2 = paramsList.get(1);

		if (this.str1.isLiteral()
				&& !this.str1.getType().equals(ExpressionType.Register)
				&& this.str2.isLiteral()
				&& !this.str2.getType().equals(ExpressionType.Register))
			throw new NslException(
					"\"StrCmp\" instruction used with literals for both arguments; use the \"==s\" or \"!=s\" equality operators instead",
					true);

		this.type = ExpressionType.Boolean;
		this.booleanValue = true;
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
		if (this.thrownAwayAfterOptimise != null)
			AssembleExpression.assembleIfRequired(this.thrownAwayAfterOptimise);

		Expression varOrStr1 = AssembleExpression.getRegisterOrExpression(this.str1);
		Expression varOrStr2 = AssembleExpression.getRegisterOrExpression(this.str2);
		if (this.booleanValue)
			ScriptParser.writeLine(name + " " + varOrStr1 + " " + varOrStr2 + " 0 +3");
		else ScriptParser.writeLine(name + " " + varOrStr1 + " " + varOrStr2 + " +3");
		ScriptParser.writeLine("StrCpy " + var + " true");
		ScriptParser.writeLine("Goto +2");
		ScriptParser.writeLine("StrCpy " + var + " false");
		varOrStr1.setInUse(false);
		varOrStr2.setInUse(false);
	}

	/**
	 * Assembles the source code.
	 *
	 * @param gotoA the first go-to label
	 * @param gotoB the second go-to label
	 */
	@Override
	public void assemble(Label gotoA, Label gotoB) throws IOException {
		if (this.thrownAwayAfterOptimise != null)
			AssembleExpression.assembleIfRequired(this.thrownAwayAfterOptimise);

		Expression varOrStr1 = AssembleExpression.getRegisterOrExpression(this.str1);
		Expression varOrStr2 = AssembleExpression.getRegisterOrExpression(this.str2);
		if (this.booleanValue == true)
			ScriptParser.writeLine(name + " " + varOrStr1 + " " + varOrStr2 + " " + gotoA + " " + gotoB);
		else
			ScriptParser.writeLine(name + " " + varOrStr1 + " " + varOrStr2 + " " + gotoB + " " + gotoA);
		varOrStr1.setInUse(false);
		varOrStr2.setInUse(false);
	}

	/**
	 * Assembles the source code.
	 *
	 * @param switchCases a list of {@link SwitchCaseStatement} in the switch statement statement that
	 *     this {@code JumpExpression} is being switched on.
	 */
	public void assemble(ArrayList<SwitchCaseStatement> switchCases) throws IOException {
		if (this.thrownAwayAfterOptimise != null)
			AssembleExpression.assembleIfRequired(this.thrownAwayAfterOptimise);

		String gotoA = "";
		String gotoB = "";

		for (SwitchCaseStatement caseStatement : switchCases) {
			if (caseStatement.getMatch().getBooleanValue() == this.booleanValue) {
				if (gotoA.isEmpty()) gotoA = " " + caseStatement.getLabel();
			} else {
				if (gotoB.isEmpty()) gotoB = " " + caseStatement.getLabel();
			}
		}

		if (gotoA.isEmpty()) gotoA = " 0";

		ScriptParser.writeLine(name + gotoA + gotoB);
	}
}
