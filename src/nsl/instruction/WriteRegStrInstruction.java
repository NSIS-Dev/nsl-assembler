/*
 * WriteRegStrInstruction.java
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
public class WriteRegStrInstruction extends AssembleExpression {
	public static final String name = "WriteRegStr";
	private final Expression rootKey;
	private final Expression subKey;
	private final Expression valueName;
	private final Expression value;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public WriteRegStrInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 4) throw new NslArgumentException(name, 4);

		this.rootKey = paramsList.get(0);
		if (!ExpressionType.isString(this.rootKey))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		this.subKey = paramsList.get(1);
		this.valueName = paramsList.get(2);
		this.value = paramsList.get(3);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.rootKey);
		Expression varOrSubKey = AssembleExpression.getRegisterOrExpression(this.subKey);
		Expression varOrValueName = AssembleExpression.getRegisterOrExpression(this.valueName);
		Expression varOrValue = AssembleExpression.getRegisterOrExpression(this.value);
		ScriptParser.writeLine(
				name + " " + this.rootKey + " " + varOrSubKey + " " + varOrValueName + " " + varOrValue);
		varOrSubKey.setInUse(false);
		varOrValueName.setInUse(false);
		varOrValue.setInUse(false);
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
