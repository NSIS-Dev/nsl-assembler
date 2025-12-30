/*
 * OutFileInstruction.java
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
public class OutFileInstruction extends AssembleExpression {
	public static final String name = "OutFile";
	private final Expression outFile;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public OutFileInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 1) throw new NslArgumentException(name, 1);

		this.outFile = paramsList.get(0);
		if (!ExpressionType.isString(this.outFile))
			throw new NslArgumentException(name, 1, ExpressionType.String);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.outFile);
		ScriptParser.writeLine(name + " " + this.outFile);
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
