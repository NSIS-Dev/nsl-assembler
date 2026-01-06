/*
 * UnsafeStrCpyInstruction.java
 */

package nsl.instruction;

/**
 * @author Jan
 */
public class UnsafeStrCpyInstruction extends StrCpyInstruction {
	public static final String name = "UnsafeStrCpy";

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public UnsafeStrCpyInstruction(int returns) {
		super(returns, name);
	}
}
