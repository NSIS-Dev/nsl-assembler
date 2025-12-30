/*
 * RedefineDirective.java
 */

package nsl.preprocessor;

import java.io.IOException;
import nsl.*;
import nsl.expression.*;
import nsl.statement.Statement;

/**
 * Defines a new preprocessor constant.
 *
 * @author Stuart
 */
public class RedefineDirective extends Statement {
	/** Class constructor. */
	public RedefineDirective() {
		int line = ScriptParser.tokenizer.lineno();
		String name = ScriptParser.tokenizer.matchAWord("a constant name");
		int valueLine = ScriptParser.tokenizer.lineno();

		// If the next token is on a new line then we can assume the constant has no
		// value associated with it.
		Expression value;
		if (line != valueLine) {
			value = Expression.Empty;
		} else {
			boolean specialStringNoEscapePrevious = Expression.setSpecialStringEscape(false);
			value = Expression.matchComplex();
			Expression.setSpecialStringEscape(specialStringNoEscapePrevious);
		}

		if (value instanceof AssembleExpression)
			throw new NslException(
					"\"#redefine\" directive (for constant \""
							+ name
							+ "\") requires an expression that can be evaluated",
					true);

		DefineList.getCurrent().add(name, value);
	}

	/**
	 * Assembles nothing.
	 *
	 * @throws IOException
	 */
	@Override
	public void assemble() throws IOException {}
}
