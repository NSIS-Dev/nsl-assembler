/*
 * FileReadInstruction.java
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
public class FileReadInstruction extends AssembleExpression {
	public static final String name = "FileRead";
	private final Expression handle;
	private final Expression maxLen;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public FileReadInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns != 1) throw new NslReturnValueException(name, 1);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 1 || paramsCount > 2) throw new NslArgumentException(name, 1, 2);

		this.handle = paramsList.get(0);
		if (!this.handle.getType().equals(ExpressionType.Register))
			throw new NslArgumentException(name, 1, ExpressionType.Register);

		if (paramsCount > 1) this.maxLen = paramsList.get(1);
		else this.maxLen = null;
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
		AssembleExpression.assembleIfRequired(this.handle);
		if (this.maxLen == null) {
			ScriptParser.writeLine(name + " " + this.handle + " " + var);
		} else {
			AssembleExpression.assembleIfRequired(this.maxLen);
			ScriptParser.writeLine(name + " " + this.handle + " " + var + " " + this.maxLen);
		}
	}
}
