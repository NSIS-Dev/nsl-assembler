/*
 * PageStatement.java
 */

package nsl.statement;

import java.io.IOException;
import java.util.EnumSet;
import nsl.*;

/**
 * Describes a block of statements.
 *
 * @author Stuart
 */
public class BlockStatement extends Statement {
	private final Statement statementList;

	/** Class constructor. */
	public BlockStatement() {
		if (ScriptParser.inGlobalContext())
			throw new NslContextException(
					EnumSet.of(NslContext.Section, NslContext.Function), "code block");

		Scope.create();

		if (ScriptParser.tokenizer.match('{')) {
			this.statementList = StatementList.match();
			ScriptParser.tokenizer.matchOrDie('}');
		} else {
			this.statementList = Statement.match();
			if (this.statementList == null) throw new NslExpectedException("a statement");
		}

		Scope.getCurrent().end();
	}

	/**
	 * Determines if the block statement is empty.
	 *
	 * @return <code>true</code> if the block statement is empty
	 */
	public boolean isEmpty() {
		if (this.statementList instanceof StatementList)
			return ((StatementList) this.statementList).isEmpty();
		return false;
	}

	/**
	 * Gets the last statement in the statement list.
	 *
	 * @return the last statement in the statement list
	 */
	public Statement getLast() {
		if (this.statementList instanceof StatementList)
			return ((StatementList) this.statementList).getLast();
		if (this.statementList instanceof BlockStatement)
			return ((BlockStatement) this.statementList).getLast();
		return this.statementList;
	}

	/** Assembles the source code. */
	public void assemble() throws IOException {
		if (this.statementList != null) this.statementList.assemble();
	}
}
