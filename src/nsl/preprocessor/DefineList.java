/*
 * DefineList.java
 */

package nsl.preprocessor;

import java.util.HashMap;
import java.util.Set;
import nsl.expression.Expression;

/**
 * Lists defined constants.
 *
 * @author Stuart
 */
public class DefineList {
	private final HashMap<String, Expression> constants;
	private int count;

	private static DefineList current = new DefineList();

	/**
	 * Gets the current {@link DefineList}.
	 *
	 * @return the current {@link DefineList}
	 */
	public static DefineList getCurrent() {
		return current;
	}

	/** Class constructor. */
	public DefineList() {
		this.constants = new HashMap<String, Expression>();
		this.count = 0;
	}

	/**
	 * Gets the number of defined constants.
	 *
	 * @return the number of defined constants
	 */
	public int getCount() {
		return this.count;
	}

	/**
	 * Looks up a constant.
	 *
	 * @param name the constant name
	 * @return the value on success or <code>null</code> otherwise
	 */
	public Expression get(String name) {
		return this.constants.get(name);
	}

	/**
	 * Gets the names of the defined constants.
	 *
	 * @return the names of the defined constants
	 */
	public Set<String> getNames() {
		return this.constants.keySet();
	}

	/**
	 * Adds a new constant.
	 *
	 * @param name the constant name
	 * @param value the constant value
	 * @return <code>true</code> if the constant was added without replacing one already listed
	 */
	public boolean add(String name, Expression value) {
		Expression oldValue = this.constants.put(name, value);
		if (oldValue == null) {
			this.count++;
			return true;
		}
		return false;
	}

	/**
	 * Removes an existing constant.
	 *
	 * @param name the constant name
	 * @return <code>true</code> if the constant was removed
	 */
	public boolean remove(String name) {
		return this.constants.remove(name) != null;
	}

	/**
	 * Looks up the given constant in both the global constants list and current macro constants.
	 *
	 * @param name the constant name
	 * @return the value on success or <code>null</code> otherwise
	 */
	public static Expression lookup(String name) {
		Expression value = null;
		if (MacroEvaluated.getCurrent() != null)
			value = MacroEvaluated.getCurrent().getDefineList().get(name);
		if (value == null) value = DefineList.getCurrent().get(name);
		return value;
	}
}
