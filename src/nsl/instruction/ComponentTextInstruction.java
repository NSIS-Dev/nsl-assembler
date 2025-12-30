/*
 * ComponentTextInstruction.java
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
public class ComponentTextInstruction extends AssembleExpression {
	public static final String name = "ComponentText";
	private final Expression text;
	private final Expression subText;
	private final Expression subText2;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public ComponentTextInstruction(int returns) {
		if (!ScriptParser.inGlobalContext() && !PageExInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Global, NslContext.PageEx), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 1 || paramsCount > 3) throw new NslArgumentException(name, 1, 3);

		this.text = paramsList.get(0);

		if (paramsCount > 1) {
			this.subText = paramsList.get(1);

			if (paramsCount > 2) {
				this.subText2 = paramsList.get(2);
			} else {
				this.subText2 = null;
			}
		} else {
			this.subText = null;
			this.subText2 = null;
		}
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		Expression varOrText = AssembleExpression.getRegisterOrExpression(this.text);
		String write = name + " " + varOrText;

		if (this.subText != null) {
			Expression varOrSubText = AssembleExpression.getRegisterOrExpression(this.subText);
			write += " " + varOrSubText;

			if (this.subText2 != null) {
				Expression varOrSubText2 = AssembleExpression.getRegisterOrExpression(this.subText2);
				write += " " + varOrSubText2;
				varOrSubText2.setInUse(false);
			}

			varOrSubText.setInUse(false);
		}

		ScriptParser.writeLine(write);
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
