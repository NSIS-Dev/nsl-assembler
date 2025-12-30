/*
 * CopyFilesInstruction.java
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
public class CopyFilesInstruction extends AssembleExpression {
	public static final String name = "CopyFiles";
	private final Expression source;
	private final Expression dest;
	private final Expression silent;
	private final Expression filesOnly;
	private final Expression size;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public CopyFilesInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 2 || paramsCount > 5) throw new NslArgumentException(name, 2, 5);

		this.source = paramsList.get(0);

		this.dest = paramsList.get(1);

		if (paramsCount > 2) {
			this.silent = paramsList.get(2);
			if (!ExpressionType.isBoolean(this.silent))
				throw new NslArgumentException(name, 3, ExpressionType.Boolean);

			if (paramsCount > 3) {
				this.filesOnly = paramsList.get(3);
				if (!ExpressionType.isBoolean(this.filesOnly))
					throw new NslArgumentException(name, 4, ExpressionType.Boolean);

				if (paramsCount > 4) {
					this.size = paramsList.get(4);
					if (!ExpressionType.isInteger(this.size))
						throw new NslArgumentException(name, 5, ExpressionType.Integer);
				} else this.size = null;
			} else {
				this.filesOnly = null;
				this.size = null;
			}
		} else {
			this.silent = null;
			this.filesOnly = null;
			this.size = null;
		}
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		Expression varOrSource = AssembleExpression.getRegisterOrExpression(this.source);
		Expression varOrDest = AssembleExpression.getRegisterOrExpression(this.dest);

		String write = name + " ";

		if (this.silent != null) {
			AssembleExpression.assembleIfRequired(this.silent);
			if (this.silent.getBooleanValue() == true) write += "/SILENT ";
		}

		if (this.filesOnly != null) {
			AssembleExpression.assembleIfRequired(this.filesOnly);
			if (this.filesOnly.getBooleanValue() == true) write += "/FILESONLY ";
		}

		write += varOrSource + " " + varOrDest;

		if (this.size != null) {
			AssembleExpression.assembleIfRequired(this.size);
			write += " " + this.size;
		}

		ScriptParser.writeLine(write);

		varOrSource.setInUse(false);
		varOrDest.setInUse(false);
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
