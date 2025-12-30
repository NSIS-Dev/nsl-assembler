/*
 * ReserveFileRecursiveInstruction.java
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
public class ReserveFileRecursiveInstruction extends AssembleExpression {
	public static final String name = "ReserveFileRecursive";
	private final Expression file;
	private final Expression nonFatal;
	private final ArrayList<Expression> excludesList;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public ReserveFileRecursiveInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount == 0) throw new NslArgumentException(name, 1, 999);

		this.file = paramsList.get(0);
		if (!ExpressionType.isString(this.file))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		this.excludesList = new ArrayList<Expression>();

		if (paramsCount > 1) {
			Expression nonFatalOrExclude = paramsList.get(1);
			if (ExpressionType.isBoolean(nonFatalOrExclude)) {
				this.nonFatal = nonFatalOrExclude;
			} else {
				if (!ExpressionType.isString(nonFatalOrExclude))
					throw new NslArgumentException(name, 2, ExpressionType.String);

				this.excludesList.add(nonFatalOrExclude);

				this.nonFatal = null;
			}

			for (int i = 2; i < paramsCount; i++) {
				Expression exclude = paramsList.get(i);

				if (!ExpressionType.isString(exclude))
					throw new NslArgumentException(name, i + 1, ExpressionType.String);

				this.excludesList.add(exclude);
			}
		} else {
			this.nonFatal = null;
		}
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		String write = "ReserveFile ";

		AssembleExpression.assembleIfRequired(this.file);

		if (this.nonFatal != null) {
			AssembleExpression.assembleIfRequired(this.nonFatal);
			if (this.nonFatal.getBooleanValue()) write += "/nonfatal ";
		}

		write += "/r ";

		for (Expression exclude : this.excludesList) {
			AssembleExpression.assembleIfRequired(exclude);
			write += "/x " + exclude + " ";
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
