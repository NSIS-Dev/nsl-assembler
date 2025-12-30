/*
 * Expression.java
 */

package nsl.expression;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import nsl.*;
import nsl.preprocessor.*;
import nsl.statement.*;

/**
 * Describes an expression with its type and value.
 *
 * @author Stuart
 */
public class Expression {
	protected ExpressionType type;
	protected String stringValue;
	protected int integerValue;
	protected boolean booleanValue;

	public static final Expression Empty = Expression.fromString("");
	public static final Expression Null = new Expression();

	private static boolean specialStringEscape = true;

	/**
	 * Sets whether or not strings using the special quote character (`) will be escaped (that is $
	 * replaced with $$ etc.).
	 *
	 * @see escapeString(String)
	 * @param value the value to set
	 * @return the old value
	 */
	public static boolean setSpecialStringEscape(boolean value) {
		boolean oldValue = specialStringEscape;
		specialStringEscape = value;
		return oldValue;
	}

	/** Class constructor. */
	protected Expression() {
		this.type = ExpressionType.Other;
	}

	/**
	 * Class constructor.
	 *
	 * @param the {@link Tokenizer} to use
	 */
	private Expression(Tokenizer tokenizer) {
		if (tokenizer.tokenIsWord()) {
			if (tokenizer.sval.startsWith("$")) {
				if (tokenizer.sval.length() == 1) {
					tokenizer.tokenNext();

					if (tokenizer.match('(')) {
						this.type = ExpressionType.String;
						this.stringValue = "$(";
						if (tokenizer.match('^')) this.stringValue += "^";
						this.stringValue += tokenizer.matchAWord("a language string");
						if (!tokenizer.tokenIs(')'))
							throw new NslException("Register, constant or language string requires a name", true);
						this.stringValue += ")";
						this.booleanValue = false;
						this.integerValue = 0;
					} else
						throw new NslException("Register, constant or language string requires a name", true);
				} else {
					int constant = ConstantList.getCurrent().lookup(tokenizer.sval);
					if (constant != -1) {
						this.type = ExpressionType.Constant;
						this.stringValue = null;
						this.booleanValue = false;
						this.integerValue = constant;
					} else {
						this.type = ExpressionType.Register;
						this.stringValue = null;
						this.booleanValue = false;
						this.integerValue = RegisterList.getCurrent().add(tokenizer.sval);

						if (CodeInfo.getCurrent() != null)
							CodeInfo.getCurrent().addUsedVar(RegisterList.getCurrent().get(this.integerValue));
					}
				}
			} else if (tokenizer.sval.equalsIgnoreCase("true")) {
				this.type = ExpressionType.Boolean;
				this.stringValue = null;
				this.booleanValue = true;
				this.integerValue = 0;
			} else if (tokenizer.sval.equalsIgnoreCase("false")) {
				this.type = ExpressionType.Boolean;
				this.stringValue = null;
				this.booleanValue = false;
				this.integerValue = 0;
			} else throw new NslException("Unrecognised token \"" + tokenizer.sval + "\"", true);
		} else if (tokenizer.tokenIsString()) {
			if (tokenizer.ttype == '`') {
				this.type = ExpressionType.StringSpecial;
				if (specialStringEscape) {
					this.stringValue = escapeString(tokenizer.sval);
					this.booleanValue = false;
				} else {
					this.stringValue = tokenizer.sval;
					this.booleanValue = true;
				}
			} else {
				this.type = ExpressionType.String;
				this.stringValue = escapeString(tokenizer.sval);
				this.booleanValue = false;
			}
			this.integerValue = 0;
		} else if (tokenizer.tokenIsNumber()) {
			this.type = ExpressionType.Integer;
			this.integerValue = (int) tokenizer.nval;
			this.stringValue = null;
			this.booleanValue = false;
		} else throw new NslException("Unrecognised token \"" + (char) tokenizer.ttype + "\"", true);

		tokenizer.tokenNext();
	}

	/**
	 * Escapes the given string.
	 *
	 * @param escape the string to escape
	 * @return the escaped string
	 */
	private static String escapeString(String escape) {
		return escape
				.replaceAll("\\$", "\\$\\$")
				.replaceAll("\"", "\\$\\\\\"")
				.replaceAll("\r", "\\$\\\\r")
				.replaceAll("\n", "\\$\\\\n")
				.replaceAll("\t", "\\$\\\\t");
	}

	/**
	 * Returns a new {@link Expression} object with the given register as a value.
	 *
	 * @param register the register
	 * @return the new {@link Expression} object
	 */
	public static Expression fromRegister(String register) {
		Expression expression = new Expression();
		expression.type = ExpressionType.Register;
		expression.stringValue = null;
		expression.booleanValue = false;
		expression.integerValue = RegisterList.getCurrent().add(register);
		return expression;
	}

	/**
	 * Returns a new {@link Expression} object with the given string as a value.
	 *
	 * @param value the string value
	 * @return the new {@link Expression} object
	 */
	public static Expression fromString(String value) {
		Expression expression = new Expression();
		expression.type = ExpressionType.String;
		expression.stringValue = value;
		expression.booleanValue = false;
		expression.integerValue = 0;
		return expression;
	}

	/**
	 * Returns a new {@link Expression} object with the given string as a value.
	 *
	 * @param value the string value
	 * @return the new {@link Expression} object
	 */
	public static Expression fromSpecialString(String value) {
		Expression expression = new Expression();
		expression.type = ExpressionType.StringSpecial;
		expression.stringValue = value;
		expression.booleanValue = !specialStringEscape;
		expression.integerValue = 0;
		return expression;
	}

	/**
	 * Returns a new {@link Expression} object with the given integer as a value.
	 *
	 * @param value the integer value
	 * @return the new {@link Expression} object
	 */
	public static Expression fromInteger(int value) {
		Expression expression = new Expression();
		expression.type = ExpressionType.Integer;
		expression.stringValue = null;
		expression.booleanValue = false;
		expression.integerValue = value;
		return expression;
	}

