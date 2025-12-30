/*
 * NameInstruction.java
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
public class NameInstruction extends AssembleExpression {
	public static final String name = "Name";
	private final Expression value;
	private final Expression valueDoubleAmpersands;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public NameInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 1 || paramsCount > 2) throw new NslArgumentException(name, 1, 2);

		this.value = paramsList.get(0);

		if (paramsCount > 1) this.valueDoubleAmpersands = paramsList.get(1);
		else this.valueDoubleAmpersands = null;
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		Expression varOrValue = AssembleExpression.getRegisterOrExpression(this.value);
		if (this.valueDoubleAmpersands == null) {
			ScriptParser.writeLine(name + " " + varOrValue);
		} else {
			Expression varOrValueDoubleAmpersands =
					AssembleExpression.getRegisterOrExpression(this.valueDoubleAmpersands);
			ScriptParser.writeLine(name + " " + varOrValue + " " + varOrValueDoubleAmpersands);
			varOrValueDoubleAmpersands.setInUse(false);
		}
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
