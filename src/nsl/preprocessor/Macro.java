/*
 * Macro.java
 */

package nsl.preprocessor;

import java.io.StringReader;
import java.util.ArrayList;
import nsl.*;
import nsl.expression.*;
import nsl.statement.*;

/**
 * Describes a macro.
 *
 * @author Stuart
 */
public class Macro {
	private final String name;
	private final String[] params;
	private final int paramCount;
	private final int lineNo;
	private final String contents;

	/**
	 * Class constructor.
	 *
	 * @param name the macro name
	 * @param params the macro parameters
	 * @param lineNo the line that the macro was defined on
	 * @param contents the contents of the macro
	 */
	public Macro(String name, String[] params, int lineNo, String contents) {
		this.name = name;
		this.params = params;
		this.paramCount = params.length;
		this.lineNo = lineNo;
		this.contents = contents;
	}

	/**
	 * Gets the macro name.
	 *
	 * @return the macro name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the macro parameters.
	 *
	 * @return the macro parameters
	 */
	public String[] getParams() {
		return this.params;
	}

	/**
	 * Gets the number of macro parameters.
	 *
	 * @return the number of macro parameters
	 */
	public int getParamCount() {
		return this.paramCount;
	}

	/**
	 * Gets the line that the macro was defined on.
	 *
	 * @return the line that the macro was defined on
	 */
	public int getLineNo() {
		return this.lineNo;
	}

	/**
	 * Gets the contents of the macro.
	 *
	 * @return the contents of the macro
	 */
	public String getContents() {
		return this.contents;
	}

	/**
	 * Evaluates the current macro.
	 *
	 * @param paramValues the parameter values for the macro insertion
	 * @param returns the number of return values required
	 */
	public Expression evaluate(ArrayList<Expression> paramValues, int returns) {
		int currentLineNo = ScriptParser.tokenizer.lineno();

		ScriptParser.pushTokenizer(
				new Tokenizer(new StringReader(this.contents), "macro \"" + this.name + "\""));
		ScriptParser.tokenizer.setAutoPop(false);

		// A local constants list for the arguments for this macro.
		DefineList constantList = new DefineList();
		if (paramValues != null)
			for (int i = 0; i < this.paramCount; i++)
				constantList.add(this.params[i], paramValues.get(i));

		// Add the number of return values to the constants list.
		constantList.add("Returns", Expression.fromInteger(returns));

		MacroEvaluated macroEvaluated = new MacroEvaluated(this.name, constantList);
		macroEvaluated.evaluate();

		if (returns > 0) {
			if (macroEvaluated.getReturnValues() == null) {
				throw new NslException(
						"Insertion of macro \""
								+ this.name
								+ "\" requires "
								+ returns
								+ (returns == 1 ? " return value" : " return values")
								+ ", but no #return directive used",
						true);
			} else if (!macroEvaluated.getReturnValues().isEmpty()) {
				int returnValues = macroEvaluated.getReturnValues().size();
				if (returnValues != returns)
					throw new NslException(
							"Insertion of macro \""
									+ this.name
									+ "\" requires "
									+ returns
									+ (returns == 1 ? " return value" : " return values")
									+ ", but "
									+ returnValues
									+ (returnValues == 1 ? " return value was" : " return values were")
									+ " returned",
							currentLineNo);
			}
		}

		ScriptParser.popTokenizer();

		// We add the contents after the current statement. This is so the return
		// registers list is known throughout the macro (i.e. for #nsis).
		// StatementList.getCurrent().addQueued(macroContents);

		// #nsis directive was used in the macro. Return the evaluated macro so that
		// it can tell the #nsis directive what the return registers are.
		if (macroEvaluated.getReturnValues().isEmpty()) return macroEvaluated;

		// Caller requires a single return value. Return that value.
		if (returns == 1) {
			StatementList.getCurrent()
					.add(new AssignmentStatement(new AssignmentExpression(-1, macroEvaluated)));
			return macroEvaluated.getReturnValues().get(0);
		}

		// Return the evaluated macro which handles multiple return values.
		if (returns > 1) return macroEvaluated;

		return new NullAssembleExpression();
	}

	/**
	 * Returns a string representation of the current object.
	 *
	 * @return a string representation of the current object
	 */
	@Override
	public String toString() {
		if (this.paramCount < 0) return this.name;
		String call = this.name + '(';
		for (int i = 0; i < this.paramCount; i++) {
			call += this.params[i];
			if (i != this.paramCount - 1) call += ", ";
		}
		return call + ')';
	}
}