	/**
	 * Returns a new {@link Expression} object with the given Boolean as a value.
	 *
	 * @param value the Boolean value
	 * @return the new {@link Expression} object
	 */
	public static Expression fromBoolean(boolean value) {
		Expression expression = new Expression();
		expression.type = ExpressionType.Boolean;
		expression.stringValue = null;
		expression.booleanValue = value;
		expression.integerValue = 0;
		return expression;
	}

	/**
	 * Determines if the expression is a literal (hard coded) value.
	 *
	 * @return <code>true</code> if the expression is a literal (hard coded) value
	 */
	public boolean isLiteral() {
		return true;
	}

	/**
	 * Creates a mathematical expression object or if the operands are both numbers, evaluates the
	 * expression.
	 *
	 * @param left the left operand
	 * @param operator the operator
	 * @param right the right operand
	 * @return the expression
	 */
	private static Expression createMathematical(Expression left, String operator, Expression right) {
		// Matched registers. Check their scope.
		if (left.type.equals(ExpressionType.Register)) Scope.getCurrent().check(left.integerValue);
		if (right.type.equals(ExpressionType.Register)) Scope.getCurrent().check(right.integerValue);

		// Left and right are literals.
		if (left.isLiteral() && right.isLiteral()) {
			// Should not be using mathematical operators on Boolean values.
			if (left.type.equals(ExpressionType.Boolean) || right.type.equals(ExpressionType.Boolean))
				NslException.printWarning("\"" + operator + "\" operator used with a Boolean operand");

			// Should not be using mathematical operators on string values.
			if (ExpressionType.isString(left) || ExpressionType.isString(right))
				NslException.printWarning("\"" + operator + "\" operator used with a string operand");

			// Integer types.
			if (left.type.equals(ExpressionType.Integer) && right.type.equals(ExpressionType.Integer)) {
				if (operator.equals("+"))
					return Expression.fromInteger(left.integerValue + right.integerValue);
				if (operator.equals("-"))
					return Expression.fromInteger(left.integerValue - right.integerValue);
				if (operator.equals("*"))
					return Expression.fromInteger(left.integerValue * right.integerValue);
				if (operator.equals("/")) {
					if (right.integerValue == 0) throw new NslException("Division by zero", true);
					return Expression.fromInteger(left.integerValue / right.integerValue);
				}
				if (operator.equals("%"))
					return Expression.fromInteger(left.integerValue % right.integerValue);
				if (operator.equals("|"))
					return Expression.fromInteger(left.integerValue | right.integerValue);
				if (operator.equals("&"))
					return Expression.fromInteger(left.integerValue & right.integerValue);
				if (operator.equals("^"))
					return Expression.fromInteger(left.integerValue ^ right.integerValue);
				if (operator.equals("<<"))
					return Expression.fromInteger(left.integerValue << right.integerValue);
				if (operator.equals(">>"))
					return Expression.fromInteger(left.integerValue >> right.integerValue);
				if (operator.equals("~")) return Expression.fromInteger(~left.integerValue);
			}
		}

		return new MathematicalExpression(left, operator, right);
	}

	/**
	 * Creates a comparison expression object or if the operands are both numbers, evaluates the
	 * expression.
	 *
	 * @param left the left operand
	 * @param operator the operator
	 * @param right the right operand
	 * @param comparisonType how the two operands should be compared
	 * @return the expression
	 */
	private static Expression createComparison(
			Expression left, String operator, Expression right, ComparisonType comparisonType) {
		// Matched registers. Check their scope.
		if (left.type.equals(ExpressionType.Register)) Scope.getCurrent().check(left.integerValue);
		if (right.type.equals(ExpressionType.Register)) Scope.getCurrent().check(right.integerValue);

		// Left and right are literals.
		if (left.isLiteral() && right.isLiteral()) {
			if (left.type.equals(ExpressionType.Boolean) || right.type.equals(ExpressionType.Boolean)) {
				// Should not be using these operators on Boolean values.
				if (operator.equals(">")
						|| operator.equals(">=")
						|| operator.equals("<")
						|| operator.equals("<="))
					NslException.printWarning("\"" + operator + "\" operator used with a Boolean operand");
				else comparisonType = ComparisonType.String;
			}

			// Integer types.
			if (left.type.equals(ExpressionType.Integer) && right.type.equals(ExpressionType.Integer)) {
				if (comparisonType.equals(ComparisonType.IntegerUnsigned)) {
					left.integerValue = Math.abs(left.integerValue);
					right.integerValue = Math.abs(right.integerValue);
				}

				if (operator.equals("=="))
					return Expression.fromBoolean(left.integerValue == right.integerValue);
				if (operator.equals("!="))
					return Expression.fromBoolean(left.integerValue != right.integerValue);
				if (operator.equals(">"))
					return Expression.fromBoolean(left.integerValue > right.integerValue);
				if (operator.equals(">="))
					return Expression.fromBoolean(left.integerValue >= right.integerValue);
				if (operator.equals("<"))
					return Expression.fromBoolean(left.integerValue < right.integerValue);
				if (operator.equals("<="))
					return Expression.fromBoolean(left.integerValue <= right.integerValue);
			}
			// Boolean types.
			else if (left.type.equals(ExpressionType.Boolean)
					&& right.type.equals(ExpressionType.Boolean)) {
				if (operator.equals("=="))
					return Expression.fromBoolean(left.booleanValue == right.booleanValue);
				if (operator.equals("!="))
					return Expression.fromBoolean(left.booleanValue != right.booleanValue);
			}
			// String types.
			else if (ExpressionType.isString(left) && ExpressionType.isString(right)) {
				if (comparisonType.equals(ComparisonType.StringCaseSensitive)) {
					if (operator.equals("=="))
						return Expression.fromBoolean(left.toString(true).equals(right.toString(true)));
					if (operator.equals("!="))
						return Expression.fromBoolean(!left.toString(true).equals(right.toString(true)));
					if (operator.equals(">"))
						return Expression.fromBoolean(left.toString(true).compareTo(right.toString(true)) > 0);
					if (operator.equals(">="))
						return Expression.fromBoolean(left.toString(true).compareTo(right.toString(true)) >= 0);
					if (operator.equals("<"))
						return Expression.fromBoolean(left.toString(true).compareTo(right.toString(true)) < 0);
					if (operator.equals("<="))
						return Expression.fromBoolean(left.toString(true).compareTo(right.toString(true)) <= 0);
				} else {
					if (operator.equals("=="))
						return Expression.fromBoolean(
								left.toString(true).equalsIgnoreCase(right.toString(true)));
					if (operator.equals("!="))
						return Expression.fromBoolean(
								!left.toString(true).equalsIgnoreCase(right.toString(true)));
					if (operator.equals(">"))
						return Expression.fromBoolean(
								left.toString(true).compareToIgnoreCase(right.toString(true)) > 0);
					if (operator.equals(">="))
						return Expression.fromBoolean(
								left.toString(true).compareToIgnoreCase(right.toString(true)) >= 0);
					if (operator.equals("<"))
						return Expression.fromBoolean(
								left.toString(true).compareToIgnoreCase(right.toString(true)) < 0);
					if (operator.equals("<="))
						return Expression.fromBoolean(
								left.toString(true).compareToIgnoreCase(right.toString(true)) <= 0);
				}
			}
		} else if (ExpressionType.isBoolean(left)) {
			if (operator.equals("==") && right.type == ExpressionType.Boolean) {
				if (left.booleanValue == false) right.booleanValue = !right.booleanValue;
				return right;
			}
			if (operator.equals("!=") && right.type == ExpressionType.Boolean) {
				if (left.booleanValue == true) right.booleanValue = !right.booleanValue;
				return right;
			}
			comparisonType = ComparisonType.String;
		} else if (ExpressionType.isBoolean(right)) {
			if (operator.equals("==") && left.type == ExpressionType.Boolean) {
				if (right.booleanValue == false) left.booleanValue = !left.booleanValue;
				return left;
			}
			if (operator.equals("!=") && left.type == ExpressionType.Boolean) {
				if (right.booleanValue == true) left.booleanValue = !left.booleanValue;
				return left;
			}
			comparisonType = ComparisonType.String;
		}

		return new ComparisonExpression(left, operator, right, comparisonType);
	}

