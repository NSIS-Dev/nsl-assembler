/*
 * DeleteRegKeyInstruction.java
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
public class DeleteRegKeyInstruction extends AssembleExpression {
	public static final String name = "DeleteRegKey";
	private final Expression rootKey;
	private final Expression subKey;
	private final Expression ifEmpty;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public DeleteRegKeyInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 2 || paramsCount > 3) throw new NslArgumentException(name, 2, 3);

		this.rootKey = paramsList.get(0);
		if (!ExpressionType.isString(this.rootKey))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		this.subKey = paramsList.get(1);

		if (paramsCount > 2) {
			this.ifEmpty = paramsList.get(2);
			if (!ExpressionType.isBoolean(this.ifEmpty))
				throw new NslArgumentException(name, 3, ExpressionType.Boolean);
		} else this.ifEmpty = null;
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.rootKey);
		Expression varOrSubKey = AssembleExpression.getRegisterOrExpression(this.subKey);
		AssembleExpression.assembleIfRequired(this.ifEmpty);
		ScriptParser.writeLine(
				name
						+ " "
						+ (this.ifEmpty != null && this.ifEmpty.getBooleanValue() == true ? "/ifempty " : "")
						+ this.rootKey
						+ " "
						+ varOrSubKey);
		varOrSubKey.setInUse(false);
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
