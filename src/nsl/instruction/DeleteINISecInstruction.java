/*
 * DeleteINISecInstruction.java
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
public class DeleteINISecInstruction extends AssembleExpression {
	public static final String name = "DeleteINISec";
	private final Expression iniFile;
	private final Expression sectionName;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public DeleteINISecInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 2) throw new NslArgumentException(name, 2);

		this.iniFile = paramsList.get(0);

		this.sectionName = paramsList.get(1);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		Expression varOrIniFile = AssembleExpression.getRegisterOrExpression(this.iniFile);
		Expression varOrSectionName = AssembleExpression.getRegisterOrExpression(this.sectionName);
		ScriptParser.writeLine(name + " " + varOrIniFile + " " + varOrSectionName);
		varOrIniFile.setInUse(false);
		varOrSectionName.setInUse(false);
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
