/*
 * ManifestMaxVersionTestedInstruction.java
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
public class ManifestMaxVersionTestedInstruction extends AssembleExpression {
	public static final String name = "ManifestMaxVersionTested";
	private final Expression version;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public ManifestMaxVersionTestedInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 1) throw new NslArgumentException(name, 1);

		this.version = paramsList.get(0);
		if (!ExpressionType.isString(this.version))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		// "makensis" writes this value into the manifest without looking at it, so a
		// typo would only show up at run time, in a file nobody reads. Check it here.
		String versionValue = this.version.getStringValue();
		if (!isVersion(versionValue))
			throw new NslException(
					String.format(
							"%s: Invalid version \"%s\" at parameter 1. Expected one to four numbers"
									+ " separated by dots, as in \"maj.min.bld.rev\"",
							name, versionValue),
					true);
	}

	/**
	 * Determines whether the given value is one to four dot separated numbers.
	 *
	 * @param value the value to check
	 * @return whether the value is a version
	 */
	private static boolean isVersion(String value) {
		if (value == null || value.isEmpty()) return false;

		int parts = 1;
		int digits = 0;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c == '.') {
				if (digits == 0 || ++parts > 4) return false;
				digits = 0;
			} else if (c >= '0' && c <= '9') {
				digits++;
			} else return false;
		}

		return digits > 0;
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.version);
		ScriptParser.writeLine(name + " " + this.version);
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
