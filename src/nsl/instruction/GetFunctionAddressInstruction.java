/*
 * GetFunctionAddressInstruction.java
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
public class GetFunctionAddressInstruction extends AssembleExpression {
	public static final String name = "GetFunctionAddress";
	private final Expression functionName;
	private final int lineNo;
	private final boolean inUninstaller;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public GetFunctionAddressInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns != 1) throw new NslReturnValueException(name, 1);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 1) throw new NslArgumentException(name, 1);

		this.functionName = paramsList.get(0);
		if (!ExpressionType.isString(this.functionName))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		this.lineNo = ScriptParser.tokenizer.lineno();
		// Recorded here rather than read in assemble(): the flag tracks where the
		// parser is, and by the time anything is written it has long been reset.
		this.inUninstaller = Scope.inUninstaller();
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
		// Resolved here rather than in the constructor so that a function defined
		// further down the script can have its address taken.
		ScriptParser.writeLine(
				name
						+ " "
						+ var
						+ " "
						+ FunctionInfo.resolveNsisName(
								this.functionName.getStringValue(), this.inUninstaller, this.lineNo));
	}
}
