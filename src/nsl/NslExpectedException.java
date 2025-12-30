/*
 * NslExpectedException.java
 */

package nsl;

import java.io.StreamTokenizer;

/**
 * Thrown when an unexpected token is found while parsing.
 *
 * @author Stuart
 */
public class NslExpectedException extends NslException {
	/**
	 * Class constructor specifying the expected string.
	 *
	 * @param expected the expected string
	 */
	public NslExpectedException(String expected) {
		super(
				String.format(
						"Expected %s, but found %s",
						expected,
						ScriptParser.tokenizer.ttype == StreamTokenizer.TT_EOF
								? "the end of the file"
								: ScriptParser.tokenizer.tokenIsWord()
										? "\"" + ScriptParser.tokenizer.sval + "\""
										: ScriptParser.tokenizer.tokenIsNumber()
												? "\"" + (int) ScriptParser.tokenizer.nval + "\""
												: ScriptParser.tokenizer.tokenIsString()
														? "\"" + ScriptParser.tokenizer.sval + "\""
														: "\"" + (char) ScriptParser.tokenizer.ttype + "\""),
				true);
	}

	/**
	 * Class constructor specifying the expected string and the found string.
	 *
	 * @param expected the expected string
	 * @param found the found string
	 */
	public NslExpectedException(String expected, String found) {
		super(String.format("Expected %s, but found %s", expected, found), true);
	}
}
