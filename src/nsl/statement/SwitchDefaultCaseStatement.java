/*
 * SwitchDefaultCaseStatement.java
 */

package nsl.statement;

import java.io.IOException;
import nsl.*;

/**
 * Represents the default case within a switch statement.
 *
 * @author Stuart
 */
public class SwitchDefaultCaseStatement extends Statement {
	private Label label;

	/**
	 * Class constructor.
	 *
	 * @param label the label for the case
	 */
	public SwitchDefaultCaseStatement() {
		ScriptParser.tokenizer.matchOrDie(':');
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
	 * Assembles the source code.
	 *
	 * @throws IOException
	 */
	@Override
	public void assemble() throws IOException {
		this.label.write();
	}
}
