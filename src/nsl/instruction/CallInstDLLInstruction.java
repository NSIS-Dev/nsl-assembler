/*
 * CallInstDLLInstruction.java
 */

package nsl.instruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;

/**
 * @author Jan
 */
public class CallInstDLLInstruction extends AssembleExpression {
	public static final String name = "CallInstDLL";
	private final Expression dllPath;
	private final Expression function;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public CallInstDLLInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 2) throw new NslArgumentException(name, 2);

		this.dllPath = paramsList.get(0);
		this.function = paramsList.get(1);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		Expression varOrDllPath = AssembleExpression.getRegisterOrExpression(this.dllPath);
		Expression varOrFunction = AssembleExpression.getRegisterOrExpression(this.function);
		ScriptParser.writeLine(name + " " + varOrDllPath + " " + varOrFunction);
		varOrDllPath.setInUse(false);
		varOrFunction.setInUse(false);
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
