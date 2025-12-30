/*
 * BGGradientInstruction.java
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
public class BGGradientInstruction extends AssembleExpression {
	public static final String name = "BGGradient";
	private final Expression topC;
	private final Expression botC;
	private final Expression textColor;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public BGGradientInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 1 || paramsCount > 3) throw new NslArgumentException(name, 1, 3);

		this.topC = paramsList.get(0);
		if (!ExpressionType.isString(this.topC))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		if (paramsCount > 1) {
			this.botC = paramsList.get(1);
			if (!ExpressionType.isString(this.botC))
				throw new NslArgumentException(name, 2, ExpressionType.String);

			if (paramsCount > 2) {
				this.textColor = paramsList.get(2);
				if (!ExpressionType.isString(this.textColor))
					throw new NslArgumentException(name, 3, ExpressionType.String);
			} else {
				this.textColor = null;
			}
		} else {
			this.botC = null;
			this.textColor = null;
		}
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.topC);
		String write = name + " " + this.topC;

		if (this.botC != null) {
			AssembleExpression.assembleIfRequired(this.botC);
			write += " " + this.botC;

			if (this.textColor != null) {
				AssembleExpression.assembleIfRequired(this.textColor);
				write += " " + this.textColor;
			}
		}

		ScriptParser.writeLine(write);
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
