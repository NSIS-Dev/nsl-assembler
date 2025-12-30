/*
 * GetDlgItemInstruction.java
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
public class GetDlgItemInstruction extends AssembleExpression {
	public static final String name = "GetDlgItem";
	private final Expression hWnd;
	private final Expression itemId;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public GetDlgItemInstruction(int returns) {
		if (PageExInfo.in())
			throw new NslContextException(
					EnumSet.of(NslContext.Section, NslContext.Function, NslContext.Global), name);
		if (returns != 1) throw new NslReturnValueException(name, 1);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 2) throw new NslArgumentException(name, 2);

		this.hWnd = paramsList.get(0);

		this.itemId = paramsList.get(1);
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
		Expression varOrHWnd = AssembleExpression.getRegisterOrExpression(this.hWnd);
		Expression varOrItemId = AssembleExpression.getRegisterOrExpression(this.itemId);
		ScriptParser.writeLine(name + " " + var + " " + varOrHWnd + " " + varOrItemId);
		varOrHWnd.setInUse(false);
	}
}