	/**
	 * Creates a Boolean expression object or if the operands are both numbers, evaluates the
	 * expression.
	 *
	 * @param left the left operand
	 * @param operator the operator
	 * @param right the right operand
	 * @return the expression
	 */
	private static Expression createBoolean(Expression left, String operator, Expression right) {
		// Matched registers. Check their scope.
		if (left.type.equals(ExpressionType.Register)) Scope.getCurrent().check(left.integerValue);
		if (right.type.equals(ExpressionType.Register)) Scope.getCurrent().check(right.integerValue);

		// Left and right are literals. If both are also Boolean, we can evaluate
		// now.
		if (left.isLiteral() && right.isLiteral()) {
			if (left.type.equals(ExpressionType.Boolean) && right.type.equals(ExpressionType.Boolean)) {
				if (operator.equals("&&"))
					return Expression.fromBoolean(left.booleanValue && right.booleanValue);
				if (operator.equals("||"))
					return Expression.fromBoolean(left.booleanValue || right.booleanValue);
			}
		} else if (right.isLiteral()) {
			// If the right operand is a literal true and the operator is && then we can
			// discard the right operand.
			if (operator.equals("&&")
					&& right.type.equals(ExpressionType.Boolean)
					&& right.booleanValue == true) return left;
			// If the right operand is a literal false and the operator is || then we
			// can discard the right operand.
			if (operator.equals("||")
					&& right.type.equals(ExpressionType.Boolean)
					&& right.booleanValue == false) return left;
		}

		return new BooleanExpression(left, operator, right);
	}

	/**
	 * Creates a concatenation expression object or if the operands are literals, concatenates them.
	 *
	 * @param left the left operand
	 * @param right the right operand
	 * @return the expression
	 */
	private static Expression createConcatenation(Expression left, Expression right) {
		// Matched registers. Check their scope.
		if (left.type.equals(ExpressionType.Register)) Scope.getCurrent().check(left.integerValue);
		if (right.type.equals(ExpressionType.Register)) Scope.getCurrent().check(right.integerValue);

		// Left and right are literals; concatenate them.
		if (left.isLiteral() && right.isLiteral()) {
			if (left.type.equals(ExpressionType.StringSpecial)
					|| right.type.equals(ExpressionType.StringSpecial))
				return Expression.fromSpecialString(left.toString(true) + right.toString(true));
			return Expression.fromString(left.toString(true) + right.toString(true));
		}

		return new ConcatenationExpression(left, right);
	}

	/**
	 * Creates a ternary expression object or if the left operand is a literal, evaluates it.
	 *
	 * @param left the left operand
	 * @param ifTrue the expression if true
	 * @param ifFalse the expression if false
	 * @return the expression
	 */
	private static Expression createTernary(Expression left, Expression ifTrue, Expression ifFalse) {
		// Matched registers. Check their scope.
		if (left.type.equals(ExpressionType.Register)) Scope.getCurrent().check(left.integerValue);
		if (ifTrue.type.equals(ExpressionType.Register)) Scope.getCurrent().check(ifTrue.integerValue);
		if (ifFalse.type.equals(ExpressionType.Register))
			Scope.getCurrent().check(ifFalse.integerValue);

		if (left.isLiteral()) {
			if (left.type.equals(ExpressionType.Boolean)) {
				if (left.booleanValue == true) return ifTrue;
				return ifFalse;
			}
		}

		return new TernaryExpression(left, ifTrue, ifFalse);
	}

