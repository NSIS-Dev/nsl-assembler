/*
 * AddBrandingImageInstruction.java
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
public class AddBrandingImageInstruction extends AssembleExpression {
	public static final String name = "AddBrandingImage";
	private final Expression position;
	private final Expression size;
	private final Expression padding;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public AddBrandingImageInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 2 || paramsCount > 3) throw new NslArgumentException(name, 2, 3);

		this.position = paramsList.get(0);
		if (!ExpressionType.isString(this.position))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		this.size = paramsList.get(1);
		if (!ExpressionType.isString(this.size))
			throw new NslArgumentException(name, 2, ExpressionType.Integer);

		if (paramsCount > 2) {
			this.padding = paramsList.get(2);
			if (!ExpressionType.isString(this.padding))
				throw new NslArgumentException(name, 3, ExpressionType.Integer);
		} else this.padding = null;
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.position);
		AssembleExpression.assembleIfRequired(this.size);
		if (this.padding == null) {
			ScriptParser.writeLine(name + " " + this.position + " " + this.size);
		} else {
			AssembleExpression.assembleIfRequired(this.padding);
			ScriptParser.writeLine(name + " " + this.position + " " + this.size + " " + this.padding);
		}
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
