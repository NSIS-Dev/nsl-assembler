/*
 * SectionSetFlagsInstruction.java
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
public class SectionSetFlagsInstruction extends AssembleExpression {
	public static final String name = "SectionSetFlags";
	private final Expression index;
	private final Expression flags;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public SectionSetFlagsInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 2) throw new NslArgumentException(name, 2);

		this.index = paramsList.get(0);

		this.flags = paramsList.get(1);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		Expression varOrIndex = AssembleExpression.getRegisterOrExpression(this.index);
		Expression varOrFlags = AssembleExpression.getRegisterOrExpression(this.flags);
		ScriptParser.writeLine(name + " " + varOrIndex + " " + varOrFlags);
		varOrIndex.setInUse(false);
		varOrFlags.setInUse(false);
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
