/*
 * SwitchStatement.java
 */

package nsl.statement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;

/**
 * Represents a switch statement.
 *
 * @author Stuart
 */
public class SwitchStatement extends Statement {
	private final Expression switchExpression;
	private final ArrayList<Statement> statementList;
	private final ArrayList<SwitchCaseStatement> casesList;
	private SwitchDefaultCaseStatement defaultCase;

	/** Class constructor. */
	public SwitchStatement() {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), "switch");

		int lineNo = ScriptParser.tokenizer.lineno();
		ScriptParser.tokenizer.matchOrDie('(');
		this.switchExpression = Expression.matchComplex();
		ScriptParser.tokenizer.matchOrDie(')');
		ScriptParser.tokenizer.matchOrDie('{');

		// Set non-null values so that the block statement can contain break statements.
		CodeInfo.getCurrent().setBreakLabel(RelativeJump.Zero);

		this.statementList = new ArrayList<Statement>();
		this.casesList = new ArrayList<SwitchCaseStatement>();
		this.defaultCase = null;

		// Get the statements including case statements.
		while (true) {
			if (ScriptParser.tokenizer.match("case")) {
				Statement statement = new SwitchCaseStatement();
				if (this.defaultCase != null)
					throw new NslException(
							"The \"default\" case in a \"switch\" statement must be the last case", true);
				this.casesList.add((SwitchCaseStatement) statement);
				this.statementList.add(statement);
			} else if (ScriptParser.tokenizer.match("default")) {
				this.defaultCase = new SwitchDefaultCaseStatement();
				this.statementList.add(this.defaultCase);
			} else {
				Statement statement = Statement.match();
				if (statement == null) break;
				this.statementList.add(statement);
			}
		}

		// No cases?
		if (this.casesList.isEmpty())
			throw new NslException(
					"A \"switch\" statement must have at least one \"case\" statement", true);

		// Validate switch cases for jump instructions.
		if (this.switchExpression instanceof JumpExpression)
			((JumpExpression) this.switchExpression).checkSwitchCases(this.casesList, lineNo);

		// Check the last statement is a break statement.
		boolean noBreak = true;
		if (!this.statementList.isEmpty()) {
			Statement last = this.statementList.get(this.statementList.size() - 1);
			if (last instanceof BlockStatement) last = ((BlockStatement) last).getLast();
			if (last instanceof BreakStatement) noBreak = false;
		}
		if (noBreak)
			throw new NslException("A \"switch\" statement must end with a \"break\" statement", true);

		CodeInfo.getCurrent().setBreakLabel(null);

		ScriptParser.tokenizer.matchOrDie('}');
	}

	/**
	 * Assembles the source code.
	 *
	 * @throws IOException
	 */
	@Override
	public void assemble() throws IOException {
		// Do not assemble anything if there are no cases!
		if (this.casesList.isEmpty()) return;

		// Give each case a label.
		for (SwitchCaseStatement statement : this.casesList)
			statement.setLabel(LabelList.getCurrent().getNext());
		if (this.defaultCase != null) this.defaultCase.setLabel(LabelList.getCurrent().getNext());

		Label gotoEnd = LabelList.getCurrent().getNext();
		Label gotoStart = LabelList.getCurrent().getNext();

		// Go to the jump table which is assembled after the switch case labels and
		// statements.
		ScriptParser.writeLine("Goto " + gotoStart);

		// Using "break;" inside a switch jumps to the end.
		Label parentBreak = CodeInfo.getCurrent().setBreakLabel(gotoEnd);

		// Assemble all the statements inside the switch { }. This includes the
		// case labels.
		for (Statement statement : this.statementList) statement.assemble();

		// Restore the parent break label.
		CodeInfo.getCurrent().setBreakLabel(parentBreak);

		// Label at the top of the jump table.
		gotoStart.write();

		// Jump instructions can jump directly to the case labels.
		if (this.switchExpression instanceof JumpExpression) {
			((JumpExpression) this.switchExpression).assemble(this.casesList);
		}
		// Other expressions we just assemble them if required and compare their
		// result.
		else {
			Expression varOrSwitchExpression =
					AssembleExpression.getRegisterOrExpression(this.switchExpression);
			for (SwitchCaseStatement caseStatement : this.casesList) {
				// Type is an integer; use IntCmp.
				if (caseStatement.getMatch().getType().equals(ExpressionType.Integer)) {
					ScriptParser.writeLine(
							String.format(
									"IntCmp %s %s %s",
									varOrSwitchExpression, caseStatement.getMatch(), caseStatement.getLabel()));
				}
				// Type is a string; use StrCmp or StrCmpS (if special `` quotes were
				// used).
				else {
					ScriptParser.writeLine(
							String.format(
									"StrCmp%s %s %s %s",
									caseStatement.getMatch().getType().equals(ExpressionType.StringSpecial)
											? "S"
											: "",
									varOrSwitchExpression,
									caseStatement.getMatch(),
									caseStatement.getLabel()));
				}
			}
			varOrSwitchExpression.setInUse(false);
		}

		// Default case jump.
		if (this.defaultCase != null) ScriptParser.writeLine("Goto " + this.defaultCase.getLabel());

		gotoEnd.write();
	}
}
