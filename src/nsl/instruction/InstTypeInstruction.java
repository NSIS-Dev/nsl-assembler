/*
 * InstTypeInstruction.java
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
public class InstTypeInstruction extends AssembleExpression {
	public static final String name = "InstType";
	private final Expression value1;
	private final Expression value2;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public InstTypeInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 1 || paramsCount > 2) throw new NslArgumentException(name, 1, 2);

		this.value1 = paramsList.get(0);
		if (!ExpressionType.isBoolean(this.value1) && !ExpressionType.isString(this.value1))
			throw new NslArgumentException(name, 1, ExpressionType.Boolean, ExpressionType.String);

		if (paramsCount > 1) {
			this.value2 = paramsList.get(1);
			if (!ExpressionType.isString(this.value2))
				throw new NslArgumentException(name, 2, ExpressionType.String);
		} else this.value2 = null;
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.value1);

		if (this.value1.getType().equals(ExpressionType.Boolean)) {
			/*
			 * InstType(true)
			 * InstType /NOCUSTOM
			 */
			if (this.value1.getBooleanValue() == true) {
				if (this.value2 != null) AssembleExpression.assembleIfRequired(this.value2);
				ScriptParser.writeLine(name + " /NOCUSTOM");
			} else {
				/*
				 * InstType(false)
				 * InstType /COMPONENTSONLYONCUSTOM
				 */
				if (this.value2 == null) {
					ScriptParser.writeLine(name + " /COMPONENTSONLYONCUSTOM");
				}
				/*
				 * InstType(false, "str")
				 * InstType /CUSTOMSTRING=str
				 */
				else {
					Expression varOrValue = AssembleExpression.getRegisterOrExpression(this.value2);
					ScriptParser.writeLine(name + " \"/CUSTOMSTRING=" + varOrValue.toString(true) + "\"");
					varOrValue.setInUse(true);
				}
			}
		}
		/*
		 * InstType("install_type_name")
		 * InstType install_type_name
		 */
		else {
			if (this.value2 != null) AssembleExpression.assembleIfRequired(this.value2);
			ScriptParser.writeLine(name + " " + this.value1);
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
