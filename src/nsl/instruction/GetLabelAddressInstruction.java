/*
 * GetLabelAddressInstruction.java
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
public class GetLabelAddressInstruction extends AssembleExpression {
	public static final String name = "GetLabelAddress";
	private final Expression label;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public GetLabelAddressInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns != 1) throw new NslReturnValueException(name, 1);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 1) throw new NslArgumentException(name, 1);

		this.label = paramsList.get(0);
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
		Expression varOrLabel = AssembleExpression.getRegisterOrExpression(this.label);
		ScriptParser.writeLine(name + " " + var + " " + varOrLabel);
		varOrLabel.setInUse(false);
	}
}
