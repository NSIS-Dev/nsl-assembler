/*
 * ErrorDirective.java
 */

package nsl.preprocessor;

import java.io.IOException;
import nsl.*;
import nsl.statement.Statement;

/**
 * Directive to throw an error.
 *
 * @author Stuart
 */
public class ErrorDirective extends Statement {
	/** Class constructor. */
	public ErrorDirective() {
		int line = ScriptParser.tokenizer.lineno();
		String error = ScriptParser.tokenizer.matchAString();
		if (error == null || (error = error.trim()).isEmpty())
			throw new NslException("An error occurred (no error message specified)", line);
		throw new NslException(error, line);
	}

	/**
	 * Assembles nothing.
	 *
	 * @throws IOException
	 */
	@Override
	public void assemble() throws IOException {}
}