	/**
	 * Matches a primary expression. This includes matching the Boolean NOT (!) operator and unary
	 * negate (~) operator.
	 *
	 * @return the expression
	 */
	private static Expression matchPrimary() {
		boolean logicalNegate = false, binaryNegate = false, minus = false;

		// The expression is prefixed with a char.
		if (ScriptParser.tokenizer.tokenIsChar()) {
			// Logical negate the expression.
			if (ScriptParser.tokenizer.match('!')) logicalNegate = true;
			// Binary negate the expression.
			else if (ScriptParser.tokenizer.match('~')) binaryNegate = true;

			// Negative number.
			if (ScriptParser.tokenizer.match('-')) minus = true;

			// New bracket; new expression.
			if (ScriptParser.tokenizer.match('(')) {
				Expression left = matchComplex();

				// Negate the returned Boolean (logical) expression.
				if (logicalNegate) {
					if (!left.type.equals(ExpressionType.Boolean))
						throw new NslException(
								"The \"!\" operator must be applied to a Boolean expression", true);

					/*if (left instanceof ConditionalExpression)
						((ConditionalExpression)left).setNegate(true);
					else */ if (left.booleanValue) left.booleanValue = false;
					else left.booleanValue = true;
				}
				// Binary negate the returned expression.
				else if (binaryNegate) {
					left = createMathematical(left, "~", Expression.fromInteger(0));
				}

				// To make the returned number negative, multiply by -1.
				if (minus) {
					left = createMathematical(Expression.fromInteger(-1), "*", left);
				}

				// Match the end bracket.
				ScriptParser.tokenizer.matchOrDie(')');

				return left;
			}

			// Would only occur if some invalid character appeared.
			if (!logicalNegate && !binaryNegate && !minus)
				throw new NslExpectedException("an expression");
		}

		// Match the actual value.
		Expression left = match();

		// Negate the returned Boolean (logical) value.
		if (logicalNegate) {
			if (!left.type.equals(ExpressionType.Boolean))
				throw new NslException("The \"!\" operator must be applied to a Boolean expression", true);

			if (left.booleanValue) left.booleanValue = false;
			else left.booleanValue = true;
		}
		// Binary negate the returned value.
		else if (binaryNegate) {
			left = createMathematical(left, "~", Expression.fromInteger(0));
		}

		// To make the returned number negative, multiply by -1.
		if (minus) {
			left = createMathematical(Expression.fromInteger(-1), "*", left);
		}

		// Next token is a word? It could be an operator in a late evaluation
		// constant.
		if (ScriptParser.tokenizer.tokenIsWord()) {
			Expression value = DefineList.lookup(ScriptParser.tokenizer.sval);
			if (value != null
					&& value.type.equals(ExpressionType.StringSpecial)
					&& value.booleanValue == true) {
				String name = ScriptParser.tokenizer.sval;
				ScriptParser.tokenizer.tokenNext(); // Discard the constant name.
				ScriptParser.pushTokenizer(
						new Tokenizer(new StringReader(value.stringValue), "constant \"" + name + "\""));
			}
		}

		return left;
	}

	/**
	 * Matches any concatenation expression.
	 *
	 * @param concatenateList the {@link ArrayList} of {@link Expression} objects
	 * @param first the current expression is the first in the list
	 */
	private static Expression matchConcatenation() {
		Expression left = matchPrimary();

		while (ScriptParser.tokenizer.match('.')) {
			// Match .= (assignment)
			if (ScriptParser.tokenizer.match('=')) {
				if (!left.type.equals(ExpressionType.Register))
					throw new NslException("The left operand must be a variable", true);

				left =
						new AssignmentExpression(left.integerValue, createConcatenation(left, matchComplex()));
				Scope.getCurrent().addVar(left.integerValue);
			}
			// Matched .
			else {
				left = createConcatenation(left, matchPrimary());
			}
		}

		return left;
	}

	/**
	 * Matches a multiplicative expression.
	 *
	 * @return the expression
	 */
	private static Expression matchMultiplicative() {
		Expression left = matchConcatenation();

		while (ScriptParser.tokenizer.tokenIs('*')
				|| ScriptParser.tokenizer.tokenIs('/')
				|| ScriptParser.tokenizer.tokenIs('%')) {
			String operator = Character.toString((char) ScriptParser.tokenizer.ttype);
			ScriptParser.tokenizer.tokenNext("an expression");

			// Match *=, /= or %= (assignment)
			if (ScriptParser.tokenizer.match('=')) {
				if (!left.type.equals(ExpressionType.Register))
					throw new NslException("The left operand must be a variable", true);

				left =
						new AssignmentExpression(
								left.integerValue, createMathematical(left, operator, matchComplex()));
				Scope.getCurrent().addVar(left.integerValue);
			}
			// Matched *, / or %
			else {
				left = createMathematical(left, operator, matchConcatenation());
			}
		}

		return left;
	}

	/**
	 * Matches an additive expression.
	 *
	 * @return the expression
	 */
	private static Expression matchAdditive() {
		Expression left = matchMultiplicative();

		while (ScriptParser.tokenizer.tokenIs('+') || ScriptParser.tokenizer.tokenIs('-')) {
			String operator = Character.toString((char) ScriptParser.tokenizer.ttype);
			ScriptParser.tokenizer.tokenNext("an expression");

			// Match n++ or n--
			if (ScriptParser.tokenizer.match(operator.charAt(0))) {
				if (!left.type.equals(ExpressionType.Register))
					throw new NslException("The left operand must be a variable", true);

				left =
						new AssignmentExpression(
								left.integerValue, createMathematical(left, operator, Expression.fromInteger(1)));
				Scope.getCurrent().addVar(left.integerValue);
			}
			// Match += or -= (assignment)
			else if (ScriptParser.tokenizer.match('=')) {
				if (!left.type.equals(ExpressionType.Register))
					throw new NslException("The left operand must be a variable", true);

				left =
						new AssignmentExpression(
								left.integerValue, createMathematical(left, operator, matchComplex()));
				Scope.getCurrent().addVar(left.integerValue);
			}
			// Matched + or -
			else {
				left = createMathematical(left, operator, matchMultiplicative());
			}
		}

		return left;
	}

