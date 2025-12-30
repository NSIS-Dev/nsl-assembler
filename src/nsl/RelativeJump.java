/*
 * RelativeJump.java
 */

package nsl;

/**
 * A relative go-to jump (0, -1, +1 etc).
 *
 * @author Stuart
 */
public class RelativeJump extends Label {
	/** The zero relative jump. */
	public static final RelativeJump Zero = new RelativeJump("0");

	/**
	 * Class constructor.
	 *
	 * @param jump the relative go-to jump
	 */
	public RelativeJump(String jump) {
		super(jump);
	}

	/** Does nothing. */
	@Override
	public void write() {}
}
