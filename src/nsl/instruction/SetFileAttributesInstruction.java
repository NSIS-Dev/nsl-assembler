/*
 * SetFileAttributesInstruction.java
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
public class SetFileAttributesInstruction extends AssembleExpression {
	public static final String name = "SetFileAttributes";
	private final Expression file;
	private final Expression attributes;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public SetFileAttributesInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 2) throw new NslArgumentException(name, 2);

		this.file = paramsList.get(0);

		this.attributes = paramsList.get(1);
		if (!ExpressionType.isString(this.attributes))
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
		Expression varOrFile = AssembleExpression.getRegisterOrExpression(this.file);
		AssembleExpression.assembleIfRequired(this.attributes);
		ScriptParser.writeLine(name + " " + var + " " + varOrFile + " " + this.attributes);
		varOrFile.setInUse(false);
	}
}
