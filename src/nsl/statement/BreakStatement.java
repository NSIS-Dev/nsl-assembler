/*
 * BreakStatement.java
 */

package nsl.statement;

import java.io.IOException;
import java.util.EnumSet;
import nsl.*;

/**
 * @author Stuart
 */
public class BreakStatement extends Statement {
	/** Class constructor. */
	public BreakStatement() {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), "break");

		ScriptParser.tokenizer.matchEolOrDie();

		if (CodeInfo.getCurrent().getBreakLabel() == null)
			throw new NslException("The \"break\" statement cannot be used here", true);
	}

	@Override
	public void assemble() throws IOException {
		CodeInfo.getCurrent().getBreakLabel().setNotUsed(false);
		ScriptParser.writeLine("Goto " + CodeInfo.getCurrent().getBreakLabel());
	}
}
