/*
 * SubCaptionInstruction.java
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
public class SubCaptionInstruction extends AssembleExpression {
	public static final String name = "SubCaption";
	private final Expression pageNumber;
	private final Expression text;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public SubCaptionInstruction(int returns) {
		if (!ScriptParser.inGlobalContext() && !PageExInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Global, NslContext.PageEx), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount != 2) throw new NslArgumentException(name, 2);

		this.pageNumber = paramsList.get(0);
		if (!ExpressionType.isInteger(this.pageNumber))
			throw new NslArgumentException(name, 1, ExpressionType.Integer);

		this.text = paramsList.get(1);
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.pageNumber);
		Expression varOrText = AssembleExpression.getRegisterOrExpression(this.text);
		ScriptParser.writeLine(name + " " + this.pageNumber + " " + varOrText);
		varOrText.setInUse(false);
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
