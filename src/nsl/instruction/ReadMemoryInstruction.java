/*
 * ReadMemoryInstruction.java
 */

package nsl.instruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;

/**
 * @author Jan
 */
public class ReadMemoryInstruction extends AssembleExpression {
	public static final String name = "ReadMemory";
	private final Expression address;
	private final Expression size;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public ReadMemoryInstruction(int returns) {
		if (PageExInfo.in())
			throw new NslContextException(
					EnumSet.of(NslContext.Section, NslContext.Function, NslContext.Global), name);
		if (returns != 1) throw new NslReturnValueException(name, 1);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 2) throw new NslArgumentException(name, 2);

		this.address = paramsList.get(0);

		this.size = paramsList.get(1);
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
		Expression varOrAddress = AssembleExpression.getRegisterOrExpression(this.address);
		Expression varOrSize = AssembleExpression.getRegisterOrExpression(this.size);
		ScriptParser.writeLine(name + " " + var + " " + varOrAddress + " " + varOrSize);
		varOrAddress.setInUse(false);
		varOrSize.setInUse(false);
	}
}
