/*
 * SetCompressorInstruction.java
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
public class SetCompressorInstruction extends AssembleExpression {
	public static final String name = "SetCompressor";
	private final Expression value;
	private final Expression solidFlag;
	private final Expression finalFlag;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public SetCompressorInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 1 || paramsCount > 3) throw new NslArgumentException(name, 1, 3);

		this.value = paramsList.get(0);
		if (!ExpressionType.isString(this.value))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		if (paramsCount > 1) {
			this.solidFlag = paramsList.get(1);
			if (!ExpressionType.isBoolean(this.solidFlag))
				throw new NslArgumentException(name, 2, ExpressionType.Boolean);
		} else this.solidFlag = null;

		if (paramsCount > 2) {
			this.finalFlag = paramsList.get(2);
			if (!ExpressionType.isBoolean(this.finalFlag))
				throw new NslArgumentException(name, 3, ExpressionType.Boolean);
		} else this.finalFlag = null;
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.value);

		String write = name;
		if (this.solidFlag != null) {
			AssembleExpression.assembleIfRequired(this.solidFlag);
			if (this.solidFlag.getBooleanValue() == true) write += " /SOLID";
		}
		if (this.finalFlag != null) {
			AssembleExpression.assembleIfRequired(this.finalFlag);
			if (this.finalFlag.getBooleanValue() == true) write += " /FINAL";
		}
		ScriptParser.writeLine(write + " " + this.value);
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
