/*
 * PEAddResourceInstruction.java
 */

package nsl.instruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import nsl.*;
import nsl.expression.*;

/**
 * @author Jan
 */
public class PEAddResourceInstruction extends AssembleExpression {
	public static final String name = "PEAddResource";

	private static final List<String> validModes = Arrays.asList("/OVERWRITE", "/REPLACE");

	private final Expression file;
	private final Expression resourceType;
	private final Expression resourceName;
	private Expression resourceLanguage;
	private Expression mode;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public PEAddResourceInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 3 || paramsCount > 5) throw new NslArgumentException(name, 3, 5);

		this.file = paramsList.get(0);
		if (!ExpressionType.isString(this.file))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		// A resource type or name is either an integer id or a string name, so
		// neither can be checked beyond being one of the two.
		this.resourceType = paramsList.get(1);
		this.resourceName = paramsList.get(2);

		// Both remaining parameters are optional, and the mode is told apart from a
		// language id by looking like a switch. A language id never starts with a
		// slash, so the two cannot be confused.
		for (int i = 3; i < paramsCount; i++) {
			Expression expr = paramsList.get(i);
			if (ExpressionType.isString(expr) && expr.getStringValue().startsWith("/")) {
				if (this.mode != null)
					throw new NslException(
							String.format(
									"%s: Only one of %s may be given", name, String.join(" or ", validModes)),
							true);

				String modeValue = expr.getStringValue();
				if (!validModes.contains(modeValue.toUpperCase()))
					throw new NslException(
							String.format(
									"%s: Invalid switch \"%s\" at parameter %d. Valid switches are: %s",
									name, modeValue, i + 1, String.join(", ", validModes)),
							true);

				this.mode = expr;
			} else {
				if (this.resourceLanguage != null) throw new NslArgumentException(name, 3, 5);
				this.resourceLanguage = expr;
			}
		}
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		String write = name + " ";

		if (this.mode != null) {
			AssembleExpression.assembleIfRequired(this.mode);
			write += this.mode.getStringValue().toUpperCase() + " ";
		}

		AssembleExpression.assembleIfRequired(this.file);
		AssembleExpression.assembleIfRequired(this.resourceType);
		AssembleExpression.assembleIfRequired(this.resourceName);
		write += this.file + " " + this.resourceType + " " + this.resourceName;

		if (this.resourceLanguage != null) {
			AssembleExpression.assembleIfRequired(this.resourceLanguage);
			write += " " + this.resourceLanguage;
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
