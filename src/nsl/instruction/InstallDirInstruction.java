/*
 * InstallDirInstruction.java
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
public class InstallDirInstruction extends AssembleExpression {
	public static final String name = "InstallDir";
	private final Expression installDir;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public InstallDirInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 1) throw new NslArgumentException(name, 1);

		this.installDir = paramsList.get(0);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		Expression varOrInstallDir = AssembleExpression.getRegisterOrExpression(this.installDir);
		ScriptParser.writeLine(name + " " + varOrInstallDir);
		varOrInstallDir.setInUse(false);
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
