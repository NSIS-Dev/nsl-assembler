/*
 * SetCurInstTypeInstruction.java
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
public class SetCurInstTypeInstruction extends AssembleExpression {
	public static final String name = "SetCurInstType";
	private final Expression instType;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public SetCurInstTypeInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 1) throw new NslArgumentException(name, 1);

		this.instType = paramsList.get(0);
		if (!ExpressionType.isInteger(this.instType))
			throw new NslArgumentException(name, 1, ExpressionType.Integer);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.instType);
		ScriptParser.writeLine(name + " " + this.instType);
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
