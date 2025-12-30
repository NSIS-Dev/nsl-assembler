/*
 * FileOpenInstruction.java
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
public class FileOpenInstruction extends AssembleExpression {
	public static final String name = "FileOpen";
	private final Expression fileName;
	private final Expression openMode;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public FileOpenInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns != 1) throw new NslReturnValueException(name, 1);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 2) throw new NslArgumentException(name, 2);

		this.fileName = paramsList.get(0);

		this.openMode = paramsList.get(1);
		if (!ExpressionType.isString(this.openMode))
			throw new NslArgumentException(name, 2, ExpressionType.String);
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
		AssembleExpression.assembleIfRequired(this.openMode);
		Expression varOrFileName = AssembleExpression.getRegisterOrExpression(this.fileName);
		ScriptParser.writeLine(name + " " + var + " " + varOrFileName + " " + this.openMode);
		varOrFileName.setInUse(false);
	}
}
