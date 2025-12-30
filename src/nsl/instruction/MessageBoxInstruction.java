/*
 * MessageBoxInstruction.java
 */

package nsl.instruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;
import nsl.statement.SwitchCaseStatement;

/**
 * The NSIS MessageBox instruction.
 *
 * @author Stuart
 */
public class MessageBoxInstruction extends JumpExpression {
	public static final String name = "MessageBox";
	private Expression options;
	private String[] optionsArray;
	private Expression body;
	private Expression silentButton;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public MessageBoxInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 1) throw new NslReturnValueException(name, 0, 1);

		ArrayList<Expression> paramsList = Expression.matchList();
		switch (paramsList.size()) {
			case 1:
				this.optionsArray = new String[] {"MB_OK"};
				this.body = paramsList.get(0);
				this.silentButton = null;
				break;
			case 2:
				this.options = paramsList.get(0);
				if (!ExpressionType.isString(this.options))
					throw new NslArgumentException(name, 1, ExpressionType.String);
				this.optionsArray = this.options.getStringValue().split("\\|");
				this.body = paramsList.get(1);
				this.silentButton = null;
				break;
			case 3:
				this.options = paramsList.get(0);
				if (!ExpressionType.isString(this.options))
					throw new NslArgumentException(name, 1, ExpressionType.String);
				this.optionsArray = this.options.getStringValue().split("\\|");
				this.body = paramsList.get(1);
				this.silentButton = paramsList.get(2);
				if (!ExpressionType.isString(this.silentButton))
					throw new NslArgumentException(name, 3, ExpressionType.String);
				break;
			default:
				throw new NslArgumentException(name, 1, 3, paramsList.size());
		}

		for (String option : this.optionsArray) {
			if (!option.equals("MB_OK")
					&& !option.equals("MB_OKCANCEL")
					&& !option.equals("MB_ABORTRETRYIGNORE")
					&& !option.equals("MB_RETRYCANCEL")
					&& !option.equals("MB_YESNO")
					&& !option.equals("MB_YESNOCANCEL")
					&& !option.equals("MB_ICONEXCLAMATION")
					&& !option.equals("MB_ICONINFORMATION")
					&& !option.equals("MB_ICONQUESTION")
					&& !option.equals("MB_ICONSTOP")
					&& !option.equals("MB_USERICON")
					&& !option.equals("MB_TOPMOST")
					&& !option.equals("MB_SETFOREGROUND")
					&& !option.equals("MB_RIGHT")
					&& !option.equals("MB_RTLREADING")
					&& !option.equals("MB_DEFBUTTON1")
					&& !option.equals("MB_DEFBUTTON2")
					&& !option.equals("MB_DEFBUTTON3")
					&& !option.equals("MB_DEFBUTTON4")) {
				throw new NslArgumentException(name, 1, "\"" + option + "\"");
			}
		}

		if (this.silentButton != null) {
			String value = this.silentButton.getStringValue();
			if (!isValidReturnValue(value)) {
				throw new NslArgumentException(name, 3, "\"" + value + "\"");
			}
		}

		// The negate switch.
		this.booleanValue = false;

		// Not a Boolean type unless optimise gets called successfully.
		this.type = ExpressionType.Other;
	}

	/**
	 * Determines if the given message box return value (such as IDOK) is valid.
	 *
	 * @param value the value to validate
	 * @return <code>true</code> if the value is valid
	 */
	private static boolean isValidReturnValue(String value) {
		return value.equals("IDABORT")
				|| value.equals("IDCANCEL")
				|| value.equals("IDIGNORE")
				|| value.equals("IDNO")
				|| value.equals("IDOK")
				|| value.equals("IDRETRY")
				|| value.equals("IDYES");
	}

	/**
	 * Writes the MessageBox instruction.
	 *
	 * @param append a string to append to the end
	 */
	private void writeMessageBox(String append) throws IOException {
		Expression varOrBody = AssembleExpression.getRegisterOrExpression(this.body);
		this.writeMessageBox(varOrBody.toString(), append);
		varOrBody.setInUse(false);
	}

	/**
	 * Writes the MessageBox instruction.
	 *
	 * @param messageBody the message to display
	 * @param append a string to append to the end
	 */
	private void writeMessageBox(String messageBody, String append) throws IOException {
		String line = name + " ";
		for (int i = 0; i < this.optionsArray.length; i++) {
			if (i == this.optionsArray.length - 1) line += this.optionsArray[i];
			else line += this.optionsArray[i] + '|';
		}
		line += " " + messageBody;
		if (this.silentButton != null) {
			if (this.silentButton != null) AssembleExpression.assembleIfRequired(this.silentButton);
			line += " /SD " + this.silentButton.getStringValue();
		}
		ScriptParser.writeLine(line + append);
	}

	/**
	 * Creates the string to append to the end of the message box instruction.
	 *
	 * @param gotoA the first go-to label
	 * @param gotoB the second go-to label
	 * @param returnChecks the return check values (such as IDOK)
	 * @return the append string
	 */
	private String createAppendStr(Label gotoA, Label gotoB, String[] returnChecks) {
		String append = "";

		/*
		 * At this point, this.stringValue is e.g. "IDYES" which is what we are
		 * checking the result of the MessageBox should be.
		 *
		 * MessageBox takes the form of, e.g.:
		 * MessageBox MB_YESNOCANCEL "text" IDYES gotoA IDNO gotoB
		 *
		 * Unfortunately NSIS only allows two Goto jumps on the end even if we have
		 * 3 buttons. Therefore we would need to add Goto gotoB on the next line to
		 * account for IDCANCEL.
		 *
		 * If gotoA or gotoB are relative jumps of 0 then we can omit that jump from
		 * the end of the MessageBox.
		 */

		// Can have a max of 2 jumps on a MessageBox but up to 3 buttons.
		if (returnChecks.length == 3) {
			// gotoA doesn't jump anywhere so find gotoB's return-check value and
			// ignore gotoA.
			if (gotoA.toString().equals("0")) {
				for (String value : returnChecks) {
					if (!value.equals(this.stringValue)) {
						if (!gotoB.toString().equals("0")) append += " " + value + " " + gotoB;
					}
				}
			}
			// gotoB doesn't jump anywhere so find gotoA's return-check value and
			// ignore gotoB.
			else if (gotoB.toString().equals("0")) {
				for (String value : returnChecks) {
					if (value.equals(this.stringValue)) {
						if (!gotoA.toString().equals("0")) append += " " + value + " " + gotoA;
					}
				}
			}
			// We need to use gotoA and gotoB but we're stuck with 2 Goto jumps on the
			// end of our MessageBox (due to NSIS) so we have to stick a Goto instr.
			// after our MessageBox instr.
			else {
				for (String value : returnChecks) {
					if (value.equals(this.stringValue)) {
						if (!gotoA.toString().equals("0")) append = " " + value + " " + gotoA;
						break;
					}
				}

				// We'll have to stick a Goto on the next line...
				append += "\r\nGoto " + gotoB;
			}
		} else {
			for (String value : returnChecks) {
				if (value.equals(this.stringValue)) {
					if (!gotoA.toString().equals("0")) append += " " + value + " " + gotoA;
				} else {
					if (!gotoB.toString().equals("0")) append += " " + value + " " + gotoB;
				}
			}
		}
		return append;
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		if (this.options != null) AssembleExpression.assembleIfRequired(this.options);
		if (this.thrownAwayAfterOptimise != null)
			AssembleExpression.assembleIfRequired(this.thrownAwayAfterOptimise);

		this.writeMessageBox("");
	}

	/**
	 * Assembles the source code.
	 *
	 * @param var the variable to assign the value to
	 */
	@Override
	public void assemble(Register var) throws IOException {
		String bodyNew;

		if (this.options != null) AssembleExpression.assembleIfRequired(this.options);
		if (this.thrownAwayAfterOptimise != null)
			AssembleExpression.assembleIfRequired(this.thrownAwayAfterOptimise);

		if (this.body instanceof AssembleExpression) {
			((AssembleExpression) this.body).assemble(var);
			bodyNew = var.toString();
		} else {
			bodyNew = this.body.toString();
		}

		Label gotoA = LabelList.getCurrent().getNext();
		Label gotoB = LabelList.getCurrent().getNext();

		// Switch labels around if ! operator was used.
		if (this.booleanValue) {
			Label gotoTemp = gotoA;
			gotoA = gotoB;
			gotoB = gotoTemp;
		}

		for (String option : this.optionsArray) {
			if (option.equals("MB_OK")) {
				if (this.stringValue == null) {
					this.writeMessageBox(bodyNew, "");
					ScriptParser.writeLine("StrCpy " + var + " IDOK");
				} else {
					this.writeMessageBox(bodyNew, "");
					if (this.booleanValue) ScriptParser.writeLine("StrCpy " + var + " true");
					else ScriptParser.writeLine("StrCpy " + var + " false");
				}
				break;
			}
			if (option.equals("MB_OKCANCEL")) {
				if (this.stringValue == null) {
					this.writeMessageBox(bodyNew, " IDCANCEL +3");
					ScriptParser.writeLine("StrCpy " + var + " IDOK");
					ScriptParser.writeLine("Goto +2");
					ScriptParser.writeLine("StrCpy " + var + " IDCANCEL");
				} else {
					this.writeMessageBox(
							bodyNew, createAppendStr(gotoA, gotoB, new String[] {"IDOK", "IDCANCEL"}));
					gotoA.write();
					ScriptParser.writeLine("StrCpy " + var + " true");
					ScriptParser.writeLine("Goto +2");
					gotoB.write();
					ScriptParser.writeLine("StrCpy " + var + " false");
				}
				break;
			}
			if (option.equals("MB_ABORTRETRYIGNORE")) {
				if (this.stringValue == null) {
					this.writeMessageBox(bodyNew, " IDRETRY +3 IDIGNORE +5");
					ScriptParser.writeLine("StrCpy " + var + " IDABORT");
					ScriptParser.writeLine("Goto +4");
					ScriptParser.writeLine("StrCpy " + var + " IDRETRY");
					ScriptParser.writeLine("Goto +2");
					ScriptParser.writeLine("StrCpy " + var + " IDIGNORE");
				} else {
					this.writeMessageBox(
							bodyNew,
							createAppendStr(gotoA, gotoB, new String[] {"IDABORT", "IDRETRY", "IDIGNORE"}));
					gotoA.write();
					ScriptParser.writeLine("StrCpy " + var + " true");
					ScriptParser.writeLine("Goto +2");
					gotoB.write();
					ScriptParser.writeLine("StrCpy " + var + " false");
				}
				break;
			}
			if (option.equals("MB_RETRYCANCEL")) {
				if (this.stringValue == null) {
					this.writeMessageBox(bodyNew, " IDCANCEL +3");
					ScriptParser.writeLine("StrCpy " + var + " IDRETRY");
					ScriptParser.writeLine("Goto +2");
					ScriptParser.writeLine("StrCpy " + var + " IDCANCEL");
				} else {
					this.writeMessageBox(
							bodyNew, createAppendStr(gotoA, gotoB, new String[] {"IDRETRY", "IDCANCEL"}));
					gotoA.write();
					ScriptParser.writeLine("StrCpy " + var + " true");
					ScriptParser.writeLine("Goto +2");
					gotoB.write();
					ScriptParser.writeLine("StrCpy " + var + " false");
				}
				break;
			}
			if (option.equals("MB_YESNO")) {
				if (this.stringValue == null) {
					this.writeMessageBox(bodyNew, " IDNO +3");
					ScriptParser.writeLine("StrCpy " + var + " IDYES");
					ScriptParser.writeLine("Goto +2");
					ScriptParser.writeLine("StrCpy " + var + " IDNO");
				} else {
					this.writeMessageBox(
							bodyNew, createAppendStr(gotoA, gotoB, new String[] {"IDYES", "IDNO"}));
					gotoA.write();
					ScriptParser.writeLine("StrCpy " + var + " true");
					ScriptParser.writeLine("Goto +2");
					gotoB.write();
					ScriptParser.writeLine("StrCpy " + var + " false");
				}
				break;
			}
			if (option.equals("MB_YESNOCANCEL")) {
				if (this.stringValue == null) {
					this.writeMessageBox(bodyNew, " IDNO +3 IDCANCEL +5");
					ScriptParser.writeLine("StrCpy " + var + " IDYES");
					ScriptParser.writeLine("Goto +4");
					ScriptParser.writeLine("StrCpy " + var + " IDNO");
					ScriptParser.writeLine("Goto +2");
					ScriptParser.writeLine("StrCpy " + var + " IDCANCEL");
				} else {
					this.writeMessageBox(
							bodyNew, createAppendStr(gotoA, gotoB, new String[] {"IDYES", "IDNO", "IDCANCEL"}));
					gotoA.write();
					ScriptParser.writeLine("StrCpy " + var + " true");
					ScriptParser.writeLine("Goto +2");
					gotoB.write();
					ScriptParser.writeLine("StrCpy " + var + " false");
				}
				break;
			}
		}
	}

	/**
	 * Assembles the source code.
	 *
	 * @param gotoA the first go-to label
	 * @param gotoB the second go-to label
	 */
	@Override
	public void assemble(Label gotoA, Label gotoB) throws IOException {
		if (this.stringValue == null)
			throw new NslException("\"" + name + "\" used with no return value check");

		// Switch labels around if ! operator was used.
		if (this.booleanValue) {
			Label gotoTemp = gotoA;
			gotoA = gotoB;
			gotoB = gotoTemp;
		}

		if (this.options != null) AssembleExpression.assembleIfRequired(this.options);
		if (this.thrownAwayAfterOptimise != null)
			AssembleExpression.assembleIfRequired(this.thrownAwayAfterOptimise);

		String append = "";
		for (String option : this.optionsArray) {
			if (option.equals("MB_OK")) {
				append = createAppendStr(gotoA, gotoB, new String[] {"IDOK"});
				break;
			}
			if (option.equals("MB_OKCANCEL")) {
				append = createAppendStr(gotoA, gotoB, new String[] {"IDOK", "IDCANCEL"});
				break;
			}
			if (option.equals("MB_ABORTRETRYIGNORE")) {
				append = createAppendStr(gotoA, gotoB, new String[] {"IDABORT", "IDRETRY", "IDIGNORE"});
				break;
			}
			if (option.equals("MB_RETRYCANCEL")) {
				append = createAppendStr(gotoA, gotoB, new String[] {"IDRETRY", "IDCANCEL"});
				break;
			}
			if (option.equals("MB_YESNO")) {
				append = createAppendStr(gotoA, gotoB, new String[] {"IDYES", "IDNO"});
				break;
			}
			if (option.equals("MB_YESNOCANCEL")) {
				append = createAppendStr(gotoA, gotoB, new String[] {"IDYES", "IDNO", "IDCANCEL"});
				break;
			}
		}

		this.writeMessageBox(append);
	}

	/**
	 * Assembles the source code.
	 *
	 * @param switchCases a list of {@link SwitchCaseStatement} in the switch statement statement that
	 *     this {@code JumpExpression} is being switched on.
	 */
	public void assemble(ArrayList<SwitchCaseStatement> switchCases) throws IOException {
		if (this.stringValue == null) {
			if (this.options != null) AssembleExpression.assembleIfRequired(this.options);
			if (this.thrownAwayAfterOptimise != null)
				AssembleExpression.assembleIfRequired(this.thrownAwayAfterOptimise);

			String append = "";
			for (SwitchCaseStatement caseStatement : switchCases) {
				append += " " + caseStatement.getMatch().getStringValue() + " " + caseStatement.getLabel();
			}

			this.writeMessageBox(append);
		} else {
			Label gotoA = null;
			Label gotoB = null;

			for (SwitchCaseStatement caseStatement : switchCases) {
				if (caseStatement.getMatch().getBooleanValue() == true) {
					if (gotoA == null) gotoA = caseStatement.getLabel();
				} else {
					if (gotoB == null) gotoB = caseStatement.getLabel();
				}
			}

			this.assemble(gotoA, gotoB);
		}
	}

	/**
	 * Checks whether the switch cases are valid for this type of {@code JumpExpression}.
	 *
	 * @param the line number of the switch statement
	 * @param switchCases a list of {@link SwitchCaseStatement} in the switch statement
	 */
	@Override
	public void checkSwitchCases(ArrayList<SwitchCaseStatement> switchCases, int switchLineNo) {
		if (this.stringValue == null && switchCases.size() > 2)
			throw new NslException(
					"Too many \"case\" values for \"switch\" statement (2 allowed)", switchLineNo);

		for (SwitchCaseStatement caseStatement : switchCases) {
			// Unless optimise() was called then stringValue will be null.
			if (this.stringValue == null) {
				if (!caseStatement.getMatch().getType().equals(ExpressionType.String)
								&& !caseStatement.getMatch().getType().equals(ExpressionType.StringSpecial)
						|| !isValidReturnValue(caseStatement.getMatch().getStringValue()))
					throw new NslException(
							"Invalid \"case\" value of " + caseStatement.getMatch(), caseStatement.getLineNo());
			}
			// optimise() was called and therefore we should check the switch case
			// values are Boolean (base implementation of checkSwitchCases does this).
			else {
				super.checkSwitchCases(switchCases, switchLineNo);
			}
		}
	}

	/**
	 * Attempts to optimise the jump expression.
	 *
	 * @param returnCheck the return check expression
	 * @param operator the operator being used
	 * @return <code>true</code> if the expression could be optimised
	 */
	@Override
	public boolean optimise(Expression returnCheck, String operator) {
		if (returnCheck.getType().equals(ExpressionType.String)) {
			this.thrownAwayAfterOptimise = returnCheck;
			this.type = ExpressionType.Boolean;
			this.booleanValue = operator.equals("!=");
			this.stringValue = returnCheck.getStringValue();
			return true;
		}
		return false;
	}
}
