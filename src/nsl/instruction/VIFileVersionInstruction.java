/*
 * VIFileVersionInstruction.java
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
public class VIFileVersionInstruction extends AssembleExpression {
	public static final String name = "VIFileVersion";

	/** Each of the four fields of a version resource is a 16-bit number. */
	private static final int maxPart = 0xFFFF;

	private final Expression value;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public VIFileVersionInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 1) throw new NslArgumentException(name, 1);

		this.value = paramsList.get(0);
		if (!ExpressionType.isString(this.value))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		// "makensis" only counts the dots: it reads each field with atoi and takes
		// whatever comes out, so "1.0.0.0extra", "1.0.0.-1" and a fifth field all pass
		// it and end up in the version resource as something else.
		String versionValue = this.value.getStringValue();
		if (!isVersion(versionValue))
			throw new NslException(
					String.format(
							"%s: Invalid version \"%s\" at parameter 1. Expected four numbers separated by"
									+ " dots, as in \"maj.min.bld.rev\", each from 0 to %d",
							name, versionValue, maxPart),
					true);
	}

	private static boolean isVersion(String value) {
		String[] parts = value.split("\\.", -1);
		if (parts.length != 4) return false;

		for (String part : parts) {
			if (part.isEmpty() || part.length() > 5) return false;

			for (int i = 0; i < part.length(); i++) {
				char c = part.charAt(i);
				if (c < '0' || c > '9') return false;
			}

			if (Integer.parseInt(part) > maxPart) return false;
		}

		return true;
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
