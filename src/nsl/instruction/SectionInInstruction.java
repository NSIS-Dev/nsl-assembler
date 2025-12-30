/*
 * SectionInInstruction.java
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
public class SectionInInstruction extends AssembleExpression {
	public static final String name = "SectionIn";
	private final ArrayList<Expression> paramsList;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public SectionInInstruction(int returns) {
		if (!SectionInfo.in()) throw new NslContextException(EnumSet.of(NslContext.Section), name);
		if (returns > 0) throw new NslReturnValueException(name);

		this.paramsList = Expression.matchList();
		int paramsCount = this.paramsList.size();
		if (paramsCount == 0) throw new NslArgumentException(name, 1, 999);

		for (int i = 0; i < paramsCount; i++) {
			Expression param = this.paramsList.get(i);
			if (!ExpressionType.isInteger(param) && !ExpressionType.isBoolean(param))
				throw new NslArgumentException(name, i + 1, ExpressionType.Integer, ExpressionType.Boolean);
		}
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		String write = name;
		for (Expression param : this.paramsList) {
			AssembleExpression.assembleIfRequired(param);
			if (param.getType().equals(ExpressionType.Boolean) && param.getBooleanValue() == true)
				write += " RO";
			else write += " " + param;
		}
		ScriptParser.writeLine(write);
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
