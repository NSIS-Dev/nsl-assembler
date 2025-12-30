/*
 * LabelList.java
 */

package nsl;

import java.io.IOException;

/**
 * Describes a list of go-to labels.
 *
 * @author Stuart
 */
public class LabelList {
	private static LabelList current = new LabelList();
	private int counter;

	/** Class constructor. */
	public LabelList() {
		this.counter = 0;
	}

	/** Resets the label list counter. */
	public void reset() {
		this.counter = 0;
	}

	/**
	 * Returns the script label list used in sections and functions.
	 *
	 * @return the script label list used in sections and functions
	 */
	public static LabelList getCurrent() {
		return current;
	}

	/**
	 * Gets the next label.
	 *
	 * @param write write the label
	 * @return the next label
	 * @throws IOException
	 */
	public Label getNext() {
		Label label = new Label("_lbl_" + this.counter);
		this.counter++;
		return label;
	}
}
