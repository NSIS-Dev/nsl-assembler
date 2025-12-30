/*
 * ReserveFileInstruction.java
 */

package nsl.instruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;

/**
 * The File NSIS instruction.
 *
 * @author Stuart
 */
public class ReserveFileInstruction extends AssembleExpression {
	public static final String name = "ReserveFile";
	private final Expression file;
	private final Expression nonFatal;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public ReserveFileInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 1 || paramsCount > 2) throw new NslArgumentException(name, 1, 2);

		this.file = paramsList.get(0);
		if (!ExpressionType.isString(this.file))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		if (paramsCount > 1) {
			this.nonFatal = paramsList.get(1);
			if (!ExpressionType.isBoolean(this.nonFatal))
				throw new NslArgumentException(name, 2, ExpressionType.Boolean);
		} else this.nonFatal = null;
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		String write = name + " ";

		AssembleExpression.assembleIfRequired(this.file);

		if (this.nonFatal != null) {
			AssembleExpression.assembleIfRequired(this.nonFatal);
			if (this.nonFatal.getBooleanValue()) write += "/nonfatal ";
		}

		ScriptParser.writeLine(write + this.file);
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
