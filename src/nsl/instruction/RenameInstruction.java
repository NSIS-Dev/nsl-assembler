/*
 * RenameInstruction.java
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
public class RenameInstruction extends AssembleExpression {
	public static final String name = "Rename";
	private final Expression sourceFile;
	private final Expression destFile;
	private final Expression rebootOK;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public RenameInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 1 || paramsCount > 3) throw new NslArgumentException(name, 1, 3);

		this.sourceFile = paramsList.get(0);

		if (paramsCount > 1) {
			this.destFile = paramsList.get(1);

			if (paramsCount > 2) {
				this.rebootOK = paramsList.get(2);
				if (!ExpressionType.isBoolean(this.rebootOK))
					throw new NslArgumentException(name, 3, ExpressionType.Boolean);
			} else this.rebootOK = null;
		} else {
			this.destFile = null;
			this.rebootOK = null;
		}
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		Expression varOrSourceFile = AssembleExpression.getRegisterOrExpression(this.sourceFile);
		Expression varOrDestFile = AssembleExpression.getRegisterOrExpression(this.destFile);
		String write = name + " ";
		if (this.rebootOK != null) {
			AssembleExpression.assembleIfRequired(this.rebootOK);
			if (this.rebootOK.getBooleanValue() == true) write += "/REBOOTOK ";
		}
		ScriptParser.writeLine(write + varOrSourceFile + " " + varOrDestFile);
		varOrSourceFile.setInUse(false);
		varOrDestFile.setInUse(false);
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