	/**
	 * Matches a Boolean relational expression.
	 *
	 * @return the expression
	 */
	private static Expression matchRelational() {
		Expression left = matchAdditive();

		while (ScriptParser.tokenizer.tokenIs('<') || ScriptParser.tokenizer.tokenIs('>')) {
			String operator = Character.toString((char) ScriptParser.tokenizer.ttype);
			ScriptParser.tokenizer.tokenNext("an expression");

			// Match <= or >=
			if (ScriptParser.tokenizer.match('=')) {
				ComparisonType comparisonType = ComparisonType.match();
				left = createComparison(left, operator + '=', matchAdditive(), comparisonType);
			}
			// Match <<
			else if (operator.equals("<") && ScriptParser.tokenizer.match('<')) {
				// Match <<= (assignment)
				if (ScriptParser.tokenizer.match('=')) {
					if (!left.type.equals(ExpressionType.Register))
						throw new NslException("The left operand must be a variable", true);

					left =
							new AssignmentExpression(
									left.integerValue, createMathematical(left, "<<", matchComplex()));
					Scope.getCurrent().addVar(left.integerValue);
				}
				// Matched <<
				else {
					left = createMathematical(left, "<<", matchAdditive());
				}
			}
			// Match >>
			else if (operator.equals(">") && ScriptParser.tokenizer.match('>')) {
				// Match >>= (assignment)
				if (ScriptParser.tokenizer.match('=')) {
					if (!left.type.equals(ExpressionType.Register))
						throw new NslException("The left operand must be a variable", true);

					left =
							new AssignmentExpression(
									left.integerValue, createMathematical(left, ">>", matchComplex()));
					Scope.getCurrent().addVar(left.integerValue);
				}
				// Matched >>
				else {
					left = createMathematical(left, ">>", matchAdditive());
				}
			}
			// Matched < or >
			else {
				ComparisonType comparisonType = ComparisonType.match();
				left = createComparison(left, operator, matchAdditive(), comparisonType);
			}
		}

		return left;
	}

	/**
	 * Matches a Boolean equality expression or an assignment expression.
	 *
	 * @return the expression
	 */
	private static Expression matchEqualityOrAssignment() {
		Expression left = matchRelational();

		while (ScriptParser.tokenizer.tokenIs('!') || ScriptParser.tokenizer.tokenIs('=')) {
			String operator = Character.toString((char) ScriptParser.tokenizer.ttype);
			ScriptParser.tokenizer.tokenNext("an expression");

			// Match != or ==
			if (ScriptParser.tokenizer.match('=')) {
				operator += "=";

				ComparisonType comparisonType = ComparisonType.match();
				left = createComparison(left, operator, matchRelational(), comparisonType);

				// Optimise jump instructions to assemble jumps according to the Boolean
				// expression.
				if (left instanceof ComparisonExpression) {
					ComparisonExpression leftComparisonExpression = (ComparisonExpression) left;
					if (leftComparisonExpression.getLeftOperand() instanceof JumpExpression
							&& leftComparisonExpression.getRightOperand().isLiteral()) {
						JumpExpression jumpInstruction =
								(JumpExpression) (leftComparisonExpression).getLeftOperand();
						if (jumpInstruction.optimise((leftComparisonExpression).getRightOperand(), operator)) {
							if (leftComparisonExpression.booleanValue)
								jumpInstruction.booleanValue = !jumpInstruction.booleanValue;
							left = jumpInstruction;
						}
					} else if (leftComparisonExpression.getRightOperand() instanceof JumpExpression
							&& leftComparisonExpression.getLeftOperand().isLiteral()) {
						JumpExpression jumpInstruction =
								(JumpExpression) (leftComparisonExpression).getRightOperand();
						if (jumpInstruction.optimise((leftComparisonExpression).getLeftOperand(), operator)) {
							if (leftComparisonExpression.booleanValue)
								jumpInstruction.booleanValue = !jumpInstruction.booleanValue;
							left = jumpInstruction;
						}
					}
				}
			}
			// Match = (assignment)
			else if (operator.equals("=")) {
				if (!left.type.equals(ExpressionType.Register))
					throw new NslException("The left operand must be a variable", true);

				left = new AssignmentExpression(left.integerValue, matchComplex());
				Scope.getCurrent().addVar(left.integerValue);
			}
		}

		return left;
	}

	/**
	 * Matches a logical (Boolean) AND (&&) expression or a binary AND (&) expression.
	 *
	 * @return the expression
	 */
	private static Expression matchLogicalAndOrBinaryAnd() {
		Expression left = matchEqualityOrAssignment();

		while (ScriptParser.tokenizer.match('&')) {
			// Match &&
			if (ScriptParser.tokenizer.match('&')) {
				if (ExpressionType.isBoolean(left) && left.booleanValue == false) {
					// Evaluated to false; we need not evaluate anything up until the next
					// ||, ) or ;.
					while (ScriptParser.tokenizer.tokenNext("\")\"")) {
						if (ScriptParser.tokenizer.tokenIs('('))
							while (ScriptParser.tokenizer.tokenNext("\")\""))
								if (ScriptParser.tokenizer.match(')')) break;
						if (ScriptParser.tokenizer.tokenIs(')')
								|| ScriptParser.tokenizer.tokenIs('|')
								|| ScriptParser.tokenizer.tokenIs(';')) break;
					}
				} else {
					left = createBoolean(left, "&&", matchEqualityOrAssignment());
				}
			}
			// Match &= (assignment)
			else if (ScriptParser.tokenizer.match('=')) {
				if (!left.type.equals(ExpressionType.Register))
					throw new NslException("The left operand must be a variable", true);

				left =
						new AssignmentExpression(
								left.integerValue, createMathematical(left, "&", matchComplex()));
				Scope.getCurrent().addVar(left.integerValue);
			}
			// Matched &
			else {
				left = createMathematical(left, "&", matchEqualityOrAssignment());
			}
		}

		return left;
	}

