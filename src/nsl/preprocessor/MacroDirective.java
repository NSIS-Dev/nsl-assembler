/*
 * MacroDirective.java
 */

package nsl.preprocessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import nsl.*;
import nsl.expression.Expression;
import nsl.statement.Statement;

/**
 * Creates a new macro.
 *
 * @author Stuart
 */
public class MacroDirective extends Statement {
	/** Class constructor. */
	public MacroDirective() {
		int macroLine = ScriptParser.tokenizer.lineno();
		String name = ScriptParser.tokenizer.matchAWord("a macro name");

		ScriptParser.tokenizer.matchOrDie('(');
		Set<String> paramsSet = new HashSet<String>();
		ArrayList<String> paramsList = new ArrayList<String>();
		if (!ScriptParser.tokenizer.tokenIs(')')) {
			while (true) {
				String word = ScriptParser.tokenizer.matchAWord("a constant name");

				if (!paramsSet.add(word))
					throw new NslException(
							"Macro \"" + name + "\" has parameter names with the same name", macroLine);
				paramsList.add(word);

				if (ScriptParser.tokenizer.tokenIs(')')) break;
				ScriptParser.tokenizer.matchOrDie(',');
			}
		}

		String contents = ScriptParser.tokenizer.readUntil("#macroend");
		ScriptParser.tokenizer.tokenNext();

		if (!MacroList.getCurrent()
				.add(new Macro(name, paramsList.toArray(new String[0]), macroLine, contents)))
			throw new NslException(
					"Macro \"" + name + "\" already defined with " + paramsList.size() + " parameters",
					macroLine);
	}

	/** Matches the #return directive which can only be used inside a #macro. */
	public static void matchReturnDirective() {
		MacroEvaluated macro = MacroEvaluated.getCurrent();
		if (macro == null)
			throw new NslException("\"#return\" directive can only be used within a \"#macro\"", true);

		boolean specialStringNoEscapePrevious = Expression.setSpecialStringEscape(false);
		if (ScriptParser.tokenizer.tokenIs('(')) {
			macro.setReturnValues(Expression.matchList());
		} else {
			ArrayList<Expression> returnValues = new ArrayList<Expression>();
			returnValues.add(Expression.matchComplex());
			macro.setReturnValues(returnValues);
		}
		Expression.setSpecialStringEscape(specialStringNoEscapePrevious);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {}
}
