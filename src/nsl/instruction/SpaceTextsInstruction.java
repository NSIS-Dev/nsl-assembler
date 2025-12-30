/*
 * SpaceTextsInstruction.java
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
public class SpaceTextsInstruction extends AssembleExpression {
	public static final String name = "SpaceTexts";
	private final Expression reqText;
	private final Expression availText;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public SpaceTextsInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 1 || paramsCount > 2) throw new NslArgumentException(name, 1, 2);

		this.reqText = paramsList.get(0);

		if (paramsCount > 1) this.availText = paramsList.get(1);
		else this.availText = null;
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		Expression varOrReqText = AssembleExpression.getRegisterOrExpression(this.reqText);
		if (this.availText == null) {
			ScriptParser.writeLine(name + " " + varOrReqText);
		} else {
			Expression varOrAvailText = AssembleExpression.getRegisterOrExpression(this.availText);
			ScriptParser.writeLine(name + " " + varOrReqText + " " + varOrAvailText);
			varOrAvailText.setInUse(false);
		}
		varOrReqText.setInUse(false);
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
