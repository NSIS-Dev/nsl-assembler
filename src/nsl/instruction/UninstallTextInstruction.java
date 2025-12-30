/*
 * UninstallTextInstruction.java
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
public class UninstallTextInstruction extends AssembleExpression {
	public static final String name = "UninstallText";
	private final Expression text;
	private final Expression subText;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public UninstallTextInstruction(int returns) {
		if (!ScriptParser.inGlobalContext() && !PageExInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Global, NslContext.PageEx), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 1 || paramsCount > 2) throw new NslArgumentException(name, 1, 2);

		this.text = paramsList.get(0);

		if (paramsCount > 1) this.subText = paramsList.get(1);
		else this.subText = null;
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		Expression varOrText = AssembleExpression.getRegisterOrExpression(this.text);
		if (this.subText == null) {
			ScriptParser.writeLine(name + " " + varOrText);
		} else {
			Expression varOrSubText = AssembleExpression.getRegisterOrExpression(this.subText);
			ScriptParser.writeLine(name + " " + varOrText + " " + varOrSubText);
			varOrSubText.setInUse(false);
		}
		varOrText.setInUse(false);
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
