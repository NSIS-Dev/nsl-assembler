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
public class InstallDirRegKeyInstruction extends AssembleExpression {
	public static final String name = "InstallDirRegKey";
	private final Expression rootKey;
	private final Expression subKey;
	private final Expression keyName;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public InstallDirRegKeyInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 3) throw new NslArgumentException(name, 3);

		this.rootKey = paramsList.get(0);
		if (!ExpressionType.isString(this.rootKey))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		this.subKey = paramsList.get(1);
		if (!ExpressionType.isString(this.subKey))
			throw new NslArgumentException(name, 2, ExpressionType.String);

		this.keyName = paramsList.get(2);
		if (!ExpressionType.isString(this.keyName))
			throw new NslArgumentException(name, 3, ExpressionType.String);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.rootKey);
		Expression varOrSubKey = AssembleExpression.getRegisterOrExpression(this.subKey);
		Expression varOrKeyName = AssembleExpression.getRegisterOrExpression(this.keyName);
		ScriptParser.writeLine(name + " " + this.rootKey + " " + varOrSubKey + " " + varOrKeyName);
		varOrSubKey.setInUse(false);
		varOrKeyName.setInUse(false);
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
