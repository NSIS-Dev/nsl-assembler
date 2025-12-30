/*
 * CodeInfo.java
 */

package nsl;

import java.util.HashMap;

/**
 * @author Stuart
 */
public abstract class CodeInfo {
	protected static CodeInfo current = null;

	protected final HashMap<Integer, Register> usedVars;

	private Label labelBreak;
	private Label labelContinue;

	protected CodeInfo() {
		this.usedVars = new HashMap<Integer, Register>();
		this.labelBreak = null;
		this.labelContinue = null;
	}

	/**
	 * Gets the variables used in the function.
	 *
	 * @return the variables used in the function
	 */
	public HashMap<Integer, Register> getUsedVars() {
		return this.usedVars;
	}

	/**
	 * Gets the current go-to label for a "break" instruction.
	 *
	 * @return the current go-to label for a "break" instruction
	 */
	public Label getBreakLabel() {
		return this.labelBreak;
	}

	/**
	 * Sets the current go-to label for a "break" instruction.
	 *
	 * @param labelBreak the current go-to label for a "break" instruction
	 * @return the original value
	 */
	public Label setBreakLabel(Label labelBreak) {
		Label old = this.labelBreak;
		this.labelBreak = labelBreak;
		return old;
	}

	/**
	 * Gets the current go-to label for a "continue" instruction.
	 *
	 * @return the current go-to label for a "continue" instruction
	 */
	public Label getContinueLabel() {
		return this.labelContinue;
	}

	/**
	 * Sets the current go-to label for a "continue" instruction.
	 *
	 * @param labelBreak the current go-to label for a "continue" instruction
	 * @return the original value
	 */
	public Label setContinueLabel(Label labelContinue) {
		Label old = this.labelContinue;
		this.labelContinue = labelContinue;
		return old;
	}

	/**
	 * Adds a variable to the used variables list.
	 *
	 * @param var the variable to add
	 */
	public void addUsedVar(Register var) {
		Integer key = Integer.valueOf(var.getIntegerValue());
		if (this.usedVars.get(key) == null) this.usedVars.put(key, var);
	}

	/**
	 * Gets the current code info.
	 *
	 * @return the current code info
	 */
	public static CodeInfo getCurrent() {
		return current;
	}

	/**
	 * Sets the current code info.
	 *
	 * @param codeInfo the current code info
	 */
	public static void setCurrent(CodeInfo codeInfo) {
		current = codeInfo;
	}
}
