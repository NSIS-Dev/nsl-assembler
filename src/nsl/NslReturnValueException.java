/*
 * NslReturnValueException.java
 */

package nsl;

/**
 * Thrown when the number of return registers used is different to the number of values returned.
 *
 * @author Stuart
 */
public class NslReturnValueException extends NslException {
	/**
	 * Class constructor.
	 *
	 * @param function the function name
	 */
	public NslReturnValueException(String function) {
		super("The \"" + function + "\" instruction does not return a value", true);
	}

	/**
	 * Class constructor.
	 *
	 * @param function the function name
	 * @param returns the number of return values
	 */
	public NslReturnValueException(String function, int returns) {
		super(
				"The \""
						+ function
						+ "\" instruction only returns "
						+ returns
						+ (returns == 1 ? " value" : " values"),
				true);
	}

	/**
	 * Class constructor.
	 *
	 * @param function the function name
	 * @param returns the number of return values
	 * @param orReturns the other number of return values
	 */
	public NslReturnValueException(String function, int returns, int orReturns) {
		super(
				"The \""
						+ function
						+ "\" instruction can only return "
						+ returns
						+ " or "
						+ orReturns
						+ (orReturns == 1 ? " value" : " values"),
				true);
	}
}
