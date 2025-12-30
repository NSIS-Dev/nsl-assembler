/*
 * UndefDirective.java
 */

package nsl.preprocessor;

import java.io.IOException;
import nsl.*;
import nsl.statement.Statement;

/**
 * Un-defines a previously defined preprocessor constant.
 *
 * @author Stuart
 */
public class UndefDirective extends Statement {
	/** Class constructor. */
	public UndefDirective() {
		int line = ScriptParser.tokenizer.lineno();
		String name = ScriptParser.tokenizer.matchAWord("a constant name");

		if (!DefineList.getCurrent().remove(name))
			throw new NslException("Constant \"" + name + "\" is not defined", line);
	}

	/**
	 * Assembles nothing.
	 *
	 * @throws IOException
	 */
	@Override
	public void assemble() throws IOException {}
}
