/*
 * IfAbortInstruction.java
 */

package nsl.instruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;
import nsl.statement.SwitchCaseStatement;

/**
 * The NSIS IfAbort instruction.
 *
 * @author Stuart
 */
public class IfAbortInstruction extends JumpExpression {
	public static final String name = "AbortCalled";

	/**
	 * Class constructor.
	 *
	 * @param returns the number of return values
	 */
	public IfAbortInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns != 1) throw new NslReturnValueException(name, 1);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (!paramsList.isEmpty()) throw new NslArgumentException(name, 0);

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

		if (this.booleanValue) ScriptParser.writeLine("IfAbort 0 +3");
		else ScriptParser.writeLine("IfAbort +3");
		ScriptParser.writeLine("StrCpy " + var + " true");
		ScriptParser.writeLine("Goto +2");
		ScriptParser.writeLine("StrCpy " + var + " false");
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

		if (this.booleanValue == true) ScriptParser.writeLine("IfAbort " + gotoA + " " + gotoB);
		else ScriptParser.writeLine("IfAbort " + gotoB + " " + gotoA);
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

		ScriptParser.writeLine("IfAbort" + gotoA + gotoB);
	}
}
