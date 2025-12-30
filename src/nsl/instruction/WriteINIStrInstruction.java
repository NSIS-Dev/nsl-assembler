/*
 * WriteINIStrInstruction.java
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
public class WriteINIStrInstruction extends AssembleExpression {
	public static final String name = "WriteINIStr";
	private final Expression iniFile;
	private final Expression sectionName;
	private final Expression valueName;
	private final Expression value;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public WriteINIStrInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 4) throw new NslArgumentException(name, 4);

		this.iniFile = paramsList.get(0);

		this.sectionName = paramsList.get(1);

		this.valueName = paramsList.get(2);

		this.value = paramsList.get(3);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		Expression varOrIniFile = AssembleExpression.getRegisterOrExpression(this.iniFile);
		Expression varOrSectionName = AssembleExpression.getRegisterOrExpression(this.sectionName);
		Expression varOrValueName = AssembleExpression.getRegisterOrExpression(this.valueName);
		Expression varOrValue = AssembleExpression.getRegisterOrExpression(this.value);
		ScriptParser.writeLine(
				name
						+ " "
						+ varOrIniFile
						+ " "
						+ varOrSectionName
						+ " "
						+ varOrValueName
						+ " "
						+ varOrValue);
		varOrIniFile.setInUse(false);
		varOrSectionName.setInUse(false);
		varOrValueName.setInUse(false);
		varOrValue.setInUse(false);
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
