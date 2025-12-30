/*
 * InitPluginsDirInstruction.java
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
public class InitPluginsDirInstruction extends AssembleExpression {
	public static final String name = "InitPluginsDir";

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public InitPluginsDirInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (!paramsList.isEmpty()) throw new NslArgumentException(name, 0);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		ScriptParser.writeLine(name);
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
