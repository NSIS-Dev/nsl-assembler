/*
 * Label.java
 */

package nsl;

import java.io.IOException;

/**
 * A go-to label.
 *
 * @author Stuart
 */
public class Label {
	private final String name;
	private boolean notUsed;

	/**
	 * Class constructor specifying the go-to label name.
	 *
	 * @param name the go-to label name
	 */
	public Label(String name) {
		this.name = name;
		this.notUsed = false;
	}

	/**
	 * Gets whether or not the go-to label has not been used.
	 *
	 * @return whether or not the go-to label has not been used
	 */
	public boolean isNotUsed() {
		return this.notUsed;
	}

	/**
	 * Sets whether or not the go-to label has not been used.
	 *
	 * @param notUsed whether or not the go-to label has not been used
	 */
	public void setNotUsed(boolean notUsed) {
		this.notUsed = notUsed;
	}

	/** Writes the go-to label. */
	public void write() throws IOException {
		if (!this.notUsed) ScriptParser.writeLine(this.name + ":");
	}

	/**
	 * Gets the go-to label name.
	 *
	 * @return the go-to label name
	 */
	@Override
	public String toString() {
		this.notUsed = false;
		return this.name;
	}
}
