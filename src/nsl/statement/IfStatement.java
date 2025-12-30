/*
 * IfStatement.java
 */

package nsl.statement;

import java.io.IOException;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;

/**
 * @author Stuart
 */
public class IfStatement extends Statement {
	private final Expression booleanExpression;
	private final BlockStatement blockStatement;
	private IfStatement elseStatement;

	/** Class constructor. */
	public IfStatement() throws NslContextException, NslExpectedException, NslException {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), "if");

		ScriptParser.tokenizer.matchOrDie('(');
		this.booleanExpression = Expression.matchComplex();
		if (!this.booleanExpression.getType().equals(ExpressionType.Boolean))
			throw new NslException("An \"if\" statement requires a Boolean expression", true);
		ScriptParser.tokenizer.matchOrDie(')');

		this.blockStatement = new BlockStatement();

		this.elseStatement = null;
		if (ScriptParser.tokenizer.tokenIs("else")) {
			ScriptParser.tokenizer.tokenNext();
			if (ScriptParser.tokenizer.match("if")) this.elseStatement = new IfStatement();
			else this.elseStatement = new IfStatement(new BlockStatement());
		}
	}

	/**
	 * Class constructor specifying the block statement for an else if / else.
	 *
	 * @param blockStatement the block statement
	 */
	private IfStatement(BlockStatement blockStatement) {
		this.booleanExpression = null;
		this.blockStatement = blockStatement;
		this.elseStatement = null;
	}

	/** Assembles the source code. */
	private void assemble(Label gotoEnd) throws IOException {
		if (this.booleanExpression != null) {
			if (this.booleanExpression.isLiteral()) {
				// May still need assembling even though it is a literal.
				AssembleExpression.assembleIfRequired(this.booleanExpression);

				if (this.booleanExpression.getBooleanValue() == true) this.blockStatement.assemble();
				else if (this.elseStatement != null) this.elseStatement.assemble();

				return;
			}

			Label gotoA = LabelList.getCurrent().getNext();
			gotoA.setNotUsed(true);
			Label gotoB;
			if (this.elseStatement != null || gotoEnd == null) {
				gotoB = LabelList.getCurrent().getNext();
				gotoB.setNotUsed(true);
			} else gotoB = gotoEnd;

			((ConditionalExpression) this.booleanExpression).assemble(gotoA, gotoB);

			gotoA.write();

			this.blockStatement.assemble();

			if (this.elseStatement != null || gotoEnd == null) {
				if (gotoEnd != null) ScriptParser.writeLine("Goto " + gotoEnd);

				gotoB.write();
			}
		} else this.blockStatement.assemble();

		if (this.elseStatement != null) this.elseStatement.assemble(gotoEnd);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		// if (!this.blockStatement.isEmpty())
		{
			if (this.booleanExpression != null && this.booleanExpression.isLiteral()) {
				// May still need assembling even though it is a literal.
				AssembleExpression.assembleIfRequired(this.booleanExpression);

				// Boolean expression evaluates to true.
				if (this.booleanExpression.getBooleanValue() == true) this.blockStatement.assemble();
				else if (this.elseStatement != null) this.elseStatement.assemble();
			} else {
				if (this.elseStatement != null) {
					Label gotoEnd = LabelList.getCurrent().getNext();
					gotoEnd.setNotUsed(true);
					this.assemble(gotoEnd);
					gotoEnd.write();
				} else this.assemble(null);
			}
		}
	}
}
