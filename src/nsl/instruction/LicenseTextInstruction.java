/*
 * LicenseTextInstruction.java
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
public class LicenseTextInstruction extends AssembleExpression {
	public static final String name = "LicenseText";
	private final Expression text;
	private final Expression buttonText;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public LicenseTextInstruction(int returns) {
		if (!ScriptParser.inGlobalContext() && !PageExInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Global, NslContext.PageEx), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 1 || paramsCount > 2) throw new NslArgumentException(name, 1, 2);

		this.text = paramsList.get(0);

		if (paramsCount > 1) this.buttonText = paramsList.get(1);
		else this.buttonText = null;
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		Expression varOrText = AssembleExpression.getRegisterOrExpression(this.text);
		if (this.buttonText != null) {
			Expression varOrButtonText = AssembleExpression.getRegisterOrExpression(this.buttonText);
			ScriptParser.writeLine(name + " " + varOrText + " " + varOrButtonText);
			varOrButtonText.setInUse(false);
		} else {
			ScriptParser.writeLine(name + " " + varOrText);
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
