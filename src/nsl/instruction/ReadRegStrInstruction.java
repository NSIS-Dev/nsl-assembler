/*
 * ReadRegStrInstruction.java
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
public class ReadRegStrInstruction extends AssembleExpression {
	public static final String name = "ReadRegStr";
	private final Expression rootKey;
	private final Expression subKey;
	private final Expression keyName;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public ReadRegStrInstruction(int returns) {
		if (PageExInfo.in())
			throw new NslContextException(
					EnumSet.of(NslContext.Section, NslContext.Function, NslContext.Global), name);
		if (returns != 1) throw new NslReturnValueException(name, 1);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 3) throw new NslArgumentException(name, 3);

		this.rootKey = paramsList.get(0);
		if (!ExpressionType.isString(this.rootKey))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		this.subKey = paramsList.get(1);
		this.keyName = paramsList.get(2);
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
		AssembleExpression.assembleIfRequired(this.rootKey);
		Expression varOrSubKey = AssembleExpression.getRegisterOrExpression(this.subKey);
		Expression varOrKeyName = AssembleExpression.getRegisterOrExpression(this.keyName);
		ScriptParser.writeLine(
				name + " " + var + " " + this.rootKey + " " + varOrSubKey + " " + varOrKeyName);
		varOrSubKey.setInUse(false);
		varOrKeyName.setInUse(false);
	}
}
