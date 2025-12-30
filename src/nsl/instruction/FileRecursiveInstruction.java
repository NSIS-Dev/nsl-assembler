/*
 * FileRecursiveInstruction.java
 */

package nsl.instruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;

/**
 * The File NSIS instruction with the /r (recursive) switch.
 *
 * @author Stuart
 */
public class FileRecursiveInstruction extends AssembleExpression {
	public static final String name = "FileRecursive";
	private final Expression inFile;
	private final Expression nonFatal;
	private final Expression saveAttributes;
	private final ArrayList<Expression> excludesList;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public FileRecursiveInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount == 0) throw new NslArgumentException(name, 1, 999);

		this.inFile = paramsList.get(0);
		if (!ExpressionType.isString(this.inFile))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		this.excludesList = new ArrayList<Expression>();
		int excludesListStarts = 3;

		if (paramsCount > 1) {
			Expression nonFatalOrExclude = paramsList.get(1);
			if (ExpressionType.isBoolean(nonFatalOrExclude)) {
				this.nonFatal = nonFatalOrExclude;

				if (paramsCount > 2) {
					Expression saveAttributesOrExclude = paramsList.get(2);

					if (ExpressionType.isBoolean(saveAttributesOrExclude)) {
						this.saveAttributes = saveAttributesOrExclude;
					} else {
						if (!ExpressionType.isString(saveAttributesOrExclude))
							throw new NslArgumentException(name, 3, ExpressionType.String);

						this.excludesList.add(saveAttributesOrExclude);
						excludesListStarts = 3;

						this.saveAttributes = null;
					}
				} else {
					this.saveAttributes = null;
				}
			} else {
				if (!ExpressionType.isString(nonFatalOrExclude))
					throw new NslArgumentException(name, 2, ExpressionType.String);

				this.excludesList.add(nonFatalOrExclude);
				excludesListStarts = 2;

				this.nonFatal = null;
				this.saveAttributes = null;
			}

			for (int i = excludesListStarts; i < paramsCount; i++) {
				Expression exclude = paramsList.get(i);

				if (!ExpressionType.isString(exclude))
					throw new NslArgumentException(name, i + 1, ExpressionType.String);

				this.excludesList.add(exclude);
			}
		} else {
			this.nonFatal = null;
			this.saveAttributes = null;
		}
	}

	/**
	 * Assembles the source code.
	 *
	 * @throws IOException
	 */
	@Override
	public void assemble() throws IOException {
		String write = "File ";

		AssembleExpression.assembleIfRequired(this.inFile);

		if (this.nonFatal != null) {
			AssembleExpression.assembleIfRequired(this.nonFatal);
			if (this.nonFatal.getBooleanValue()) write += "/nonfatal ";
		}

		if (this.saveAttributes != null) {
			AssembleExpression.assembleIfRequired(this.saveAttributes);
			if (this.saveAttributes.getBooleanValue()) write += "/a ";
		}

		write += "/r ";

		for (Expression exclude : this.excludesList) {
			AssembleExpression.assembleIfRequired(exclude);
			write += "/x " + exclude + " ";
		}

		ScriptParser.writeLine(write + this.inFile);
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
