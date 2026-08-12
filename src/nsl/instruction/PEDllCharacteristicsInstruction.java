/*
 * PEDllCharacteristicsInstruction.java
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
public class PEDllCharacteristicsInstruction extends AssembleExpression {
	public static final String name = "PEDllCharacteristics";

	/** The PE header's DllCharacteristics is a 16-bit field. */
	private static final int maxBits = 0xFFFF;

	private final Expression addBits;
	private final Expression removeBits;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public PEDllCharacteristicsInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 2) throw new NslArgumentException(name, 2);

		this.addBits = paramsList.get(0);
		if (!ExpressionType.isInteger(this.addBits))
			throw new NslArgumentException(name, 1, ExpressionType.Integer);

		this.removeBits = paramsList.get(1);
		if (!ExpressionType.isInteger(this.removeBits))
			throw new NslArgumentException(name, 2, ExpressionType.Integer);

		// "makensis" takes anything that parses as a number, so a value too wide for
		// the field is silently truncated rather than reported.
		int addValue = this.addBits.getIntegerValue();
		if (addValue < 0 || addValue > maxBits) throw newRangeException(addValue, 1);

		int removeValue = this.removeBits.getIntegerValue();
		if (removeValue < 0 || removeValue > maxBits) throw newRangeException(removeValue, 2);

		// The bits are applied as (characteristics | add) & ~remove, so a bit named by
		// both is always removed and the addition of it is dead.
		int both = addValue & removeValue;
		if (both != 0)
			throw new NslException(
					String.format(
							"%s: Bits 0x%04X are both added and removed. A bit named by both parameters is"
									+ " always removed",
							name, both),
					true);
	}

	private static NslException newRangeException(int value, int param) {
		// A negative reads as 0xFFFFFFFF in hexadecimal, which hides what was written.
		return new NslException(
				String.format(
						"%s: Invalid DLL characteristics %s at parameter %d. Valid range is 0x0 to 0x%X",
						name, value < 0 ? String.valueOf(value) : String.format("0x%X", value), param, maxBits),
				true);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.addBits);
		AssembleExpression.assembleIfRequired(this.removeBits);
		ScriptParser.writeLine(name + " " + this.addBits + " " + this.removeBits);
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