	/**
	 * Matches a binary inclusive OR (^) expression.
	 *
	 * @return the expression
	 */
	private static Expression matchBinaryExclusiveOr() {
		Expression left = matchLogicalAndOrBinaryAnd();

		while (ScriptParser.tokenizer.match('^')) {
			// Match ^= (assignment)
			if (ScriptParser.tokenizer.match('=')) {
				if (!left.type.equals(ExpressionType.Register))
					throw new NslException("The left operand must be a variable", true);

				left =
						new AssignmentExpression(
								left.integerValue, createMathematical(left, "^", matchComplex()));
				Scope.getCurrent().addVar(left.integerValue);
			}
			// Matched ^
			else {
				left = createMathematical(left, "^", matchLogicalAndOrBinaryAnd());
			}
		}

		return left;
	}

	/**
	 * Matches a logical (Boolean) OR (||) expression or a binary inclusive OR (|) expression.
	 *
	 * @return the expression
	 */
	private static Expression matchLogicalOrOrBinaryInclusiveOr() {
		Expression left = matchBinaryExclusiveOr();

		while (ScriptParser.tokenizer.match('|')) {
			// Match ||
			if (ScriptParser.tokenizer.match('|')) {
				if (ExpressionType.isBoolean(left) && left.booleanValue == true) {
					// Evaluated to true; we can ignore the rest of the expression.
					while (ScriptParser.tokenizer.tokenNext("\")\" or \";\""))
						if (ScriptParser.tokenizer.tokenIs(')') || ScriptParser.tokenizer.tokenIs(';')) break;
				} else {
					left = createBoolean(left, "||", matchBinaryExclusiveOr());
				}
			}
			// Match |= (assignment)
			else if (ScriptParser.tokenizer.match('=')) {
				if (!left.type.equals(ExpressionType.Register))
					throw new NslException("The left operand must be a variable", true);

				left =
						new AssignmentExpression(
								left.integerValue, createMathematical(left, "|", matchComplex()));
				Scope.getCurrent().addVar(left.integerValue);
			}
			// Matched |
			else {
				left = createMathematical(left, "|", matchBinaryExclusiveOr());
			}
		}

		return left;
	}

	/**
	 * Matches a ternary expression.
	 *
	 * @return the expression
	 */
	private static Expression matchTernary() {
		Expression left = matchLogicalOrOrBinaryInclusiveOr();

		while (ScriptParser.tokenizer.match('?')) {
			// Matched ? :
			Expression ifTrue = matchComplex();
			ScriptParser.tokenizer.matchOrDie(':');
			left = createTernary(left, ifTrue, matchLogicalOrOrBinaryInclusiveOr());
		}

		return left;
	}

	/**
	 * Matches any complex expression.
	 *
	 * @return the expression
	 */
	public static Expression matchComplex() {
		Expression expression = matchTernary();

		// Matched a register that isn't being assigned to. Check its scope.
		if (!(expression instanceof AssembleExpression)
				&& expression.type.equals(ExpressionType.Register))
			Scope.getCurrent().check(expression.integerValue);

		return expression;
	}

