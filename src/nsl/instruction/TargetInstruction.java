/*
 * TargetInstruction.java
 */

package nsl.instruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import nsl.*;
import nsl.expression.*;

/**
 * @author Jan
 */
public class TargetInstruction extends AssembleExpression {
	public static final String name = "Target";

	private static final Set<String> VALID_TARGETS =
			new HashSet<>(Arrays.asList("amd64-unicode", "x86-ansi", "x86-unicode"));

	private final Expression architecture;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public TargetInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 1) throw new NslArgumentException(name, 1);

		this.architecture = paramsList.get(0);
		if (!ExpressionType.isString(this.architecture))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		String targetValue = this.architecture.getStringValue();
		if (!VALID_TARGETS.contains(targetValue)) {
			throw new NslException(
					String.format(
							"%s: Invalid target architecture \"%s\". Valid values are: %s",
							name, targetValue, String.join(", ", VALID_TARGETS)),
					true);
		}
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.architecture);
		ScriptParser.writeLine(name + " " + this.architecture);
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
