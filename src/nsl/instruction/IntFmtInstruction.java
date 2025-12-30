/*
 * IntFmtInstruction.java
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
public class IntFmtInstruction extends AssembleExpression {
	public static final String name = "IntFmt";
	private final Expression format;
	private final Expression number;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public IntFmtInstruction(int returns) {
		if (PageExInfo.in())
			throw new NslContextException(
					EnumSet.of(NslContext.Section, NslContext.Function, NslContext.Global), name);
		if (returns != 1) throw new NslReturnValueException(name, 1);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 2) throw new NslArgumentException(name, 2);

		this.format = paramsList.get(0);

		this.number = paramsList.get(1);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		throw new UnsupportedOperationException("Not supported.");
	}

	/**
	 * Assembles the source code.
	 *
	 * @param var the variable to assign the value to
	 */
	@Override
	public void assemble(Register var) throws IOException {
		Expression varOrFormat = AssembleExpression.getRegisterOrExpression(this.format);
		Expression varOrNumber = AssembleExpression.getRegisterOrExpression(this.number);
		ScriptParser.writeLine(name + " " + var + " " + varOrFormat + " " + varOrNumber);
		varOrFormat.setInUse(false);
		varOrNumber.setInUse(false);
	}
}
