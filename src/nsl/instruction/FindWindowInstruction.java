/*
 * FindWindowInstruction.java
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
public class FindWindowInstruction extends AssembleExpression {
	public static final String name = "FindWindow";
	private final Expression windowClass;
	private final Expression windowTitle;
	private final Expression windowParent;
	private final Expression childAfter;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public FindWindowInstruction(int returns) {
		if (PageExInfo.in())
			throw new NslContextException(
					EnumSet.of(NslContext.Section, NslContext.Function, NslContext.Global), name);
		if (returns != 1) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 1 || paramsCount > 4) throw new NslArgumentException(name, 1, 4);

		this.windowClass = paramsList.get(0);

		if (paramsCount > 1) {
			this.windowTitle = paramsList.get(1);

			if (paramsCount > 2) {
				this.windowParent = paramsList.get(2);

				if (paramsCount > 3) {
					this.childAfter = paramsList.get(3);
				} else {
					this.childAfter = null;
				}
			} else {
				this.windowParent = null;
				this.childAfter = null;
			}
		} else {
			this.windowTitle = null;
			this.windowParent = null;
			this.childAfter = null;
		}
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
		Expression varOrWindowClass = AssembleExpression.getRegisterOrExpression(this.windowClass);
		String write = name + " " + var + " " + varOrWindowClass;

		if (this.windowTitle != null) {
			Expression varOrWindowTitle = AssembleExpression.getRegisterOrExpression(this.windowTitle);
			write += " " + varOrWindowTitle;

			if (this.windowParent != null) {
				Expression varOrWindowParent =
						AssembleExpression.getRegisterOrExpression(this.windowParent);
				write += " " + varOrWindowParent;

				if (this.childAfter != null) {
					Expression varOrChildAfter = AssembleExpression.getRegisterOrExpression(this.childAfter);
					write += " " + varOrChildAfter;
					varOrChildAfter.setInUse(false);
				}

				varOrWindowParent.setInUse(false);
			}

			varOrWindowTitle.setInUse(false);
		}

		ScriptParser.writeLine(write);
		varOrWindowClass.setInUse(false);
	}
}
