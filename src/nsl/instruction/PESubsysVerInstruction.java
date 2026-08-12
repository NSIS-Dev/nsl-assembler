/*
 * PESubsysVerInstruction.java
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
public class PESubsysVerInstruction extends AssembleExpression {
	public static final String name = "PESubsysVer";

	/** Both halves of the PE header's subsystem version are 16-bit fields. */
	private static final int maxPart = 0xFFFF;

	private final Expression version;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public PESubsysVerInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 1 || paramsCount > 2) throw new NslArgumentException(name, 1, 2);

		// NSIS takes one "major.minor" token, which nsL cannot write unquoted since it
		// has no decimal literal. The two halves are therefore also accepted as two
		// integers, which spares the caller quoting a number.
		if (paramsCount == 2) {
			int major = matchPart(paramsList.get(0), 1);
			int minor = matchPart(paramsList.get(1), 2);
			this.version = Expression.fromString(major + "." + minor);
			return;
		}

		Expression expr = paramsList.get(0);
		if (!ExpressionType.isString(expr))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		// "makensis" reads the two numbers and ignores whatever follows them, so it
		// takes "5.0.1" and "5.0extra" without a word. Insist on the documented form.
		String value = expr.getStringValue();
		int dot = value.indexOf('.');
		if (dot < 0 || !isPart(value.substring(0, dot)) || !isPart(value.substring(dot + 1)))
			throw new NslException(
					String.format(
							"%s: Invalid version \"%s\" at parameter 1. Expected two numbers separated by a"
									+ " dot, as in \"maj.min\", each from 0 to %d",
							name, value, maxPart),
					true);

		this.version = expr;
	}

	private static int matchPart(Expression expr, int param) {
		if (!ExpressionType.isInteger(expr))
			throw new NslArgumentException(name, param, ExpressionType.Integer);

		int value = expr.getIntegerValue();
		if (value < 0 || value > maxPart)
			throw new NslException(
					String.format(
							"%s: Invalid version part %d at parameter %d. Valid range is 0 to %d",
							name, value, param, maxPart),
					true);

		return value;
	}

	private static boolean isPart(String value) {
		if (value.isEmpty() || value.length() > 5) return false;

		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c < '0' || c > '9') return false;
		}

		return Integer.parseInt(value) <= maxPart;
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.version);
		ScriptParser.writeLine(name + " " + this.version.getStringValue());
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
