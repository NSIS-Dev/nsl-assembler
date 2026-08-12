/*
 * SetCtlColorsInstruction.java
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
public class SetCtlColorsInstruction extends AssembleExpression {
	public static final String name = "SetCtlColors";
	private final Expression hWnd;
	private final Expression textColor;
	private final Expression bgColor;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public SetCtlColorsInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 2 || paramsCount > 3) throw new NslArgumentException(name, 2, 3);

		this.hWnd = paramsList.get(0);

		this.textColor = paramsList.get(1);
		if (!ExpressionType.isString(this.textColor))
			throw new NslArgumentException(name, 2, ExpressionType.String);

		if (paramsCount > 2) {
			this.bgColor = paramsList.get(2);
			if (!ExpressionType.isString(this.bgColor))
				throw new NslArgumentException(name, 3, ExpressionType.String);
		} else this.bgColor = null;
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		Expression varOrHWnd = AssembleExpression.getRegisterOrExpression(this.hWnd);

		AssembleExpression.assembleIfRequired(this.textColor);
		String write = name + " " + varOrHWnd + " " + this.textColor;

		if (this.bgColor != null) {
			AssembleExpression.assembleIfRequired(this.bgColor);
			write += " " + this.bgColor;
		}

		ScriptParser.writeLine(write);
		varOrHWnd.setInUse(false);
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
