/*
 * LicenseForceSelectionInstruction.java
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
public class LicenseForceSelectionInstruction extends AssembleExpression {
	public static final String name = "LicenseForceSelection";
	private final Expression value;
	private final Expression acceptText;
	private final Expression declineText;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public LicenseForceSelectionInstruction(int returns) {
		if (!ScriptParser.inGlobalContext() && !PageExInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Global, NslContext.PageEx), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 1 || paramsCount > 3) throw new NslArgumentException(name, 1);

		this.value = paramsList.get(0);
		if (!ExpressionType.isString(this.value))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		if (paramsCount > 1) {
			this.acceptText = paramsList.get(1);
			if (!ExpressionType.isString(this.acceptText))
				throw new NslArgumentException(name, 2, ExpressionType.String);

			if (paramsCount > 2) {
				this.declineText = paramsList.get(2);
				if (!ExpressionType.isString(this.declineText))
					throw new NslArgumentException(name, 3, ExpressionType.String);
			} else {
				this.declineText = null;
			}
		} else {
			this.acceptText = null;
			this.declineText = null;
		}
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.value);
		String write = name + " " + this.value;

		if (this.acceptText != null) {
			AssembleExpression.assembleIfRequired(this.acceptText);
			write += " " + this.acceptText;

			if (this.declineText != null) {
				AssembleExpression.assembleIfRequired(this.declineText);
				write += " " + this.declineText;
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
