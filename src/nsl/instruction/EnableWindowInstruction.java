/*
 * EnableWindowInstruction.java
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
public class EnableWindowInstruction extends AssembleExpression {
	public static final String name = "EnableWindow";
	private final Expression hWnd;
	private final Expression enabled;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public EnableWindowInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 2) throw new NslArgumentException(name, 2);

		this.hWnd = paramsList.get(0);

		this.enabled = paramsList.get(1);
		if (!ExpressionType.isInteger(this.enabled))
			throw new NslArgumentException(name, 2, ExpressionType.Integer);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		Expression varOrHWnd = AssembleExpression.getRegisterOrExpression(this.hWnd);
		AssembleExpression.assembleIfRequired(this.enabled);
		ScriptParser.writeLine(name + " " + varOrHWnd + " " + this.enabled);
		varOrHWnd.setInUse(false);
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
