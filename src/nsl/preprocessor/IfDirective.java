/*
 * IfDirective.java
 */

package nsl.preprocessor;

import java.io.IOException;
import nsl.*;
import nsl.expression.*;
import nsl.statement.*;

/**
 * @author Stuart
 */
public class IfDirective extends Statement {
	private static boolean inIfDirective = false;

	private StatementList statementList;

	/** Class constructor. */
	public IfDirective() {
		this.statementList = null;

		while (true) {
			int line = ScriptParser.tokenizer.lineno();

			Expression booleanExpression = Expression.matchComplex();
			if (booleanExpression instanceof AssembleExpression)
				throw new NslException(
						"\"#if\" directive requires a Boolean expression that can be evaluated", true);

			if (booleanExpression.getBooleanValue() == true && this.statementList == null) {
				boolean inIfDirectiveOld = inIfDirective;
				inIfDirective = true;
				this.statementList = StatementList.match();
				inIfDirective = inIfDirectiveOld;
			} else {
				do {
					if (ScriptParser.tokenizer.tokenIs("#endif")
							|| ScriptParser.tokenizer.tokenIs("#else")
							|| ScriptParser.tokenizer.tokenIs("#elseif")) break;
				} while (ScriptParser.tokenizer.tokenNext());
			}

			if (ScriptParser.tokenizer.match("#elseif")) continue;

			if (ScriptParser.tokenizer.match("#else")) {
				if (this.statementList == null) {
					boolean inIfDirectiveOld = inIfDirective;
					inIfDirective = true;
					this.statementList = StatementList.match();
					inIfDirective = inIfDirectiveOld;
				} else {
					do {
						if (ScriptParser.tokenizer.tokenIs("#endif")) break;
					} while (ScriptParser.tokenizer.tokenNext());
				}
			}

			if (ScriptParser.tokenizer.match("#endif")) break;

			throw new NslException("\"#if\" missing matching \"#endif\"", line);
		}
	}

	/**
	 * Determines if the parser is inside an #if pre-processor directive.
	 *
	 * @return <code>true</code> if the parser is inside an #if pre-processor directive
	 */
	public static boolean in() {
		return inIfDirective;
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		if (this.statementList != null) this.statementList.assemble();
	}
}
