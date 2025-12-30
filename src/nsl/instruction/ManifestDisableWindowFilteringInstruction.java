/*
 * ManifestDisableWindowFilteringInstruction.java
 */

package nsl.instruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;

/**
 * @author Jan
 */
public class ManifestDisableWindowFilteringInstruction extends AssembleExpression {
	public static final String name = "ManifestDisableWindowFiltering";
	private final Expression value;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public ManifestDisableWindowFilteringInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() > 1) throw new NslArgumentException(name, 0, 1);

		if (paramsList.isEmpty()) {
			this.value = Expression.fromString("notset");
		} else {
			this.value = paramsList.get(0);
			if (!ExpressionType.isBoolean(this.value))
				throw new NslArgumentException(name, 1, ExpressionType.Boolean);

			// This is a bit of weird one: Only allows 'true', not 'false'
			if (!this.value.getBooleanValue()) {
				throw new NslException(
						String.format(
								"%s: Only 'true' is allowed as parameter. Use no parameter for 'notset'.", name),
						true);
			}
		}
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.value);
		ScriptParser.writeLine(name + " " + this.value);
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
