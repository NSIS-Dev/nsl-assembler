/*
 * MiscButtonTextInstruction.java
 */

package nsl.instruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;

/**
 * @author Stuart
 */
public class MiscButtonTextInstruction extends AssembleExpression {
	public static final String name = "MiscButtonText";
	private final Expression backButtonText;
	private final Expression nextButtonText;
	private final Expression cancelButtonText;
	private final Expression closeButtonText;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public MiscButtonTextInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 1 || paramsCount > 4) throw new NslArgumentException(name, 1, 4);

		this.backButtonText = paramsList.get(0);

		if (paramsCount > 1) {
			this.nextButtonText = paramsList.get(1);

			if (paramsCount > 2) {
				this.cancelButtonText = paramsList.get(2);

				if (paramsCount > 3) this.closeButtonText = paramsList.get(3);
				else this.closeButtonText = null;
			} else {
				this.cancelButtonText = null;
				this.closeButtonText = null;
			}
		} else {
			this.nextButtonText = null;
			this.cancelButtonText = null;
			this.closeButtonText = null;
		}
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		Expression varOrbackButtonText =
				AssembleExpression.getRegisterOrExpression(this.backButtonText);
		if (this.nextButtonText != null) {
			Expression varOrNextButtonText =
					AssembleExpression.getRegisterOrExpression(this.nextButtonText);
			if (this.cancelButtonText != null) {
				Expression varOrCancelButtonText =
						AssembleExpression.getRegisterOrExpression(this.cancelButtonText);
				if (this.closeButtonText != null) {
					Expression varOrCloseButtonText =
							AssembleExpression.getRegisterOrExpression(this.closeButtonText);
					ScriptParser.writeLine(
							name
									+ " "
									+ varOrbackButtonText
									+ " "
									+ varOrNextButtonText
									+ " "
									+ varOrCancelButtonText
									+ " "
									+ varOrCloseButtonText);
					this.closeButtonText.setInUse(false);
				} else {
					ScriptParser.writeLine(
							name
									+ " "
									+ varOrbackButtonText
									+ " "
									+ varOrNextButtonText
									+ " "
									+ varOrCancelButtonText);
				}
			} else {
				ScriptParser.writeLine(name + " " + varOrbackButtonText + " " + varOrNextButtonText);
			}
			varOrNextButtonText.setInUse(false);
		} else {
			ScriptParser.writeLine(name + " " + varOrbackButtonText);
		}
		varOrbackButtonText.setInUse(false);
	}

	/**
	 * Assembles the source code.
	 *
	 * @param var the variable to assign the value to
	 */
	@Override
	public void assemble(Register var) throws IOException {
		throw new UnsupportedOperationException("Not supported.");
	}
}
