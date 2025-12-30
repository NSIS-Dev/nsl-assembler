/*
 * DirTextInstruction.java
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
public class DirTextInstruction extends AssembleExpression {
	public static final String name = "DirText";
	private final Expression text;
	private final Expression subText;
	private final Expression browseButtonText;
	private final Expression browseDlgText;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public DirTextInstruction(int returns) {
		if (!ScriptParser.inGlobalContext() && !PageExInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Global, NslContext.PageEx), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 1 || paramsCount > 4) throw new NslArgumentException(name, 1, 4);

		this.text = paramsList.get(0);

		if (paramsCount > 1) {
			this.subText = paramsList.get(1);

			if (paramsCount > 2) {
				this.browseButtonText = paramsList.get(2);

				if (paramsCount > 3) {
					this.browseDlgText = paramsList.get(3);
				} else {
					this.browseDlgText = null;
				}
			} else {
				this.browseButtonText = null;
				this.browseDlgText = null;
			}
		} else {
			this.subText = null;
			this.browseButtonText = null;
			this.browseDlgText = null;
		}
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		Expression varOrText = AssembleExpression.getRegisterOrExpression(this.text);
		String write = name + " " + varOrText;

		if (this.subText != null) {
			Expression varOrSubText = AssembleExpression.getRegisterOrExpression(this.subText);
			write += " " + varOrSubText;

			if (this.browseButtonText != null) {
				Expression varOrBrowseButtonText =
						AssembleExpression.getRegisterOrExpression(this.browseButtonText);
				write += " " + varOrBrowseButtonText;

				if (this.browseDlgText != null) {
					Expression varOrBrowseDlgText =
							AssembleExpression.getRegisterOrExpression(this.browseDlgText);
					write += " " + varOrBrowseDlgText;
					varOrBrowseDlgText.setInUse(false);
				}

				varOrBrowseButtonText.setInUse(false);
			}

			varOrSubText.setInUse(false);
		}

		ScriptParser.writeLine(write);
		varOrText.setInUse(false);
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
