/*
 * NslException.java
 */

package nsl;

/**
 * Thrown when a general error occurs.
 *
 * @author Stuart
 */
public class NslException extends RuntimeException {
	private Exception inner;

	/**
	 * Class constructor specifying an inner exception.
	 *
	 * @param inner the inner exception
	 */
	public NslException(Exception inner) {
		this.inner = inner;
	}

	/**
	 * Class constructor specifying the exception message.
	 *
	 * @param message the exception message
	 */
	public NslException(String message) {
		super(message + ".");
	}

	/**
	 * Class constructor specifying the exception message and whether or not to include the current
	 * line number.
	 *
	 * @param message the exception message
	 * @param includeLineNo
	 */
	public NslException(String message, boolean includeLineNo) {
		super(getParseStack(ScriptParser.tokenizer.lineno(), true) + message + ".");
	}

	/**
	 * Class constructor specifying the exception message and line number.
	 *
	 * @param message the exception message
	 * @param lineNo the current line number
	 */
	public NslException(String message, int lineNo) {
		super(getParseStack(lineNo, true) + message + ".");
	}

	/**
	 * Gets the inner exception.
	 *
	 * @return the inner exception
	 */
	public Exception getInner() {
		return this.inner;
	}

	/**
	 * Prints a warning in the output window.
	 *
	 * @param message the warning message to print
	 */
	public static void printWarning(String message) {
		System.out.println(getParseStack(ScriptParser.tokenizer.lineno(), false) + message + ".");
		System.out.flush();
	}

	/**
	 * Returns the error stack.
	 *
	 * @param lineNo the current line number
	 * @param isError is the message an error as opposed to a warning
	 * @return the error stack
	 */
	private static String getParseStack(int lineNo, boolean isError) {
		String errorStack = "";

		for (Tokenizer tokenizer : ScriptParser.tokenizers)
			if (tokenizer.getSource() != null)
				errorStack +=
						(isError ? "Error" : "Warning")
								+ " in "
								+ tokenizer.getSource()
								+ " on line "
								+ tokenizer.lineno()
								+ ":\r\n  ";

		if (ScriptParser.tokenizer.getSource() != null)
			errorStack +=
					(isError ? "Error" : "Warning")
							+ " in "
							+ ScriptParser.tokenizer.getSource()
							+ " on line "
							+ lineNo
							+ ":\r\n  ";

		return errorStack;
	}
}
