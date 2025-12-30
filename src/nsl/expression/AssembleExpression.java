/*
 * AssembleExpression.java
 */

package nsl.expression;

import java.io.IOException;
import nsl.Register;
import nsl.RegisterList;

/**
 * An expression that can be assembled.
 *
 * @author Stuart
 */
public abstract class AssembleExpression extends Expression {
	/**
	 * Determines if the expression is a literal (hard coded) value.
	 *
	 * @return <code>true</code> if the expression is a literal (hard coded) value
	 */
	@Override
	public boolean isLiteral() {
		return false;
	}

	/** Assembles the source code. */
	public abstract void assemble() throws IOException;

	/**
	 * Assembles the source code.
	 *
	 * @param var the variable to assign the value to
	 */
	public abstract void assemble(Register var) throws IOException;

	/**
	 * Assembles the given expression if it's a {@link AssembleExpression}.
	 *
	 * @param expression the expression
	 */
	public static void assembleIfRequired(Expression expression) throws IOException {
		if (expression instanceof AssembleExpression) ((AssembleExpression) expression).assemble();
	}
	/**
	 * Gets the next freely available NSIS register if the given expression requires assembling or the
	 * given expression otherwise.
	 *
	 * @param expression the {@link Expression} to return if it is not a {@link AssembleExpression}
	 * @return the next freely available NSIS register (or the given expression)
	 */
	public static Expression getRegisterOrExpression(Expression expression) throws IOException {
		if (expression instanceof AssembleExpression) {
			Register var = RegisterList.getCurrent().getNext();
			((AssembleExpression) expression).assemble(var);
			return var;
		}

		return expression;
	}
}
