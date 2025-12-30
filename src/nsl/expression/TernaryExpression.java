/*
 * TernaryExpression.java
 */

package nsl.expression;

import java.io.IOException;
import nsl.*;

/**
 * Describes a ternary expression.
 *
 * @author Stuart
 */
public class TernaryExpression extends AssembleExpression {
	private final Expression leftOperand;
	private final Expression ifTrue;
	private final Expression ifFalse;

	/**
	 * Class constructor.
	 *
	 * @param leftOperand the Boolean expression
	 * @param ifTrue the expression if true
	 * @param ifFalse the expression if false
	 */
	public TernaryExpression(Expression leftOperand, Expression ifTrue, Expression ifFalse) {
		this.leftOperand = leftOperand;
		this.ifTrue = ifTrue;
		this.ifFalse = ifFalse;

		if (!(leftOperand instanceof BooleanExpression)
				&& !(leftOperand instanceof ComparisonExpression)
				&& !leftOperand.getType().equals(ExpressionType.Boolean))
			throw new NslException("The left operand must be an equality or relational expression", true);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		throw new UnsupportedOperationException("Not supported.");
	}

	/**
	 * Assembles the source code.
	 *
	 * @param var the variable to assign the value to
	 */
	@Override
	public void assemble(Register var) throws IOException {
		if (this.leftOperand.isLiteral()) {
			if (this.leftOperand.booleanValue == true) {
				if (this.ifTrue instanceof AssembleExpression)
					((AssembleExpression) this.ifTrue).assemble(var);
				else ScriptParser.writeLine("StrCpy " + var + " " + this.ifTrue);
			} else {
				if (this.ifFalse instanceof AssembleExpression)
					((AssembleExpression) this.ifFalse).assemble(var);
				else ScriptParser.writeLine("StrCpy " + var + " " + this.ifFalse);
			}
		} else if (this.leftOperand instanceof ConditionalExpression) {
			Label gotoA = LabelList.getCurrent().getNext();
			Label gotoB = LabelList.getCurrent().getNext();
			Label gotoEnd = LabelList.getCurrent().getNext();

			((ConditionalExpression) this.leftOperand).assemble(gotoA, gotoB);

			gotoA.write();
			if (this.ifTrue instanceof AssembleExpression)
				((AssembleExpression) this.ifTrue).assemble(var);
			else ScriptParser.writeLine("StrCpy " + var + " " + this.ifTrue);

			ScriptParser.writeLine("Goto " + gotoEnd);

			gotoB.write();
			if (this.ifFalse instanceof AssembleExpression)
				((AssembleExpression) this.ifFalse).assemble(var);
			else ScriptParser.writeLine("StrCpy " + var + " " + this.ifFalse);

			gotoEnd.write();
		} else throw new NslException("Expression is not a Boolean expression");
	}
}
