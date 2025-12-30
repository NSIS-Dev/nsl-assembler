/*
 * AssignmentStatement.java
 */

package nsl.statement;

import java.io.IOException;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;

/**
 * An assignment statement where the left operand is a variable and the right operand is an
 * expression.
 *
 * @author Stuart
 */
public class AssignmentStatement extends Statement {
	private final Expression assignmentExpression;

	/** Class constructor. */
	public AssignmentStatement() {
		if (PageExInfo.in())
			throw new NslContextException(
					EnumSet.of(NslContext.Global, NslContext.Section, NslContext.Function),
					"assignment statement");

		this.assignmentExpression = Expression.matchComplex();
		if (!(this.assignmentExpression instanceof AssignmentExpression))
			throw new NslExpectedException("an assignment expression");

		ScriptParser.tokenizer.matchEolOrDie();
	}

	/**
	 * Class constructor.
	 *
	 * @param the assignment expression
	 */
	public AssignmentStatement(Expression assignmentExpression) {
		if (PageExInfo.in())
			throw new NslContextException(
					EnumSet.of(NslContext.Global, NslContext.Section, NslContext.Function),
					"assignment statement");

		this.assignmentExpression = assignmentExpression;
	}

	/**
	 * Gets the expression.
	 *
	 * @return the expression
	 */
	public Expression getExpression() {
		return this.assignmentExpression;
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		((AssembleExpression) this.assignmentExpression).assemble();
	}
}
