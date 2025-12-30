/*
 * SectionInfo.java
 */

package nsl;

/**
 * Describes an install section.
 *
 * @author Stuart
 */
public class SectionInfo extends CodeInfo {
	/** Class constructor. */
	public SectionInfo() {}

	/**
	 * Determines if the parser is in a section.
	 *
	 * @return <code>true</code> if the parser is in a section
	 */
	public static boolean in() {
		return current != null && current instanceof SectionInfo;
	}

	/**
	 * Gets the current code info.
	 *
	 * @return the current code info
	 */
	public static SectionInfo getCurrent() {
		return (SectionInfo) current;
	}
}
