/*
 * SetFontInstruction.java
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
public class SetFontInstruction extends AssembleExpression {
	public static final String name = "SetFont";
	private final Expression fontFace;
	private final Expression fontSize;
	private final Expression langId;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public SetFontInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 2 || paramsCount > 3) throw new NslArgumentException(name, 1);

		this.fontFace = paramsList.get(0);
		if (!ExpressionType.isString(this.fontFace))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		this.fontSize = paramsList.get(1);
		if (!ExpressionType.isInteger(this.fontSize))
			throw new NslArgumentException(name, 2, ExpressionType.Integer);

		if (paramsCount > 1) {
			this.langId = paramsList.get(2);
			if (!ExpressionType.isInteger(this.langId))
				throw new NslArgumentException(name, 3, ExpressionType.Integer);
		} else {
			this.langId = null;
		}
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.fontFace);
		AssembleExpression.assembleIfRequired(this.fontSize);
		String write = name + " " + this.fontFace + " " + this.fontSize;

		if (this.langId != null) {
			AssembleExpression.assembleIfRequired(this.langId);
			write += " " + this.langId;
		}

		ScriptParser.writeLine(write);
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
