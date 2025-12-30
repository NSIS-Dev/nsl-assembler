/*
 * DoStatement.java
 */

package nsl.statement;

import java.io.IOException;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;

/**
 * @author Stuart
 */
public class DoStatement extends Statement {
	private final Expression booleanExpression;
	private final BlockStatement blockStatement;

	/** Class constructor. */
	public DoStatement() {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), "do");

		// Set non-null values so that the block statement can contain break or continue statements.
		CodeInfo.getCurrent().setBreakLabel(RelativeJump.Zero);
		CodeInfo.getCurrent().setContinueLabel(RelativeJump.Zero);

		this.blockStatement = new BlockStatement();

		CodeInfo.getCurrent().setBreakLabel(null);
		CodeInfo.getCurrent().setContinueLabel(null);

		ScriptParser.tokenizer.matchOrDie("while");
		ScriptParser.tokenizer.matchOrDie('(');
		this.booleanExpression = Expression.matchComplex();
		if (!this.booleanExpression.getType().equals(ExpressionType.Boolean))
			throw new NslException(
					"A \"do\" statement requires a Boolean expression for its \"while\"", true);
		ScriptParser.tokenizer.matchOrDie(')');
		ScriptParser.tokenizer.matchEolOrDie();
	}

	/**
	 * Assembles the source code.
	 *
	 * @throws IOException
	 * @throws NslException
	 */
	@Override
	public void assemble() throws IOException {
		Label gotoLoop = LabelList.getCurrent().getNext();
		Label gotoEnd = LabelList.getCurrent().getNext();

		gotoLoop.write();

		gotoLoop.setNotUsed(true);
		gotoEnd.setNotUsed(true);

		Label parentBreak = CodeInfo.getCurrent().setBreakLabel(gotoEnd);
		Label parentContinue = CodeInfo.getCurrent().setContinueLabel(gotoLoop);

		this.blockStatement.assemble();

		CodeInfo.getCurrent().setBreakLabel(parentBreak);
		CodeInfo.getCurrent().setContinueLabel(parentContinue);

		if (this.booleanExpression.isLiteral() && this.booleanExpression.getBooleanValue() == true) {
			ScriptParser.writeLine("Goto " + gotoLoop);
		} else if (this.booleanExpression instanceof ConditionalExpression) {
			((ConditionalExpression) this.booleanExpression).assemble(gotoLoop, gotoEnd);
		} else if (gotoLoop.isNotUsed()) {
			// Prevent makensis warning if loop label not used.
			ScriptParser.writeLine("StrCmp \"\" \"\" 0 " + gotoLoop);
		}

		gotoEnd.write();
	}
}
