/*
 * ForStatement.java
 */

package nsl.statement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;

/**
 * @author Stuart
 */
public class ForStatement extends Statement {
	private final ArrayList<Expression> assignmentExpressions;
	private final Expression booleanExpression;
	private final ArrayList<Expression> loopExpressions;
	private final BlockStatement blockStatement;

	/** Class constructor. */
	public ForStatement() {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), "for");

		ScriptParser.tokenizer.matchOrDie('(');

		this.assignmentExpressions = new ArrayList<Expression>();
		if (!ScriptParser.tokenizer.match(';')) {
			do {
				Expression left = Expression.matchComplex();
				if (!(left instanceof AssignmentExpression))
					throw new NslException(
							"A \"for\" statement requires an assignment expression for initialization", true);
				this.assignmentExpressions.add(left);
			} while (ScriptParser.tokenizer.match(','));
			ScriptParser.tokenizer.matchOrDie(';');
		}

		if (!ScriptParser.tokenizer.match(';')) {
			this.booleanExpression = Expression.matchComplex();
			if (!this.booleanExpression.getType().equals(ExpressionType.Boolean))
				throw new NslException(
						"A \"for\" statement requires a Boolean expression for its condition", true);
			ScriptParser.tokenizer.matchOrDie(';');
		} else this.booleanExpression = null;

		this.loopExpressions = new ArrayList<Expression>();
		if (!ScriptParser.tokenizer.match(')')) {
			do {
				Expression left = Expression.matchComplex();
				if (!(left instanceof AssignmentExpression))
					throw new NslException(
							"A \"for\" statement requires an assignment expression for each iteration", true);
				this.loopExpressions.add(left);
			} while (ScriptParser.tokenizer.match(','));
			ScriptParser.tokenizer.matchOrDie(')');
		}

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
		// Do not assemble anything if the for loop will never loop!
		if (this.booleanExpression != null
				&& this.booleanExpression.isLiteral()
				&& this.booleanExpression.getBooleanValue() == false) {
			// May still need assembling even though it is a literal.
			AssembleExpression.assembleIfRequired(this.booleanExpression);
			return;
		}

		Label gotoLoop = LabelList.getCurrent().getNext();
		Label gotoContinue = LabelList.getCurrent().getNext();
		Label gotoEnd = LabelList.getCurrent().getNext();

		// We don't use the continue label unless a continue statement has been used.
		gotoContinue.setNotUsed(true);

		for (Expression assignmentExpression : assignmentExpressions)
			((AssembleExpression) assignmentExpression).assemble();

		gotoLoop.write();

		if (this.booleanExpression != null && this.booleanExpression instanceof ConditionalExpression) {
			Label gotoEnter = LabelList.getCurrent().getNext();
			((ConditionalExpression) this.booleanExpression).assemble(gotoEnter, gotoEnd);
			gotoEnter.write();
		}

		Label parentBreak = CodeInfo.getCurrent().setBreakLabel(gotoEnd);
		Label parentContinue = CodeInfo.getCurrent().setContinueLabel(gotoContinue);

		this.blockStatement.assemble();

		CodeInfo.getCurrent().setBreakLabel(parentBreak);
		CodeInfo.getCurrent().setContinueLabel(parentContinue);

		gotoContinue.write();

		for (Expression loopExpression : loopExpressions)
			((AssembleExpression) loopExpression).assemble();

		ScriptParser.writeLine("Goto " + gotoLoop);
		gotoEnd.write();
	}
}
