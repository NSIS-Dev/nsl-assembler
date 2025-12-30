/*
 * StatementExpression.java
 */

package nsl.statement;

import java.io.IOException;
import nsl.expression.*;

/**
 * Wraps an expression in a statement.
 *
 * @author Stuart
 */
public class StatementExpression extends Statement {
	private final AssembleExpression expression;

	/**
	 * Class constructor specifying the inner expression.
	 *
	 * @param expression the inner expression
	 */
	public StatementExpression(AssembleExpression expression) {
		this.expression = expression;
	}

	/**
	 * Gets the expression.
	 *
	 * @return the expression
	 */
	public Expression getExpression() {
		return this.expression;
	}

	/**
	 * Assembles the inner expression.
	 *
	 * @throws IOException
	 */
	@Override
	public void assemble() throws IOException {
		this.expression.assemble();
	}
}
