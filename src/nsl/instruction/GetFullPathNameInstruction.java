/*
 * GetFullPathNameInstruction.java
 */

package nsl.instruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;

/**
 * @author Jan
 */
public class GetFullPathNameInstruction extends AssembleExpression {
	public static final String name = "GetFullPathName";
	private final Expression path;
	private final Expression shortFlag;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public GetFullPathNameInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns != 1) throw new NslReturnValueException(name, 1);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 1 || paramsCount > 2) throw new NslArgumentException(name, 1, 2);

		this.path = paramsList.get(0);

		if (paramsCount > 1) {
			this.shortFlag = paramsList.get(1);
			if (!ExpressionType.isBoolean(this.shortFlag))
				throw new NslArgumentException(name, 2, ExpressionType.Boolean);
		} else this.shortFlag = null;
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
		String write = name;
		if (this.shortFlag != null) {
			AssembleExpression.assembleIfRequired(this.shortFlag);
			if (this.shortFlag.getBooleanValue()) write += " /SHORT";
		}

		Expression varOrPath = AssembleExpression.getRegisterOrExpression(this.path);
		ScriptParser.writeLine(write + " " + var + " " + varOrPath);
		varOrPath.setInUse(false);
	}
}
