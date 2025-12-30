/*
 * FileWriteUTF16LEInstruction.java
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
public class FileWriteUTF16LEInstruction extends AssembleExpression {
	public static final String name = "FileWriteUTF16LE";
	private final Expression handle;
	private final Expression text;
	private final Expression bomFlag;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public FileWriteUTF16LEInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 2 || paramsCount > 3) throw new NslArgumentException(name, 2, 3);

		this.handle = paramsList.get(0);
		if (!this.handle.getType().equals(ExpressionType.Register))
			throw new NslArgumentException(name, 1, ExpressionType.Register);

		this.text = paramsList.get(1);

		if (paramsCount > 2) {
			this.bomFlag = paramsList.get(2);
			if (!ExpressionType.isBoolean(this.bomFlag))
				throw new NslArgumentException(name, 3, ExpressionType.Boolean);
		} else this.bomFlag = null;
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.handle);
		Expression varOrText = AssembleExpression.getRegisterOrExpression(this.text);

		String write = name;
		if (this.bomFlag != null) {
			AssembleExpression.assembleIfRequired(this.bomFlag);
			if (this.bomFlag.getBooleanValue()) write += " /BOM";
		}

		ScriptParser.writeLine(write + " " + this.handle + " " + varOrText);
		varOrText.setInUse(false);
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
