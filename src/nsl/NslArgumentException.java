/*
 * NslArgumentException.java
 */

package nsl;

import nsl.expression.ExpressionType;

/**
 * Thrown when an invalid function argument is used.
 *
 * @author Stuart
 */
public class NslArgumentException extends NslException {
	/**
	 * Class constructor specifying the function name, the bad parameter number and the value that was
	 * given for that parameter.
	 *
	 * @param function the function name
	 * @param paramNumber bad parameter number
	 * @param valueGiven the bad value
	 */
	public NslArgumentException(String function, int paramNumber, String valueGiven) {
		super(
				valueGiven
						+ " is not a valid value for the "
						+ translateParamNumber(paramNumber)
						+ " parameter of \""
						+ function
						+ "\"",
				true);
	}

	/**
	 * Class constructor specifying the function name, the bad parameter number and the expected
	 * expression type.
	 *
	 * @param function the function name
	 * @param paramNumber bad parameter number
	 * @param expectedType the expected expression type
	 */
	public NslArgumentException(String function, int paramNumber, ExpressionType expectedType) {
		super(
				"\""
						+ function
						+ "\" expects "
						+ expectedType
						+ " for its "
						+ translateParamNumber(paramNumber)
						+ " parameter",
				true);
	}

	/**
	 * Class constructor specifying the function name, the bad parameter number and the expected
	 * expression type.
	 *
	 * @param function the function name
	 * @param paramNumber bad parameter number
	 * @param expectedType the expected expression type
	 * @param orExpectedType the other expected expression type
	 */
	public NslArgumentException(
			String function,
			int paramNumber,
			ExpressionType expectedType,
			ExpressionType orExpectedType) {
		super(
				"\""
						+ function
						+ "\" expects "
						+ expectedType
						+ " or "
						+ orExpectedType
						+ " for its "
						+ translateParamNumber(paramNumber)
						+ " parameter",
				true);
	}

	/**
	 * Class constructor specifying the function name, the bad parameter number and the expected
	 * expression type.
	 *
	 * @param function the function name
	 * @param paramNumber bad parameter number
	 * @param mustBeLiteral the expression must be a literal expression, that is it must evaluate at
	 *     compile time
	 */
	public NslArgumentException(String function, int paramNumber, boolean mustBeLiteral) {
		super(
				"\""
						+ function
						+ "\" expects "
						+ (mustBeLiteral ? "a literal expression" : "a non-literal expression")
						+ " for its "
						+ translateParamNumber(paramNumber)
						+ " parameter",
				true);
	}

	/**
	 * Class constructor specifying the function name and the required number of parameters.
	 *
	 * @param function the function name
	 * @param params the number of expected parameters
	 */
	public NslArgumentException(String function, int params) {
		super("\"" + function + "\" expects " + params + " parameter(s)", true);
	}

	/**
	 * Class constructor specifying the number of parameters that a function expects.
	 *
	 * <p>{@code paramCountTo} can be 999 for a function with an infinite number of parameters.
	 *
	 * @param function the function name
	 * @param paramCountFrom the range start number
	 * @param paramCountTo the range finish number
	 */
	public NslArgumentException(String function, int paramCountFrom, int paramCountTo) {
		super(
				"\""
						+ function
						+ "\" expects "
						+ paramCountFrom
						+ (paramCountTo == 999 ? " or more" : " to " + paramCountTo)
						+ " parameters",
				true);
	}

	/**
	 * Class constructor specifying the number of parameters that a function expects.
	 *
	 * <p>{@code paramCountTo} can be 999 for a function with an infinite number of parameters.
	 *
	 * @param function the function name
	 * @param paramCountFrom the range start number
	 * @param paramCountTo the range finish number
	 * @param paramsGiven the number of parameters given
	 */
	public NslArgumentException(
			String function, int paramCountFrom, int paramCountTo, int paramsGiven) {
		super(
				"\""
						+ function
						+ "\" expects "
						+ paramCountFrom
						+ (paramCountTo == 999 ? " or more" : " to " + paramCountTo)
						+ " parameters, but "
						+ paramsGiven
						+ " parameter(s) were given",
				true);
	}

	/**
	 * Appends the number with "st", "nd", "rd" or "th".
	 *
	 * @param paramNumber the number
	 * @return the translated number
	 */
	private static String translateParamNumber(int paramNumber) {
		if (paramNumber < 4 || paramNumber > 20) {
			String s = Integer.toString(paramNumber);
			char last = s.charAt(s.length() - 1);
			switch (last) {
				case '1':
					return paramNumber + "st";
				case '2':
					return paramNumber + "nd";
				case '3':
					return paramNumber + "rd";
			}
		}

		return paramNumber + "th";
	}
}
