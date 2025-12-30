/*
 * ShowWindowInstruction.java
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
public class ShowWindowInstruction extends AssembleExpression {
	public static final String name = "ShowWindow";
	private final Expression hWnd;
	private final Expression value;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public ShowWindowInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 2) throw new NslArgumentException(name, 2);

		this.hWnd = paramsList.get(0);

		this.value = paramsList.get(1);
		if (!ExpressionType.isInteger(this.value))
			throw new NslArgumentException(name, 2, ExpressionType.Integer);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		Expression varOrHWnd = AssembleExpression.getRegisterOrExpression(this.hWnd);
		AssembleExpression.assembleIfRequired(this.value);
		ScriptParser.writeLine(name + " " + varOrHWnd + " " + this.value);
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
