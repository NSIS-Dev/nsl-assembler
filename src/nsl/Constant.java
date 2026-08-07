/*
 * Constant.java
 */

package nsl;

/**
 * An NSIS constant.
 *
 * @author Stuart
 */
public class Constant {
	private final String name;
	private final String realName;
	private final int index;

	/**
	 * Class constructor.
	 *
	 * @param name the constant name
	 * @param realName the real constant name if it differs
	 * @param index
	 */
	public Constant(String name, String realName, int index) {
		this.name = name;
		this.realName = null;
		this.index = index;
	}

	/**
	 * Gets the register index.
	 *
	 * @return the register index
	 */
	public int getIndex() {
		return this.index;
	}

	/**
	 * Gets a string representation of the current object.
	 *
	 * @return a string representation of the current object
	 */
	@Override
	public String toString() {
		if (this.realName != null) return this.realName;
		return this.name;
	}
}
