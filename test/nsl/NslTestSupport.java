/*
 * NslTestSupport.java
 */

package nsl;

import static org.junit.Assert.*;

import java.io.StringReader;
import nsl.expression.Expression;
import nsl.statement.Statement;

/**
 * Shared helpers for the in-process tests: driving a snippet through the parser and putting the
 * assembler's static state back the way it was found.
 *
 * <p>The {@code evaluate} and {@code assertRejected} pair mirror the private helpers in {@link
 * nsl.expression.ExpressionTest}, which is left untouched deliberately - it pins existing
 * behaviour.
 *
 * @author Jan T. Sott
 */
public final class NslTestSupport {
	private NslTestSupport() {}

	/** The source name reported in diagnostics from a snippet. */
	private static final String SOURCE = "test snippet";

	/**
	 * Parses a single expression from the given source and returns it. Every call leaves the
	 * tokenizer stack as it found it.
	 *
	 * @param expression the source to parse
	 * @return the parsed expression
	 */
	public static Expression parseExpression(String expression) {
		ScriptParser.pushTokenizer(new Tokenizer(new StringReader(expression), SOURCE));
		try {
			return Expression.matchComplex();
		} finally {
			ScriptParser.popTokenizer();
		}
	}

	/**
	 * Parses a single expression from the given source and returns its value as a string.
	 *
	 * @param expression the source to parse
	 * @return the string form of the parsed expression
	 */
	public static String evaluate(String expression) {
		return parseExpression(expression).toString();
	}

	/**
	 * Asserts that the given expression is rejected by the assembler.
	 *
	 * @param expression the source to parse
	 * @return the exception that was thrown, for assertions on its message
	 */
	public static NslException assertRejected(String expression) {
		try {
			String result = evaluate(expression);
			fail(expression + " was accepted and gave " + result);
			return null; // Unreachable; fail() always throws.
		} catch (NslException ex) {
			return ex;
		}
	}

	/**
	 * Returns the assembler's global state to what a fresh JVM would hold. Call it from both
	 * {@code @Before} and {@code @After} so that no test depends on the order the others ran in.
	 *
	 * <p>{@code Scope.global} is deliberately not reset: it is {@code static final} and accumulates a
	 * variable per {@link RegisterList} constructed, which is why the codegen tests fork instead of
	 * assembling in process. See {@link Assembler}.
	 */
	public static void resetParserState() {
		// popTokenizer() returns null on an empty stack without clearing the current
		// tokenizer, so a snippet that ran to EOF can leave one behind.
		ScriptParser.tokenizers.clear();
		ScriptParser.tokenizer = null;

		RegisterList.setCurrent(new RegisterList());
		FunctionInfo.getList().clear();
		Statement.getGlobal().clear();
		Statement.getGlobalUninstaller().clear();
		CodeInfo.setCurrent(null);
		PageExInfo.setCurrent(null);
		Scope.setInUninstaller(false);
	}
}
