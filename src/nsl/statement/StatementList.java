/*
 * StatementList.java
 */

package nsl.statement;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Describes a list of statements.
 *
 * @author Stuart
 */
public class StatementList extends Statement {
	private final ArrayList<Statement> statementList;
	private final ArrayList<Statement> queuedStatementList;

	private static StatementList current;

	/**
	 * Gets the current statement list.
	 *
	 * @return the current statement list
	 */
	public static StatementList getCurrent() {
		return current;
	}

	/** Class constructor. */
	private StatementList() {
		this.statementList = new ArrayList<Statement>();
		this.queuedStatementList = new ArrayList<Statement>();
	}

	/**
	 * Matches a list of statements.
	 *
	 * @return a list of statements
	 */
	public static StatementList match() {
		StatementList statementListParent = current;
		StatementList statementList = new StatementList();
		current = statementList;

		Statement statement;
		while ((statement = Statement.match()) != null) {
			// Add the current statement.
			statementList.statementList.add(statement);

			// Add any queued statements (i.e. contents of a macro) and then dequeue
			// them.
			if (!statementList.queuedStatementList.isEmpty()) {
				statementList.statementList.addAll(statementList.queuedStatementList);
				statementList.queuedStatementList.clear();
			}
		}

		current = statementListParent;
		return statementList;
	}

	/**
	 * Adds a statement to the list.
	 *
	 * @param statement the statement to add
	 */
	public void add(Statement statement) {
		this.statementList.add(statement);
	}

	/**
	 * Adds a statement to the queued statements list. These statements will be added after the
	 * current statement.
	 *
	 * @param statement the statement to add
	 */
	public void addQueued(Statement statement) {
		this.queuedStatementList.add(statement);
	}

	/**
	 * Determines if the statement list is empty.
	 *
	 * @return <code>true</code> if the statement list is empty
	 */
	public boolean isEmpty() {
		return this.statementList.isEmpty();
	}

	/**
	 * Gets the last statement in the statement list.
	 *
	 * @return the last statement in the statement list
	 */
	public Statement getLast() {
		if (this.statementList.isEmpty()) return null;
		return this.statementList.get(this.statementList.size() - 1);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		for (Statement statement : this.statementList) statement.assemble();
	}
}
