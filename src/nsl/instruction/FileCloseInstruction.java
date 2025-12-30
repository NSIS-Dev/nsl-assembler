/*
 * FileCloseInstruction.java
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
public class FileCloseInstruction extends AssembleExpression {
	public static final String name = "FileClose";
	private final Expression handle;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public FileCloseInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 1) throw new NslArgumentException(name, 1);

		this.handle = paramsList.get(0);
		if (!this.handle.getType().equals(ExpressionType.Register))
			throw new NslArgumentException(name, 1, ExpressionType.Register);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		ScriptParser.writeLine(name + " " + this.handle);
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