	/**
	 * Matches any constant which could be a defined constant, macro call or function call.
	 *
	 * <p>If the <code>returns</code> argument is greater than 0 and a macro call or function call is
	 * evaluated which returns no value, an exception is thrown.
	 *
	 * @param returns the number of return values required
	 * @return the expression or {@code null}
	 */
	public static Expression matchConstant(int returns) {
		// NSIS instruction?
		Expression value = Statement.matchInstruction(returns);
		if (value != null) return value;

		String name = ScriptParser.tokenizer.matchAWord("a function, constant or macro identifier");

		// returnvar(n)
		// Gets the return register #n being assigned to
		if (name.equals("returnvar")) {
			ArrayList<Expression> paramsList = matchList();
			if (paramsList.size() != 1) throw new NslArgumentException("returnvar", 1);

			value = paramsList.get(0);
			if (!ExpressionType.isInteger(value))
				throw new NslArgumentException("returnvar", 1, ExpressionType.Integer);

			return new ReturnVarExpression(value.integerValue);
		}

		// toint(arg)
		// Converts a literal to an integer literal
		if (name.equals("toint")) {
			ArrayList<Expression> paramsList = matchList();
			int paramsCount = paramsList.size();
			if (paramsCount < 1 || paramsCount > 2) throw new NslArgumentException("toint", 1, 2);

			value = paramsList.get(0);
			if (!value.isLiteral()) throw new NslArgumentException("toint", 1, true);

			Expression defaultValue;
			if (paramsCount > 1) {
				defaultValue = paramsList.get(1);
				if (!ExpressionType.isInteger(defaultValue))
					throw new NslArgumentException("toint", 2, ExpressionType.Integer);
			} else defaultValue = null;

			// String literals are parsed as plain integers or as hexadecimal (0x...).
			if (value.type.equals(ExpressionType.String)
					|| value.type.equals(ExpressionType.StringSpecial)) {
				String stringValue = value.toString(true);
				try {
					if (stringValue.startsWith("0x"))
						return Expression.fromInteger(Integer.parseInt(stringValue, 16));
					return Expression.fromInteger(Integer.parseInt(stringValue));
				} catch (Exception ex) {
					if (defaultValue == null)
						NslException.printWarning(
								"\"toint\" could not convert string \""
										+ stringValue
										+ "\" to an integer. "
										+ ex.getMessage()
										+ ". Returned 0");
				}
				return defaultValue == null ? Expression.fromInteger(0) : defaultValue;
			}
			// Boolean true == 1, false == 0.
			else if (value.type.equals(ExpressionType.Boolean)) {
				if (value.booleanValue == true) return Expression.fromInteger(1);
				return defaultValue == null ? Expression.fromInteger(0) : defaultValue;
			}
			// No conversion needed for an integer literal.
			else if (value.type.equals(ExpressionType.Integer)) {
				return value;
			}
			// For a register or NSIS constant, we return the internal index.
			else if (value.type.equals(ExpressionType.Register)
					|| value.type.equals(ExpressionType.Constant)) {
				return Expression.fromInteger(value.integerValue);
			}

			throw new NslException(
					"\"intval\" cannot handle an input type of \"" + value.type + "\"", true);
		}

		// eval(string)
		// Evaluates the string argument
		if (name.equals("eval")) {
			ArrayList<Expression> paramsList = matchList();
			if (paramsList.size() != 1) throw new NslArgumentException("eval", 1);

			value = paramsList.get(0);
			if (!ExpressionType.isString(value))
				throw new NslArgumentException("eval", 1, ExpressionType.String);

			ScriptParser.pushTokenizer(new Tokenizer(new StringReader(value.stringValue), "eval"));
			if (returns == 0) return null;
			if (returns > 1) return matchConstant(returns);
			return matchComplex();
		}

		// StrCmp(a, b)
		// Compare two string values for equality
		// Replaces the NSIS StrCmp instruction
		if (name.equals("StrCmp")) {
			ArrayList<Expression> paramsList = matchList();
			if (paramsList.size() != 2) throw new NslArgumentException("StrCmp", 2);

			return createComparison(paramsList.get(0), "==", paramsList.get(1), ComparisonType.String);
		}

		// StrCmpS(a, b)
		// Compare two string values for equality, case sensitively
		// Replaces the NSIS StrCmpS instruction
		if (name.equals("StrCmpS")) {
			ArrayList<Expression> paramsList = matchList();
			if (paramsList.size() != 2) throw new NslArgumentException("StrCmpS", 2);

			return createComparison(
					paramsList.get(0), "==", paramsList.get(1), ComparisonType.StringCaseSensitive);
		}

		// defined(const1, const2, ...)
		// true if all constants in the list are defined
		if (name.equals("defined")) {
			ScriptParser.tokenizer.matchOrDie('(');
			if (ScriptParser.tokenizer.match(')'))
				throw new NslException("\"defined\" expects one or more constant names", true);

			boolean result = true;
			while (true) {
				String constant = ScriptParser.tokenizer.matchAWord("a constant name");
				if (result && DefineList.lookup(constant) == null) result = false;
				if (ScriptParser.tokenizer.match(')')) break;
				ScriptParser.tokenizer.matchOrDie(',');
			}

			return Expression.fromBoolean(result);
		}

		// type(expr)
		// the type of the given expression
		if (name.equals("type")) {
			ArrayList<Expression> paramsList = matchList();
			if (paramsList.size() != 1) throw new NslArgumentException("type", 1);

			value = paramsList.get(0);
			if (value.type != null && !(value instanceof AssembleExpression)) {
				switch (value.type) {
					case String:
					case StringSpecial:
						return Expression.fromString("String");
					case Register:
						return Expression.fromString("Register");
					case Constant:
						return Expression.fromString("Constant");
					case Integer:
						return Expression.fromString("Integer");
					case Boolean:
						return Expression.fromString("Boolean");
				}
			}

			return Expression.fromString("Nonliteral");
		}

		// format("format_str", arg1, arg2, ...)
		// inserts the arguments into format_str in place of {0}, {1} etc.
		if (name.equals("format")) {
			ArrayList<Expression> paramsList = matchList();
			int paramsCount = paramsList.size() - 1;
			if (paramsCount < 1) throw new NslArgumentException("format", 2, 999);

			value = paramsList.get(0);
			if (!ExpressionType.isString(value))
				throw new NslArgumentException("format", 1, ExpressionType.String);

			String formatString = value.toString(true);
			int formatStringLength = formatString.length();
			for (int i = 0; i < formatStringLength; i++) {
				// Two { characters escapes.
				if (formatString.charAt(i) == '{' && formatString.charAt(++i) != '{') {
					int paramNumberAt = i - 1;
					String paramNumberString = "";

					for (; i < formatStringLength; i++) {
						char c = formatString.charAt(i);
						if (c == '}') break;
						if (c < '0' || c > '9')
							throw new NslException(
									"Bad parameter number for \"format\" (contains non numeric characters)", true);
						paramNumberString += c;
					}

					int paramNumber = Integer.parseInt(paramNumberString);
					if (paramNumber < 0 || paramNumber >= paramsCount)
						throw new NslException(
								"Parameter number for \"format\" is out of range of given parameters", true);

					// Insert the parameter.
					String paramValue = paramsList.get(paramNumber + 1).toString(true);
					formatString =
							formatString.substring(0, paramNumberAt) + paramValue + formatString.substring(i + 1);

					// Move the new position to after the inserted parameter.
					i = paramNumberAt + paramValue.length();
				}
			}

			return Expression.fromString(formatString);
		}

		// length(literal)
		// length of the given literal, e.g. length($R0) = 3, length(99) = 2
		if (name.equals("length")) {
			ArrayList<Expression> paramsList = matchList();
			if (paramsList.size() != 1) throw new NslArgumentException("length", 1);

			value = paramsList.get(0);
			if (!value.isLiteral()) throw new NslArgumentException("length", 1, true);

			return Expression.fromInteger(value.toString(true).length());
		}

		// nsisconst(name)
		// returns an NSIS constant of the given name, i.e. ${name}
		if (name.equals("nsisconst")) {
			ScriptParser.tokenizer.matchOrDie('(');
			String constant = ScriptParser.tokenizer.matchAWord("a constant name");
			ScriptParser.tokenizer.matchOrDie(')');

			return Expression.fromString("${" + constant + "}");
		}

		// Get a local macro constant (macro parameter) or a global constant.
		value = DefineList.lookup(name);
		if (value != null) {
			// Late evaluation constant or a function or plug-in call follows.
			if (value.type.equals(ExpressionType.StringSpecial) && value.booleanValue == true
					|| ScriptParser.tokenizer.tokenIs('(')) {
				ScriptParser.pushTokenizer(
						new Tokenizer(new StringReader(value.toString(true)), "constant \"" + name + "\""));
				if (returns == 0) return null;
				if (returns > 1) return matchConstant(returns);
				return matchComplex();
			}

			return value;
		}

		// A plug-in call?
		if (ScriptParser.tokenizer.match(':')) {
			ScriptParser.tokenizer.matchOrDie(':');
			name = name + "::" + ScriptParser.tokenizer.matchAWord("a plug-in function name");
			return new PluginCallExpression(name, Expression.matchList());
		}

		// A function or macro call.
		if (ScriptParser.tokenizer.tokenIs('(')) {
			ArrayList<Expression> paramsList = Expression.matchList();

			ArrayList<Macro> macrosList = MacroList.getCurrent().get(name);
			if (!macrosList.isEmpty()) {
				int paramsCount = paramsList.size();

				// Find a macro which matches the function call.
				for (Macro macro : macrosList) {
					if (macro.getParamCount() == paramsCount) {
						value = macro.evaluate(paramsList, returns);

						// Late evaluation constant was returned.
						if (value.type.equals(ExpressionType.StringSpecial) && value.booleanValue == true) {
							ScriptParser.pushTokenizer(
									new Tokenizer(new StringReader(value.stringValue), "macro \"" + name + "\""));
							if (returns == 0) return null;
							if (returns > 1) return matchConstant(returns);
							return matchComplex();
						}

						return value;
					}
				}
			}

			// If it's not a macro insertion then it must be a function call.
			return new FunctionCallExpression(name, paramsList);
		}

		throw new NslException("Unrecognized constant or macro name \"" + name + "\"", true);
	}

