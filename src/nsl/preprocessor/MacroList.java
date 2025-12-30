/*
 * MacroList.java
 */

package nsl.preprocessor;

import java.util.ArrayList;

/**
 * Lists defined macros.
 *
 * @author Stuart
 */
public class MacroList {
	private final ArrayList<Macro> macros;

	private static final MacroList current = new MacroList();

	/**
	 * Gets the current macro list.
	 *
	 * @return the current macro list
	 */
	public static MacroList getCurrent() {
		return current;
	}

	/** Class constructor. */
	public MacroList() {
		this.macros = new ArrayList<Macro>();
	}

	/**
	 * Gets the macro with the given name and parameter count.
	 *
	 * @param name the macro name
	 * @param paramCount the parameter count
	 * @return the found macro on success or <code>null</code> otherwise
	 */
	public Macro get(String name, int paramCount) {
		for (Macro macro : this.macros)
			if (macro.getName().equals(name) && macro.getParamCount() == paramCount) return macro;
		return null;
	}

	/**
	 * Gets the macros with the given name.
	 *
	 * @param name the macro name
	 * @return a list of matching macros
	 */
	public ArrayList<Macro> get(String name) {
		ArrayList<Macro> macrosList = new ArrayList<Macro>();
		for (Macro macro : this.macros) if (macro.getName().equals(name)) macrosList.add(macro);
		return macrosList;
	}

	/**
	 * Adds the macro.
	 *
	 * @param macro the macro to add
	 * @return <code>false</code> if the macro could not be added due to one existing with the same
	 *     name and parameter count
	 */
	public boolean add(Macro macro) {
		Macro existing = this.get(macro.getName(), macro.getParamCount());
		if (existing == null) {
			this.macros.add(macro);
			return true;
		}
		return false;
	}
}
