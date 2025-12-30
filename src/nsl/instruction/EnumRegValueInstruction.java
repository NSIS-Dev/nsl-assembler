/*
 * EnumRegValueInstruction.java
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
public class EnumRegValueInstruction extends AssembleExpression {
	public static final String name = "EnumRegValue";
	private final Expression rootKey;
	private final Expression subKey;
	private final Expression index;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public EnumRegValueInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns != 1) throw new NslReturnValueException(name, 1);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount != 3) throw new NslArgumentException(name, 3);

		this.rootKey = paramsList.get(0);
		if (!ExpressionType.isString(this.rootKey))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		this.subKey = paramsList.get(1);

		this.index = paramsList.get(2);
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
		AssembleExpression.assembleIfRequired(this.rootKey);
		Expression varOrSubKey = AssembleExpression.getRegisterOrExpression(this.subKey);
		Expression varOrIndex = AssembleExpression.getRegisterOrExpression(this.index);
		ScriptParser.writeLine(
				name + " " + var + " " + this.rootKey + " " + varOrSubKey + " " + varOrIndex);
		varOrSubKey.setInUse(false);
		varOrIndex.setInUse(false);
	}
}
