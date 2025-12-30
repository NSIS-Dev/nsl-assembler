/*
 * JumpExpression.java
 */

package nsl.expression;

import java.io.IOException;
import java.util.ArrayList;
import nsl.Label;
import nsl.NslException;
import nsl.Register;
import nsl.statement.SwitchCaseStatement;

/**
 * An NSIS instruction which performs jumps.
 *
 * @author Stuart
 */
public abstract class JumpExpression extends ComparisonExpression {
	protected Expression thrownAwayAfterOptimise;

	/** Assembles the source code. */
	@Override
	public abstract void assemble() throws IOException;

	/**
	 * Assembles the source code.
	 *
	 * @param var the variable to assign the value to
	 */
	@Override
	public abstract void assemble(Register var) throws IOException;

	/**
	 * Assembles the source code.
	 *
	 * @param gotoA the first go-to label
	 * @param gotoB the second go-to label
	 */
	@Override
	public abstract void assemble(Label gotoA, Label gotoB) throws IOException;

	/**
	 * Assembles the source code.
	 *
	 * @param switchCases a list of {@link SwitchCaseStatement} in the switch statement statement that
	 *     this {@code JumpExpression} is being switched on.
	 */
	public abstract void assemble(ArrayList<SwitchCaseStatement> switchCases) throws IOException;

	/**
	 * Checks whether the switch cases are valid for this type of {@code JumpExpression}.
	 *
	 * @param the line number of the switch statement
	 * @param switchCases a list of {@link SwitchCaseStatement} in the switch statement
	 */
	public void checkSwitchCases(ArrayList<SwitchCaseStatement> switchCases, int switchLineNo) {
		for (SwitchCaseStatement caseStatement : switchCases)
			if (!caseStatement.getMatch().type.equals(ExpressionType.Boolean))
				throw new NslException(
						"Invalid \"case\" value of " + caseStatement.getMatch(), caseStatement.getLineNo());
	}

	/**
	 * Attempts to optimise the jump expression.
	 *
	 * <p>For example, Silent() == true becomes: IfSilent jump_a jump_b
	 *
	 * <p>Without this type of optimisation, it would instead become: IfSilent 0 +3 StrCpy
	 * $SomeTempVar true Goto +2 StrCpy $SomeTempVar false StrCmp $SomeTempVar true jump_a jump_b
	 *
	 * <p>We reduce 5 instructions into 1 and save a register!
	 *
	 * @param returnCheck the return check expression
	 * @param operator the operator being used
	 * @return <code>true</code> if the expression could be optimised
	 */
	public boolean optimise(Expression returnCheck, String operator) {
		if (returnCheck.type.equals(ExpressionType.Boolean)) {
			this.thrownAwayAfterOptimise = returnCheck;
			this.booleanValue =
					operator.equals("==") ? returnCheck.getBooleanValue() : !returnCheck.getBooleanValue();
			return true;
		}
		return false;
	}
}
