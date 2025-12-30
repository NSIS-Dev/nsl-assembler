/*
 * LicenseBkColorInstruction.java
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
public class LicenseBkColorInstruction extends AssembleExpression {
	public static final String name = "LicenseBkColor";
	private final Expression value;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public LicenseBkColorInstruction(int returns) {
		if (!ScriptParser.inGlobalContext() && !PageExInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Global, NslContext.PageEx), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 1) throw new NslArgumentException(name, 1);

		this.value = paramsList.get(0);
		if (!ExpressionType.isBoolean(this.value) && !ExpressionType.isString(this.value))
			throw new NslArgumentException(name, 1, ExpressionType.Boolean, ExpressionType.String);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.value);
		if (this.value.getType().equals(ExpressionType.Boolean)) {
			if (this.value.getBooleanValue() == true) ScriptParser.writeLine(name + " /gray");
			else ScriptParser.writeLine(name + " /windows");
		} else {
			ScriptParser.writeLine(name + " " + this.value);
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
