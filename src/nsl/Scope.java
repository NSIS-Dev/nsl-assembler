/*
 * Scope.java
 */

package nsl;

import java.util.ArrayList;

/**
 * Defines a variable scope level.
 *
 * @author Stuart
 */
public class Scope {
	private static final Scope global = new Scope();
	private static final Scope globalUninstaller = new Scope();
	private static Scope current = global;
	private static boolean inUninstaller;

	private final Scope parent;
	private final ArrayList<Integer> registersList;

	/** Class constructor. */
	private Scope() {
		this.parent = current;
		this.registersList = new ArrayList<Integer>();
	}

	/**
	 * Creates a new scope and sets it as current.
	 *
	 * @return the new scope
	 */
	public static Scope create() {
		return (current = new Scope());
	}

	/**
	 * Gets the current scope.
	 *
	 * @return the current scope
	 */
	public static Scope getCurrent() {
		return current;
	}

	/**
	 * Gets the global scope.
	 *
	 * @return the global scope
	 */
	public static Scope getGlobal() {
		return global;
	}

	/**
	 * Gets the uninstaller global scope.
	 *
	 * @return the uninstaller global scope
	 */
	public static Scope getGlobalUninstaller() {
		return globalUninstaller;
	}

	/**
	 * Sets whether or not we are currently in the uninstaller global scope or the installer global
	 * scope.
	 *
	 * @param in {@code true} if we are in the uninstaller global scope
	 */
	public static void setInUninstaller(boolean in) {
		inUninstaller = in;
		if (in) current = globalUninstaller;
		else current = global;
	}

	/**
	 * Gets whether or not the current scope is in the uninstaller.
	 *
	 * @return whether or not the current scope is in the uninstaller
	 */
	public static boolean inUninstaller() {
		return inUninstaller;
	}

	/** Must be called when the scope ends. */
	public void end() {
		current = this.parent;
	}

	/**
	 * Adds a register to the scope.
	 *
	 * @param registerIndex the index of the register to check
	 */
	public void addVar(int registerIndex) {
		if (!this.registerExists(registerIndex)) this.registersList.add(registerIndex);
	}

	/**
	 * Checks that a register is defined in the scope.
	 *
	 * @param registerIndex the index of the register to check
	 * @return <code>true</code> if the variable is defined in the scope
	 */
	private boolean registerExists(int registerIndex) {
		return this.registersList.contains(registerIndex)
				|| (this.parent != null && this.parent.registerExists(registerIndex));
	}

	/**
	 * Checks the scope of the given register/variable.
	 *
	 * @param registerIndex the index of the register to check
	 */
	public void check(int registerIndex) {
		if ((FunctionInfo.in() || SectionInfo.in()) && !this.registerExists(registerIndex))
			throw new NslException(
					"Variable "
							+ RegisterList.getCurrent().get(registerIndex)
							+ " may not have been initialised",
					true);
	}
}
