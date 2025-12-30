/*
 * InstProgressFlagsInstruction.java
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
public class InstProgressFlagsInstruction extends AssembleExpression {
	public static final String name = "InstProgressFlags";
	private final Expression value1;
	private final Expression value2;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public InstProgressFlagsInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 1 || paramsCount > 2) throw new NslArgumentException(name, 1, 2);

		this.value1 = paramsList.get(0);
		if (!ExpressionType.isString(this.value1))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		if (paramsCount > 1) {
			this.value2 = paramsList.get(1);
			if (!ExpressionType.isString(this.value2))
				throw new NslArgumentException(name, 2, ExpressionType.String);
		} else this.value2 = null;
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.value1);
		if (this.value2 == null) {
			ScriptParser.writeLine(name + " " + this.value1);
		} else {
			AssembleExpression.assembleIfRequired(this.value2);
			ScriptParser.writeLine(name + " " + this.value1 + " " + this.value2);
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