	/**
	 * Matches the most basic expression.
	 *
	 * @return the matched expression
	 */
	public static Expression match() {
		if (ScriptParser.tokenizer.tokenIsWord()
				&& !ScriptParser.tokenizer.sval.startsWith("$")
				&& !ScriptParser.tokenizer.sval.equals("true")
				&& !ScriptParser.tokenizer.sval.equals("false")) return matchConstant(1);

		return new Expression(ScriptParser.tokenizer);
	}

	/**
	 * Finds a register in the given array of {@link Expression} objects.
	 *
	 * @param search the array to search
	 * @param find the register to find
	 * @return the register on success; <code>null</code> otherwise
	 */
	public static Expression findRegister(ArrayList<Expression> search, Expression find) {
		for (Expression var : search)
			if (var.type.equals(ExpressionType.Register) && find.integerValue == var.integerValue)
				return var;
		return null;
	}

	/**
	 * Finds a register in the given array of {@link Expression} objects.
	 *
	 * @param search the array to search
	 * @param find the register to find
	 * @return the register on success; <code>null</code> otherwise
	 */
	public static Expression findRegister(ArrayList<Register> search, Register find) {
		for (Expression var : search) if (find.integerValue == var.integerValue) return var;
		return null;
	}

	/**
	 * Finds a register in the given hash table of {@link Expression} objects.
	 *
	 * @param search the hash table to search
	 * @param find the register to find
	 * @return the register on success; <code>null</code> otherwise
	 */
	public static Expression findRegister(HashMap<Integer, Expression> search, Expression find) {
		if (find.type.equals(ExpressionType.Register))
			return search.get(Integer.valueOf(find.integerValue));
		return null;
	}

	/**
	 * Finds a register in the given hash table of {@link Expression} objects.
	 *
	 * @param search the hash table to search
	 * @param find the register to find
	 * @return the register on success; <code>null</code> otherwise
	 */
	public static Expression findRegister(HashMap<Integer, Register> search, Register find) {
		return search.get(Integer.valueOf(find.integerValue));
	}

	/**
	 * Gets the expression value.
	 *
	 * @return the expression value
	 */
	public String getStringValue() {
		return this.stringValue;
	}

	/**
	 * Gets the expression value.
	 *
	 * @return the expression value
	 */
	public int getIntegerValue() {
		return this.integerValue;
	}

	/**
	 * Gets the expression value.
	 *
	 * @return the expression value
	 */
	public boolean getBooleanValue() {
		return this.booleanValue;
	}

	/**
	 * Gets the expression type.
	 *
	 * @return the expression type
	 */
	public ExpressionType getType() {
		return this.type;
	}

	/** This does nothing. It is overridden and used by the {@link nsl.Register} class. */
	public void setInUse(boolean inUse) {}

	/**
	 * Returns a string representation of the current object.
	 *
	 * @return a string representation of the current object
	 */
	@Override
	public String toString() {
		return this.toString(false);
	}

	/**
	 * Returns a string representation of the current object.
	 *
	 * @param noQuote do not quote the value if it is a string
	 * @return a string representation of the current object
	 */
	public String toString(boolean noQuote) {
		switch (this.type) {
			case String:
			case StringSpecial:
				if (noQuote) return this.stringValue;
				return "\"" + this.stringValue + "\"";
			case Register:
				return RegisterList.getCurrent().get(this.integerValue).toString();
			case Constant:
				return ConstantList.getCurrent().get(this.integerValue).toString();
			case Integer:
				return Integer.toString(this.integerValue);
			case Boolean:
				return Boolean.toString(this.booleanValue);
		}

		return "?";
	}

	/**
	 * Matches a list of expressions in the format (a, b, c, ...).
	 *
	 * @return the list of expressions
	 */
	public static ArrayList<Expression> matchList() {
		ArrayList<Expression> expressionList = new ArrayList<Expression>();

		ScriptParser.tokenizer.matchOrDie('(');

		if (!ScriptParser.tokenizer.match(')')) {
			while (true) {
				expressionList.add(matchComplex());

				if (ScriptParser.tokenizer.match(')')) break;
				ScriptParser.tokenizer.matchOrDie(',');
			}
		}

		return expressionList;
	}

	/**
	 * Matches a list of expressions in the format (a, b, c, ...) that can only be registers.
	 *
	 * @return the list of expressions
	 */
	public static ArrayList<Expression> matchRegisterList() {
		ArrayList<Expression> expressionList = new ArrayList<Expression>();

		ScriptParser.tokenizer.matchOrDie('(');

		if (!ScriptParser.tokenizer.match(')')) {
			while (true) {
				Expression expression = match();
				if (!expression.type.equals(ExpressionType.Register))
					throw new NslExpectedException("Expected a register/variable");

				expressionList.add(expression);

				if (ScriptParser.tokenizer.match(')')) break;
				ScriptParser.tokenizer.matchOrDie(',');
			}
		}

		return expressionList;
	}
}
