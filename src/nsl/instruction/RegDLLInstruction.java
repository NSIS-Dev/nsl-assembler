/*
 * RegDLLInstruction.java
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
public class RegDLLInstruction extends AssembleExpression {
	public static final String name = "RegDLL";
	private final Expression file;
	private final Expression entryPoint;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public RegDLLInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 1 || paramsCount > 2) throw new NslArgumentException(name, 1, 2);

		this.file = paramsList.get(0);

		if (paramsCount > 1) this.entryPoint = paramsList.get(1);
		else this.entryPoint = null;
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		Expression varOrFile = AssembleExpression.getRegisterOrExpression(this.file);
		if (this.entryPoint == null) {
			ScriptParser.writeLine(name + " " + varOrFile);
		} else {
			Expression varOrEntryPoint = AssembleExpression.getRegisterOrExpression(this.entryPoint);
			ScriptParser.writeLine(name + " " + varOrFile + " " + varOrEntryPoint);
			varOrEntryPoint.setInUse(false);
		}
		varOrFile.setInUse(false);
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
