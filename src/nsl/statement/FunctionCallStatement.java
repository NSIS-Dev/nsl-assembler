/*
 * FunctionCallStatement.java
 */

package nsl.statement;

import java.io.IOException;
import java.util.ArrayList;
import nsl.*;
import nsl.expression.*;

/**
 * Describes an nsL function call.
 *
 * @author Stuart
 */
public class FunctionCallStatement extends Statement {
	private final ArrayList<Register> returns;
	private final Expression functionCallExpression;

	/** Class constructor. */
	public FunctionCallStatement() {
		this.returns = new ArrayList<Register>();

		if (ScriptParser.tokenizer.tokenIs('(')) {
			ArrayList<Expression> returnsList = Expression.matchRegisterList();
			for (Expression ret : returnsList) {
				if (!ExpressionType.isRegister(ret))
					throw new NslException(
							"Return parameters in front of a function call must be registers", true);
				Scope.getCurrent().addVar(ret.getIntegerValue());
				this.returns.add(RegisterList.getCurrent().get(ret.getIntegerValue()));
			}
			ScriptParser.tokenizer.matchOrDie('=');
		}

		this.functionCallExpression = Expression.matchConstant(this.returns.size());
		if (!(this.functionCallExpression instanceof AssembleExpression))
			throw new NslException(
					"\"" + this.functionCallExpression.toString(true) + "\" is not a valid function call",
					true);

		ScriptParser.tokenizer.matchEolOrDie();
	}

	/**
	 * Class constructor.
	 *
	 * @param functionCallExpression the function call expression
	 */
	public FunctionCallStatement(Expression functionCallExpression) {
		this.returns = new ArrayList<Register>();
		this.functionCallExpression = functionCallExpression;
	}

	/**
	 * Gets the expression.
	 *
	 * @return the expression
	 */
	public Expression getExpression() {
		return this.functionCallExpression;
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		if (this.functionCallExpression instanceof MultipleReturnValueAssembleExpression) {
			((MultipleReturnValueAssembleExpression) this.functionCallExpression).assemble(this.returns);
		} else {
			if (this.returns.isEmpty()) ((AssembleExpression) this.functionCallExpression).assemble();
			else
				((AssembleExpression) this.functionCallExpression)
						.assemble(RegisterList.getCurrent().get(this.returns.get(0).getIntegerValue()));
		}
	}
}
