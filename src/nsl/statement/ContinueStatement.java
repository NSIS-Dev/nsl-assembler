/*
 * ContinueStatement.java
 */

package nsl.statement;

import java.io.IOException;
import java.util.EnumSet;
import nsl.*;

/**
 * @author Stuart
 */
public class ContinueStatement extends Statement {
	/** Class constructor. */
	public ContinueStatement() {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(
					EnumSet.of(NslContext.Section, NslContext.Function), "continue");

		ScriptParser.tokenizer.matchEolOrDie();

		if (CodeInfo.getCurrent().getContinueLabel() == null)
			throw new NslException("The \"continue\" statement cannot be used here", true);
	}

	@Override
	public void assemble() throws IOException {
		CodeInfo.getCurrent().getContinueLabel().setNotUsed(false);
		ScriptParser.writeLine("Goto " + CodeInfo.getCurrent().getContinueLabel());
	}
}
