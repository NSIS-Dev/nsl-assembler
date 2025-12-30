/*
 * ChangeUIInstruction.java
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
public class ChangeUIInstruction extends AssembleExpression {
	public static final String name = "ChangeUI";
	private final Expression dialog;
	private final Expression uiFile;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public ChangeUIInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount != 1) throw new NslArgumentException(name, 1);

		this.dialog = paramsList.get(0);
		if (!ExpressionType.isString(this.dialog))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		this.uiFile = paramsList.get(1);
		if (!ExpressionType.isString(this.uiFile))
			throw new NslArgumentException(name, 2, ExpressionType.String);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.dialog);
		AssembleExpression.assembleIfRequired(this.uiFile);
		ScriptParser.writeLine(name + " " + this.dialog + " " + this.uiFile);
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
