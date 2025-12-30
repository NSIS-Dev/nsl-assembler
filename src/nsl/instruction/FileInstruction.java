/*
 * FileInstruction.java
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
public class FileInstruction extends AssembleExpression {
	public static final String name = "File";
	private final Expression inFile;
	private final Expression outFile;
	private final Expression nonFatal;
	private final Expression saveAttributes;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public FileInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 1 || paramsCount > 4) throw new NslArgumentException(name, 1, 4);

		this.inFile = paramsList.get(0);
		if (!ExpressionType.isString(this.inFile))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		if (paramsCount > 1) {
			Expression outFileOrNonFatal = paramsList.get(1);
			if (ExpressionType.isBoolean(outFileOrNonFatal)) {
				this.outFile = null;
				this.nonFatal = outFileOrNonFatal;

				if (paramsCount > 2) {
					this.saveAttributes = paramsList.get(2);
					if (!ExpressionType.isBoolean(this.saveAttributes))
						throw new NslArgumentException(name, 3, ExpressionType.Boolean);
				} else this.saveAttributes = null;
			} else {
				this.outFile = outFileOrNonFatal;

				if (paramsCount > 2) {
					this.nonFatal = paramsList.get(2);
					if (!ExpressionType.isBoolean(this.nonFatal))
						throw new NslArgumentException(name, 3, ExpressionType.Boolean);

					if (paramsCount > 3) {
						this.saveAttributes = paramsList.get(3);
						if (!ExpressionType.isBoolean(this.saveAttributes))
							throw new NslArgumentException(name, 4, ExpressionType.Boolean);
					} else this.saveAttributes = null;
				} else {
					this.nonFatal = null;
					this.saveAttributes = null;
				}
			}
		} else {
			this.outFile = null;
			this.nonFatal = null;
			this.saveAttributes = null;
		}
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		String write = name + " ";

		AssembleExpression.assembleIfRequired(this.inFile);

		if (this.nonFatal != null && this.nonFatal.getBooleanValue()) write += "/nonfatal ";

		if (this.saveAttributes != null && this.saveAttributes.getBooleanValue()) write += "/a ";

		if (this.outFile != null) {
			Expression varOrOutFile = AssembleExpression.getRegisterOrExpression(this.outFile);
			write += "\"/oname=" + varOrOutFile.toString(true) + "\" ";
			varOrOutFile.setInUse(false);
		}

		if (this.nonFatal != null) AssembleExpression.assembleIfRequired(this.nonFatal);

		if (this.saveAttributes != null) AssembleExpression.assembleIfRequired(this.saveAttributes);

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
