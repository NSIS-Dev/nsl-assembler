/*
 * StrCpyInstruction.java
 */

package nsl.instruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;

/**
 * @author Stuart
 */
public class StrCpyInstruction extends AssembleExpression {
	public static final String name = "StrCpy";
	protected final String instructionName;
	private final Expression string;
	private final Expression maxLen;
	private final Expression startOffset;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public StrCpyInstruction(int returns) {
		this(returns, name);
	}

	/**
	 * Class constructor for subclasses with different instruction names.
	 *
	 * @param returns the number of values to return
	 * @param instructionName the name of the instruction (e.g., "StrCpy" or "UnsafeStrCpy")
	 */
	protected StrCpyInstruction(int returns, String instructionName) {
		this.instructionName = instructionName;
		if (PageExInfo.in())
			throw new NslContextException(
					EnumSet.of(NslContext.Section, NslContext.Function, NslContext.Global), instructionName);
		if (returns != 1) throw new NslReturnValueException(instructionName, 1);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 1 || paramsCount > 3) throw new NslArgumentException(instructionName, 1, 3);

		this.string = paramsList.get(0);

		if (paramsCount > 1) {
			this.maxLen = paramsList.get(1);

			if (paramsCount > 2) {
				this.startOffset = paramsList.get(2);
			} else {
				this.startOffset = null;
			}
		} else {
			this.maxLen = null;
			this.startOffset = null;
		}
	}

	/**
	 * Assembles the source code.
	 *
	 * @throws IOException
	 */
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
		Expression varOrString = AssembleExpression.getRegisterOrExpression(this.string);
		if (this.maxLen != null) {
			Expression varOrMaxLen = AssembleExpression.getRegisterOrExpression(this.maxLen);
			if (this.startOffset != null) {
				Expression varOrStartOffset = AssembleExpression.getRegisterOrExpression(this.startOffset);
				ScriptParser.writeLine(
						instructionName
								+ " "
								+ var
								+ " "
								+ varOrString
								+ " "
								+ varOrMaxLen
								+ " "
								+ varOrStartOffset);
				varOrStartOffset.setInUse(false);
			} else {
				ScriptParser.writeLine(instructionName + " " + var + " " + varOrString + " " + varOrMaxLen);
			}
			varOrMaxLen.setInUse(false);
		} else {
			ScriptParser.writeLine(instructionName + " " + var + " " + varOrString);
		}
		varOrString.setInUse(false);
	}
}
