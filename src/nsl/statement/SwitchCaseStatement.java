/*
 * SwitchCaseStatement.java
 */

package nsl.statement;

import java.io.IOException;
import nsl.*;
import nsl.expression.*;

/**
 * Represents a case statement within a switch statement.
 *
 * @author Stuart
 */
public class SwitchCaseStatement extends Statement {
	private final Expression match;
	private final int lineNo;
	private Label label;

	/**
	 * Class constructor.
	 *
	 * @param label the label for the case
	 */
	public SwitchCaseStatement() {
		this.lineNo = ScriptParser.tokenizer.lineno();
		this.match = Expression.match();
		ScriptParser.tokenizer.matchOrDie(':');

		if (!ExpressionType.isBoolean(this.match)
				&& !ExpressionType.isInteger(this.match)
				&& !ExpressionType.isString(this.match))
			throw new NslException(
					"\"case\" in a \"switch\" statement requires a literal string, Boolean or integer value",
					true);
	}

	/**
	 * Sets the label for this case.
	 *
	 * @param label the label for this case
	 */
	public void setLabel(Label label) {
		this.label = label;
	}

	/**
	 * Gets the label for this case.
	 *
	 * @return the label for this case
	 */
	public Label getLabel() {
		return this.label;
	}

	/**
	 * Gets the line number of this case statement.
	 *
	 * @return the line number of this case statement
	 */
	public int getLineNo() {
		return this.lineNo;
	}

	/**
	 * Gets the basic expression that this case matches.
	 *
	 * @return the basic expression that this case matches
	 */
	public Expression getMatch() {
		return this.match;
	}

	/**
	 * Assembles the source code.
	 *
	 * @throws IOException
	 */
	@Override
	public void assemble() throws IOException {
		this.label.write();
	}
}
