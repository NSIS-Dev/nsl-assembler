/*
 * IntPtrOpInstruction.java
 */

package nsl.instruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import nsl.*;
import nsl.expression.*;

/**
 * @author Jan
 */
public class IntPtrOpInstruction extends AssembleExpression {
	public static final String name = "IntPtrOp";

	private static final List<String> binaryOperators =
			Arrays.asList("+", "-", "*", "/", "%", "|", "&", "^", "||", "&&", "<<", ">>", ">>>");
	private static final List<String> unaryOperators = Arrays.asList("~", "!");

	private final Expression value1;
	private final Expression operator;
	private final Expression value2;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public IntPtrOpInstruction(int returns) {
		if (PageExInfo.in())
			throw new NslContextException(
					EnumSet.of(NslContext.Section, NslContext.Function, NslContext.Global), name);
		if (returns != 1) throw new NslReturnValueException(name, 1);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 2 || paramsCount > 3) throw new NslArgumentException(name, 2, 3);

		this.value1 = paramsList.get(0);

		// The operator decides how many operands the instruction takes, so it has to
		// be known while assembling and cannot be a variable.
		this.operator = paramsList.get(1);
		if (!ExpressionType.isString(this.operator))
			throw new NslArgumentException(name, 2, ExpressionType.String);

		String operatorValue = this.operator.toString(true);
		boolean unary = unaryOperators.contains(operatorValue);
		if (!unary && !binaryOperators.contains(operatorValue))
			throw new NslException(
					"\""
							+ name
							+ "\" does not recognise the operator \""
							+ operatorValue
							+ "\"; expected one of "
							+ binaryOperators
							+ " or "
							+ unaryOperators,
					true);

		// "~" and "!" negate a single operand; every other operator needs two.
		if (unary) {
			if (paramsCount != 2)
				throw new NslException(
						"\"" + name + "\" with the \"" + operatorValue + "\" operator expects 2 parameter(s)",
						true);
			this.value2 = null;
		} else {
			if (paramsCount != 3)
				throw new NslException(
						"\"" + name + "\" with the \"" + operatorValue + "\" operator expects 3 parameter(s)",
						true);
			this.value2 = paramsList.get(2);
		}
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
		AssembleExpression.assembleIfRequired(this.operator);
		Expression varOrValue1 = AssembleExpression.getRegisterOrExpression(this.value1);
		if (this.value2 == null) {
			ScriptParser.writeLine(name + " " + var + " " + varOrValue1 + " " + this.operator);
		} else {
			Expression varOrValue2 = AssembleExpression.getRegisterOrExpression(this.value2);
			ScriptParser.writeLine(
					name + " " + var + " " + varOrValue1 + " " + this.operator + " " + varOrValue2);
			varOrValue2.setInUse(false);
		}
		varOrValue1.setInUse(false);
	}
}
