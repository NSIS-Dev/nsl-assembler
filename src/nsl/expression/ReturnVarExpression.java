/*
 * MacroReturnVarExpression.java
 */

package nsl.expression;

import java.util.ArrayList;
import nsl.NslException;
import nsl.Register;

/**
 * Used to embed the returnvar(n) assembler function.
 *
 * @author Stuart
 */
public class ReturnVarExpression extends Expression {
	private static ArrayList<Register> current = null;

	/**
	 * Sets the current register list.
	 *
	 * @param registers the register list
	 * @return the parent/previous register list
	 */
	public static ArrayList<Register> setRegisters(ArrayList<Register> registers) {
		ArrayList<Register> parent = current;
		current = registers;
		return parent;
	}

	/**
	 * Sets the current register list.
	 *
	 * @param registers the register list
	 * @return the parent/previous register list
	 */
	public static ArrayList<Register> setRegisters(Register registers) {
		ArrayList<Register> parent = current;
		current = new ArrayList<Register>();
		current.add(registers);
		return parent;
	}

	/**
	 * Gets the current register list.
	 *
	 * @return the current register list
	 */
	public static ArrayList<Register> getRegisters() {
		return current;
	}

	/**
	 * Class constructor.
	 *
	 * @param registerNumber the register number from the current list
	 */
	public ReturnVarExpression(int registerNumber) {
		this.integerValue = registerNumber;
	}

	/**
	 * Returns a string representation of the current object.
	 *
	 * @return a string representation of the current object
	 */
	@Override
	public String toString() {
		return this.toString(false);
	}

	/**
	 * Returns a string representation of the current object.
	 *
	 * @param noQuote do not quote the value if it is a string
	 * @return a string representation of the current object
	 */
	@Override
	public String toString(boolean noQuote) {
		if (current == null || current.isEmpty())
			throw new NslException(
					"Use of \"returnvar()\" where no return registers are being used", false);

		int registerCount = current.size();
		if (this.integerValue < 1 || this.integerValue > registerCount)
			throw new NslException(
					"A value of "
							+ this.integerValue
							+ " is out of range for \"returnvar()\" where "
							+ registerCount
							+ (registerCount == 1 ? " register is" : " registers are")
							+ " in use",
					false);

		return current.get(this.integerValue - 1).toString(noQuote);
	}
}
