/*
 * ReturnStatement.java
 */

package nsl.statement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;

/**
 * The return statement for use in functions.
 *
 * @author Stuart
 */
public class ReturnStatement extends Statement {
	private final ArrayList<Expression> returns;

	private boolean last;

	/** Class constructor. */
	public ReturnStatement() {
		if (!FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Function), "return");

		if (ScriptParser.tokenizer.tokenIs('(')) {
			this.returns = Expression.matchList();
		} else {
			this.returns = new ArrayList<Expression>();
			this.returns.add(Expression.matchComplex());
		}
		ScriptParser.tokenizer.matchEolOrDie();
		if (FunctionInfo.getCurrent().getReturns() == -1)
			FunctionInfo.getCurrent().setReturns(this.returns.size());
		else if (FunctionInfo.getCurrent().getReturns() != this.returns.size())
			throw new NslException("return statement has differing number of return values", true);
	}

	/**
	 * Sets whether or not this is the last statement in the function.
	 *
	 * @param last whether or not this is the last statement
	 */
	public void setLast(boolean last) {
		this.last = last;
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		for (int i = this.returns.size() - 1; i >= 0; i--) {
			Expression varOrReturn = AssembleExpression.getRegisterOrExpression(this.returns.get(i));
			ScriptParser.writeLine("Push " + varOrReturn);
			varOrReturn.setInUse(false);
		}
		if (!this.last) ScriptParser.writeLine("Return");
	}
}
