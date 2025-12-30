/*
 * InstallButtonTextInstruction.java
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
public class InstallButtonTextInstruction extends AssembleExpression {
	public static final String name = "InstallButtonText";
	private final Expression value;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public InstallButtonTextInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 1) throw new NslArgumentException(name, 1);

		this.value = paramsList.get(0);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		Expression varOrValue = AssembleExpression.getRegisterOrExpression(this.value);
		ScriptParser.writeLine(name + " " + varOrValue);
		varOrValue.setInUse(false);
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
