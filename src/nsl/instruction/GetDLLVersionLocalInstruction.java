/*
 * GetDLLVersionLocalInstruction.java
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
public class GetDLLVersionLocalInstruction extends MultipleReturnValueAssembleExpression {
	public static final String name = "GetDLLVersionLocal";
	private final Expression file;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public GetDLLVersionLocalInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns != 2) throw new NslReturnValueException(name, 2);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 1) throw new NslArgumentException(name, 1);

		this.file = paramsList.get(0);
		if (!ExpressionType.isString(this.file))
			throw new NslArgumentException(name, 1, ExpressionType.String);
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
		throw new UnsupportedOperationException("Not supported.");
	}

	/**
	 * Assembles the source code.
	 *
	 * @param vars the variables to assign the values to
	 */
	@Override
	public void assemble(ArrayList<Register> vars) throws IOException {
		Expression varOrFile = AssembleExpression.getRegisterOrExpression(this.file);
		ScriptParser.writeLine(name + " " + varOrFile + " " + vars.get(0) + " " + vars.get(1));
		varOrFile.setInUse(false);
	}
}
