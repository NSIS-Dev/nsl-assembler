/*
 * LicenseLangStringInstruction.java
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
public class LicenseLangStringInstruction extends AssembleExpression {
	public static final String name = "LicenseLangString";
	private final Expression langString;
	private final Expression langId;
	private final Expression file;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public LicenseLangStringInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 3) throw new NslArgumentException(name, 3);

		this.langString = paramsList.get(0);
		if (!ExpressionType.isString(this.langString))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		this.langId = paramsList.get(1);
		if (!ExpressionType.isInteger(this.langId))
			throw new NslArgumentException(name, 2, ExpressionType.Integer);

		this.file = paramsList.get(2);
		if (!ExpressionType.isString(this.file))
			throw new NslArgumentException(name, 3, ExpressionType.String);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.langString);
		AssembleExpression.assembleIfRequired(this.langId);
		AssembleExpression.assembleIfRequired(this.file);
		ScriptParser.writeLine(name + " " + this.langString + " " + this.langId + " " + this.file);
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
