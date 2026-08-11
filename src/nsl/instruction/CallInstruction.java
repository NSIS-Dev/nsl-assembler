/*
 * CallInstruction.java
 */

package nsl.instruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;

/**
 * Calls a function by name or through an address held in a variable. Ordinary calls are written
 * <code>someFunction()</code> and go through {@link nsl.expression.FunctionCallExpression}, which
 * also passes arguments and return values; this is the indirect form, which does neither.
 *
 * @author Stuart
 */
public class CallInstruction extends AssembleExpression {
	public static final String name = "Call";
	private final Expression target;
	private final int lineNo;
	private final boolean inUninstaller;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public CallInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		if (paramsList.size() != 1) throw new NslArgumentException(name, 1);

		this.target = paramsList.get(0);
		if (!ExpressionType.isString(this.target) && !ExpressionType.isRegister(this.target))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		this.lineNo = ScriptParser.tokenizer.lineno();
		// Recorded here rather than read in assemble(): the flag tracks where the
		// parser is, and by the time anything is written it has long been reset.
		this.inUninstaller = Scope.inUninstaller();
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		// A variable holds an address and is written through as-is; a name has to be
		// resolved, because nsL mangles the names of functions that take or return
		// anything. Resolved here so that a function defined further down can be called.
		if (ExpressionType.isRegister(this.target)) {
			ScriptParser.writeLine(name + " " + this.target);
			return;
		}
		ScriptParser.writeLine(
				name
						+ " "
						+ FunctionInfo.resolveNsisName(
								this.target.getStringValue(), this.inUninstaller, this.lineNo));
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
