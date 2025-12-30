/*
 * AbortInstruction.java
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
public class AbortInstruction extends AssembleExpression {
	public static final String name = "Abort";
	private final Expression userMessage;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public AbortInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount > 1) throw new NslArgumentException(name, 0, 1);

		if (paramsCount > 0) this.userMessage = paramsList.get(0);
		else this.userMessage = null;
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		if (this.userMessage == null) {
			ScriptParser.writeLine(name);
		} else {
			Expression varOrUserMessage = AssembleExpression.getRegisterOrExpression(this.userMessage);
			ScriptParser.writeLine(name + " " + varOrUserMessage);
			varOrUserMessage.setInUse(false);
		}
	}

	/**
	 * Assembles the source code.
	 *
	 * @param var the variable to assign the value to
	 */
	@Override
	public void assemble(Register var) throws IOException {
		throw new UnsupportedOperationException("Not supported.");
	}
}
