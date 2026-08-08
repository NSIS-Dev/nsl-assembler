/*
 * SetPluginUnloadInstruction.java
 */

package nsl.instruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nsl.*;
import nsl.expression.*;

/**
 * @author Jan
 */
public class SetPluginUnloadInstruction extends AssembleExpression {
	public static final String name = "SetPluginUnload";

	/**
	 * The two modes NSIS accepts. "makensis" calls the instruction deprecated - a plug-in is expected
	 * to manage its own unloading - but still compiles it.
	 */
	private static final List<String> validValues = Arrays.asList("manual", "alwaysoff");

	private final Expression value;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public SetPluginUnloadInstruction(int returns) {
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 1) throw new NslArgumentException(name, 1);

		this.value = paramsList.get(0);
		if (!ExpressionType.isString(this.value))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		// TODO: the only deprecated command nsL wraps. Lift into a general deprecation
		// warning, with a way to silence it, if a second one ever turns up.
		NslException.printWarning(
				name + " is deprecated. A plug-in is expected to handle its own unloading");

		String mode = this.value.getStringValue();
		if (!isValidValue(mode))
			throw new NslException(
					String.format(
							"%s: Invalid mode \"%s\" at parameter 1. Valid modes are: %s",
							name, mode, String.join(", ", validValues)),
					true);
	}

	private static boolean isValidValue(String value) {
		for (String validValue : validValues) if (validValue.equalsIgnoreCase(value)) return true;
		return false;
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
