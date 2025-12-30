/*
 * WhileStatement.java
 */

package nsl.statement;

import java.io.IOException;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;

/**
 * @author Stuart
 */
public class WhileStatement extends Statement {
	private final Expression booleanExpression;
	private final BlockStatement blockStatement;

	/** Class constructor. */
	public WhileStatement() {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), "while");

		ScriptParser.tokenizer.matchOrDie('(');
		this.booleanExpression = Expression.matchComplex();
		if (!this.booleanExpression.getType().equals(ExpressionType.Boolean))
			throw new NslException("A \"while\" statement requires a Boolean expression", true);
		ScriptParser.tokenizer.matchOrDie(')');

		// Set non-null values so that the block statement can contain break or continue statements.
		CodeInfo.getCurrent().setBreakLabel(RelativeJump.Zero);
		CodeInfo.getCurrent().setContinueLabel(RelativeJump.Zero);

		this.blockStatement = new BlockStatement();

		CodeInfo.getCurrent().setBreakLabel(null);
		CodeInfo.getCurrent().setContinueLabel(null);
	}

	/**
	 * Assembles the source code.
	 *
	 * @throws IOException
	 * @throws NslException
	 */
	@Override
	public void assemble() throws IOException {
		// Do not assemble anything if the while loop will never loop!
		if (this.booleanExpression != null
				&& this.booleanExpression.isLiteral()
				&& this.booleanExpression.getBooleanValue() == false) {
			// May still need assembling even though it is a literal.
			AssembleExpression.assembleIfRequired(this.booleanExpression);

			return;
		}

		Label gotoLoop = LabelList.getCurrent().getNext();
		Label gotoEnd = LabelList.getCurrent().getNext();

		gotoLoop.write();

		gotoEnd.setNotUsed(true);

		if (this.booleanExpression != null && this.booleanExpression instanceof ConditionalExpression) {
			Label gotoEnter = LabelList.getCurrent().getNext();
			gotoEnter.setNotUsed(true);
			((ConditionalExpression) this.booleanExpression).assemble(gotoEnter, gotoEnd);
			gotoEnter.write();
		}

		Label parentBreak = CodeInfo.getCurrent().setBreakLabel(gotoEnd);
		Label parentContinue = CodeInfo.getCurrent().setContinueLabel(gotoLoop);

		this.blockStatement.assemble();

		CodeInfo.getCurrent().setBreakLabel(parentBreak);
		CodeInfo.getCurrent().setContinueLabel(parentContinue);

		ScriptParser.writeLine("Goto " + gotoLoop);
		gotoEnd.write();
	}
}
