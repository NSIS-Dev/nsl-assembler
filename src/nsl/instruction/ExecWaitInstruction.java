/*
 * ExecWaitInstruction.java
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
public class ExecWaitInstruction extends AssembleExpression {
	public static final String name = "ExecWait";
	private final Expression command;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public ExecWaitInstruction(int returns) {
		if (PageExInfo.in())
			throw new NslContextException(
					EnumSet.of(NslContext.Section, NslContext.Function, NslContext.Global), name);
		if (returns > 1) throw new NslReturnValueException(name, 0, 1);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 1) throw new NslArgumentException(name, 1);

		this.command = paramsList.get(0);
	}

	/**
	 * Assembles the source code.
	 *
	 * @throws IOException
	 */
	@Override
	public void assemble() throws IOException {
		Expression varOrCommand = AssembleExpression.getRegisterOrExpression(this.command);
		ScriptParser.writeLine(name + " " + varOrCommand);
		varOrCommand.setInUse(false);
	}

	/**
	 * Assembles the source code.
	 *
	 * @param var the variable to assign the value to
	 */
	@Override
	public void assemble(Register var) throws IOException {
		Expression varOrCommand = AssembleExpression.getRegisterOrExpression(this.command);
		ScriptParser.writeLine(name + " " + varOrCommand + " " + var);
		varOrCommand.setInUse(false);
	}
}
