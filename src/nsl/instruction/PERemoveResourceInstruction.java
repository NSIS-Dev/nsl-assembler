/*
 * PERemoveResourceInstruction.java
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
public class PERemoveResourceInstruction extends AssembleExpression {
	public static final String name = "PERemoveResource";

	private final Expression resourceType;
	private final Expression resourceName;
	private final Expression resourceLanguage;
	private final Expression noErrors;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public PERemoveResourceInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 3 || paramsCount > 4) throw new NslArgumentException(name, 3, 4);

		// A resource type or name is either an integer id or a string name, so
		// neither can be checked beyond being one of the two.
		this.resourceType = paramsList.get(0);
		this.resourceName = paramsList.get(1);

		// The language is an id or the "ALL" keyword.
		this.resourceLanguage = paramsList.get(2);

		// /NOERRORS is on or off, so it is a Boolean, as /REBOOTOK is on Delete.
		if (paramsCount > 3) {
			this.noErrors = paramsList.get(3);
			if (!ExpressionType.isBoolean(this.noErrors))
				throw new NslArgumentException(name, 4, ExpressionType.Boolean);
		} else this.noErrors = null;
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		String write = name + " ";

		if (this.noErrors != null) {
			AssembleExpression.assembleIfRequired(this.noErrors);
			if (this.noErrors.getBooleanValue()) write += "/NOERRORS ";
		}

		AssembleExpression.assembleIfRequired(this.resourceType);
		AssembleExpression.assembleIfRequired(this.resourceName);
		AssembleExpression.assembleIfRequired(this.resourceLanguage);

		ScriptParser.writeLine(
				write + this.resourceType + " " + this.resourceName + " " + this.resourceLanguage);
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
