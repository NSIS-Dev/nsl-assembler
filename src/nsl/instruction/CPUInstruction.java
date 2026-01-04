/*
 * CPUInstruction.java
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
public class CPUInstruction extends AssembleExpression {
	public static final String name = "CPU";

	private static final Set<String> VALID_CPUS = new HashSet<>(Arrays.asList("x86", "amd64"));

	private final Expression cpu;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public CPUInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 1) throw new NslArgumentException(name, 1);

		this.cpu = paramsList.get(0);
		if (!ExpressionType.isString(this.cpu))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		String cpuValue = this.cpu.getStringValue();
		if (!VALID_CPUS.contains(cpuValue)) {
			throw new NslException(
					String.format(
							"%s: Invalid CPU architecture \"%s\". Valid values are: %s",
							name, cpuValue, String.join(", ", VALID_CPUS)),
					true);
		}
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.cpu);
		ScriptParser.writeLine(name + " " + this.cpu);
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
