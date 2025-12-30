/*
 * Expression.java
 */

package nsl.expression;

import nsl.ScriptParser;

/**
 * Describes a pointer to a function or a macro.
 *
 * @author Stuart
 */
public class ReferenceExpression extends Expression {
	/** Class constructor. */
	public ReferenceExpression() {
		ScriptParser.tokenizer.matchOrDie('(');
		this.type = ExpressionType.String;
		this.stringValue = ScriptParser.tokenizer.matchAWord("a name of reference");
		ScriptParser.tokenizer.matchOrDie(')');
	}
}
