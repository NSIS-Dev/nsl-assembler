/*
 * ConstantList.java
 */

package nsl;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Manages a list of NSIS constants.
 *
 * @author Stuart
 */
public class ConstantList {
	private final ArrayList<Constant> constantList;
	private final HashMap<String, Constant> constantMap;

	private static ConstantList current = new ConstantList();

	/** Class constructor. */
	public ConstantList() {
		this.constantList = new ArrayList<Constant>();
		this.constantMap = new HashMap<String, Constant>();

		this.add("$PROGRAMFILES", null);
		this.add("$PROGRAMFILES32", null);
		this.add("$PROGRAMFILES64", null);
		this.add("$COMMONFILES", null);
		this.add("$COMMONFILES32", null);
		this.add("$COMMONFILES64", null);
		this.add("$DESKTOP", null);
		this.add("$EXEDIR", null);
		this.add("$EXEFILE", null);
		this.add("$EXEPATH", null);
		this.add("$NSISDIR", "${NSISDIR}");
		this.add("$WINDIR", null);
		this.add("$SYSDIR", null);
		this.add("$TEMP", null);
		this.add("$STARTMENU", null);
		this.add("$SMPROGRAMS", null);
		this.add("$SMSTARTUP", null);
		this.add("$QUICKLAUNCH", null);
		this.add("$DOCUMENTS", null);
		this.add("$SENDTO", null);
		this.add("$RECENT", null);
		this.add("$FAVORITES", null);
		this.add("$MUSIC", null);
		this.add("$PICTURES", null);
		this.add("$VIDEOS", null);
		this.add("$NETHOOD", null);
		this.add("$FONTS", null);
		this.add("$TEMPLATES", null);
		this.add("$APPDATA", null);
		this.add("$LOCALAPPDATA", null);
		this.add("$PRINTHOOD", null);
		this.add("$INTERNET_CACHE", null);
		this.add("$COOKIES", null);
		this.add("$HISTORY", null);
		this.add("$PROFILE", null);
		this.add("$ADMINTOOLS", null);
		this.add("$RESOURCES", null);
		this.add("$RESOURCES_LOCALIZED", null);
		this.add("$CDBURN_AREA", null);
		this.add("$HWNDPARENT", null);
		this.add("$PLUGINSDIR", null);
	}

	/**
	 * Adds a constant to the list.
	 *
	 * @param name the constant name
	 * @param realName the real constant name if it differs
	 */
	private void add(String name, String realName) {
		Constant constant = new Constant(name, realName, this.constantList.size());
		this.constantList.add(constant);
		this.constantMap.put(name, constant);
	}

	/**
	 * Looks up a constant.
	 *
	 * @param name the constant name
	 * @return the constant index or -1 if the constant is not found (assume a register or variable
	 *     instead)
	 */
	public int lookup(String name) {
		Constant constant = this.constantMap.get(name);
		if (constant != null) return constant.getIndex();
		return -1;
	}

	/**
	 * Gets the current constant list.
	 *
	 * @return the current constant list
	 */
	public static ConstantList getCurrent() {
		return current;
	}

	/**
	 * Gets a constant by index number.
	 *
	 * @param index the constant index
	 * @return the constant
	 */
	public Constant get(int index) {
		return this.constantList.get(index);
	}
}
