/*
 * PageExInfo.java
 */

package nsl;

/**
 * Describes a PageEx block.
 *
 * @author Stuart
 */
public class PageExInfo {
	private static PageExInfo current = null;

	/** Class constructor. */
	public PageExInfo() {}

	/**
	 * Determines if the parser is in a PageEx block.
	 *
	 * @return <code>true</code> if the parser is in a PageEx block
	 */
	public static boolean in() {
		return current != null;
	}

	/**
	 * Gets the current PageEx info.
	 *
	 * @return the current PageEx info
	 */
	public static PageExInfo getCurrent() {
		return current;
	}

	/**
	 * Sets the current PageEx info.
	 *
	 * @param pageExInfo the current PageEx info
	 */
	public static void setCurrent(PageExInfo pageExInfo) {
		current = pageExInfo;
	}
}
