/*
 * DeleteINIStrInstruction.java
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
public class DeleteINIStrInstruction extends AssembleExpression {
	public static final String name = "DeleteINIStr";
	private final Expression iniFile;
	private final Expression sectionName;
	private final Expression valueName;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public DeleteINIStrInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 3) throw new NslArgumentException(name, 3);

		this.iniFile = paramsList.get(0);

		this.sectionName = paramsList.get(1);

		this.valueName = paramsList.get(2);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		Expression varOrIniFile = AssembleExpression.getRegisterOrExpression(this.iniFile);
		Expression varOrSectionName = AssembleExpression.getRegisterOrExpression(this.sectionName);
		Expression varOrValueName = AssembleExpression.getRegisterOrExpression(this.valueName);
		ScriptParser.writeLine(
				name + " " + varOrIniFile + " " + varOrSectionName + " " + varOrValueName);
		varOrIniFile.setInUse(false);
		varOrSectionName.setInUse(false);
		varOrValueName.setInUse(false);
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
