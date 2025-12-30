/*
 * Register.java
 */

package nsl;

import nsl.expression.Expression;
import nsl.expression.ExpressionType;

/**
 * An NSIS register.
 *
 * @author Stuart
 */
public class Register extends Expression {
	private RegisterType registerType;
	private String substitute;

	/**
	 * Class constructor.
	 *
	 * @param name the register name
	 * @param index the register index
	 * @param registerType the type of register
	 * @param inUse whether or not the current register is in use
	 */
	public Register(String name, int index, RegisterType registerType, boolean inUse) {
		this.type = ExpressionType.Register;
		this.stringValue = name;
		this.integerValue = index;
		this.registerType = registerType;
		this.booleanValue = inUse;
		this.substitute = null;
	}

	/**
	 * Gets the register index.
	 *
	 * @return the register index
	 */
	public int getIndex() {
		return this.integerValue;
	}

	/**
	 * Gets whether or not the current register is in use.
	 *
	 * @return whether or not the current register is in use
	 */
	public boolean getInUse() {
		return this.booleanValue;
	}

	/**
	 * Sets whether or not the current register is in use.
	 *
	 * @param inUse whether or not the current register is in use
	 */
	@Override
	public void setInUse(boolean inUse) {
		this.booleanValue = inUse;
	}

	/**
	 * Gets the type of register.
	 *
	 * @return the type of register
	 */
	public RegisterType getRegisterType() {
		return this.registerType;
	}

	/**
	 * Substitutes the register with the given value.
	 *
	 * @param value the value to substitute
	 */
	public void substitute(String value) {
		this.substitute = value;
		this.booleanValue = false;
	}

	/**
	 * Gets a string representation of the current object.
	 *
	 * @param noQuote do not quote the value if it is a string
	 * @return a string representation of the current object
	 */
	@Override
	public String toString() {
		return this.toString(false);
	}

	/**
	 * Gets a string representation of the current object.
	 *
	 * @return a string representation of the current object
	 */
	@Override
	public String toString(boolean noQuote) {
		if (this.substitute != null) {
			String value = this.substitute;
			this.substitute = null;
			if (noQuote) return value;
			return "\"" + value + "\"";
		}

		if (CodeInfo.getCurrent() != null) CodeInfo.getCurrent().addUsedVar(this);

		return this.stringValue;
	}
}
