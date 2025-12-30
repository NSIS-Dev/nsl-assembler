/*
 * GetTempFileNameInstruction.java
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
public class GetTempFileNameInstruction extends AssembleExpression {
	public static final String name = "GetTempFileName";
	private final Expression baseDir;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public GetTempFileNameInstruction(int returns) {
		if (PageExInfo.in())
			throw new NslContextException(
					EnumSet.of(NslContext.Section, NslContext.Function, NslContext.Global), name);
		if (returns != 1) throw new NslReturnValueException(name, 1);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount > 1) throw new NslArgumentException(name, 0, 1);

		if (paramsCount > 1) this.baseDir = paramsList.get(0);
		else this.baseDir = null;
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
		if (this.baseDir == null) {
			ScriptParser.writeLine(name + " " + var);
		} else {
			Expression varOrBaseDir = AssembleExpression.getRegisterOrExpression(this.baseDir);
			ScriptParser.writeLine(name + " " + var + " " + varOrBaseDir);
			varOrBaseDir.setInUse(false);
		}
	}
}
