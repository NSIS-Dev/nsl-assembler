/*
 * ManifestDPIAwarenessInstruction.java
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
public class ManifestDPIAwarenessInstruction extends AssembleExpression {
	public static final String name = "ManifestDPIAwareness";

	/**
	 * The values Windows understands in the manifest's dpiAwareness element.
	 *
	 * @see https://learn.microsoft.com/en-us/windows/win32/sbscs/application-manifests#dpiAwareness
	 */
	private static final List<String> validValues =
			Arrays.asList("unaware", "system", "PerMonitor", "PerMonitorV2");

	private final Expression value;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public ManifestDPIAwarenessInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.isEmpty()) throw new NslArgumentException(name, 1, Integer.MAX_VALUE);

		// NSIS takes a single comma separated string, but the list is really a
		// sequence of fallbacks, so nsL also allows one value per parameter. Both
		// spellings end up as the same operand.
		StringBuilder joined = new StringBuilder();
		for (int i = 0; i < paramsList.size(); i++) {
			Expression expr = paramsList.get(i);
			if (!ExpressionType.isString(expr))
				throw new NslArgumentException(name, i + 1, ExpressionType.String);

			for (String part : expr.getStringValue().split(",", -1)) {
				part = part.trim();
				if (!isValidValue(part))
					throw new NslException(
							String.format(
									"%s: Invalid DPI awareness \"%s\" at parameter %d. Valid values are: %s",
									name, part, i + 1, String.join(", ", validValues)),
							true);

				if (joined.length() > 0) joined.append(",");
				joined.append(part);
			}
		}

		this.value = Expression.fromString(joined.toString());
	}

	/**
	 * Determines whether the given value names a DPI awareness mode, ignoring case as Windows does
	 * when it reads the manifest.
	 *
	 * @param value the value to check
	 * @return whether the value is valid
	 */
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
