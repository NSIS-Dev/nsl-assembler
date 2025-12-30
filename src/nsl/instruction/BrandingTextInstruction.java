/*
 * BrandingTextInstruction.java
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
public class BrandingTextInstruction extends AssembleExpression {
	public static final String name = "BrandingText";
	private final Expression text;
	private final Expression trim;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public BrandingTextInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 1 || paramsCount > 2) throw new NslArgumentException(name, 1, 2);

		this.text = paramsList.get(0);
		if (!ExpressionType.isString(this.text))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		if (paramsCount > 1) {
			this.trim = paramsList.get(1);
			if (!ExpressionType.isString(this.trim))
				throw new NslArgumentException(name, 2, ExpressionType.String);
		} else {
			this.trim = null;
		}
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.text);
		String write = name;

		if (this.trim != null) {
			AssembleExpression.assembleIfRequired(this.trim);
			write += " /TRIM" + this.trim.toString(true);
		}

		ScriptParser.writeLine(write + " " + this.text);
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
