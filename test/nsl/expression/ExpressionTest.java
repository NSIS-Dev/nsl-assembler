/*
 * ExpressionTest.java
 */

package nsl.expression;

import static nsl.NslTestSupport.evaluate;
import static org.junit.Assert.*;

import java.io.OutputStreamWriter;
import java.io.StringReader;
import nsl.NslException;
import nsl.ScriptParser;
import nsl.Tokenizer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the {@link nsl.expression.Expression} class.
 *
 * @author Stuart
 */
public class ExpressionTest {
	private static OutputStreamWriter outputStream;

	public ExpressionTest() {}

	@BeforeClass
	public static void setUpClass() throws Exception {
		outputStream = new OutputStreamWriter(System.out);
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		outputStream.close();
	}

	@Before
	public void setUp() {}

	@After
	public void tearDown() {}

	/** Test of isLiteral method, of class Expression. */
	@Test
	public void testIsLiteral() {
		boolean ja = 9 == 8 + 5;
		System.out.println("isLiteral");
		ScriptParser.pushTokenizer(
				new Tokenizer(
						new StringReader("1 'hello' $var = 9 == (8 + 5) false true blah(99, 100)"),
						"ExpressionTest"));
		Expression e;
		System.out.println("  " + (e = Expression.matchComplex()));
		assertEquals(true, e.isLiteral());
		System.out.println("  " + (e = Expression.matchComplex()));
		assertEquals(true, e.isLiteral());
		System.out.println("  " + (e = Expression.matchComplex()));
		assertEquals(false, e.isLiteral());
		System.out.println("  " + (e = Expression.matchComplex()));
		assertEquals(true, e.isLiteral());
		System.out.println("  " + (e = Expression.matchComplex()));
		assertEquals(true, e.isLiteral());
		System.out.println("  " + (e = Expression.matchComplex()));
		assertEquals(false, e.isLiteral());
	}

	/** Test of matchComplex method, of class Expression. */
	@Test
	public void testMatchComplex() {
		System.out.println("matchComplex");
		ScriptParser.pushTokenizer(
				new Tokenizer(
						new StringReader(
								"$var1 = $var2 = 3;\r\n"
										+ "$var1 = 0;\r\n"
										+ "$var2 = $var1 + $var1 + ($var1++) + 5 * $var1 - 3;\r\n"
										+ "$var2 = $var1 == 5 && $var2 == 3 || $var2 == 9 || ($var1 = 9) == 3;\r\n"
										+ "$var2 = $var1 <= 9 || $var2 <= 9 || $var2 >= 9 || $var2 > 9 && $var2 < 1 || $var1 == 9;\r\n"
										+ "$var2 = $var1 < 5 || $var1++ < 3 || $var2-- != 3 && $var1++ >= 5;\r\n"
										+ "$var2 = ($var1 | 5) == 34 || ($var2 | 3) == 99 || ($var2 & 2) == 2 && (($var3 = 3) ^ 3) == 5;\r\n"
										+ "$var2 = 99 + ($var2 ^= $var2 -= $var1 << 9);\r\n"
										+ "5 - (5 + 9) / 3 * (2 - 5) ^ 2 + 5 | (3 & 9);\r\n"
										+ "44 * 3 / 5 + 9 + 3 + 9 - 2;\r\n"
										+ "11 % 5 * 3 + 9 / ~4 - 3 - 4 + 2;\r\n"
										+ "9 << 2 >> 1 + (3 << 2) >> 1;\r\n"
										+ "true || false || true && false;\r\n"
										+ "true == false || false != true || true == false && false != true;\r\n"),
						"ExpressionTest"));

		String stringValue;
		System.out.println("  " + (stringValue = Expression.matchComplex().toString()));
		assertEquals("($var1 = ($var2 = 3))", stringValue);
		ScriptParser.tokenizer.matchEolOrDie();
		System.out.println("  " + (stringValue = Expression.matchComplex().toString()));
		assertEquals("($var1 = 0)", stringValue);
		ScriptParser.tokenizer.matchEolOrDie();
		System.out.println("  " + (stringValue = Expression.matchComplex().toString()));
		assertEquals(
				"($var2 = (((($var1 + $var1) + ($var1 = ($var1 + 1))) + (5 * $var1)) - 3))", stringValue);
		ScriptParser.tokenizer.matchEolOrDie();
		System.out.println("  " + (stringValue = Expression.matchComplex().toString()));
		assertEquals(
				"($var2 = (((($var1 == 5) && ($var2 == 3)) || ($var2 == 9)) || (($var1 = 9) == 3)))",
				stringValue);
		ScriptParser.tokenizer.matchEolOrDie();
		System.out.println("  " + (stringValue = Expression.matchComplex().toString()));
		assertEquals(
				"($var2 = ((((($var1 <= 9) || ($var2 <= 9)) || ($var2 >= 9)) || (($var2 > 9) && ($var2 < 1))) || ($var1 == 9)))",
				stringValue);
		ScriptParser.tokenizer.matchEolOrDie();
		System.out.println("  " + (stringValue = Expression.matchComplex().toString()));
		assertEquals(
				"($var2 = ((($var1 < 5) || (($var1 = ($var1 + 1)) < 3)) || ((($var2 = ($var2 - 1)) != 3) && (($var1 = ($var1 + 1)) >= 5))))",
				stringValue);
		ScriptParser.tokenizer.matchEolOrDie();
		System.out.println("  " + (stringValue = Expression.matchComplex().toString()));
		assertEquals(
				"($var2 = (((($var1 | 5) == 34) || (($var2 | 3) == 99)) || ((($var2 & 2) == 2) && ((($var3 = 3) ^ 3) == 5))))",
				stringValue);
		ScriptParser.tokenizer.matchEolOrDie();
		System.out.println("  " + (stringValue = Expression.matchComplex().toString()));
		assertEquals(
				"($var2 = (99 + ($var2 = ($var2 ^ ($var2 = ($var2 - ($var1 << 9)))))))", stringValue);
		ScriptParser.tokenizer.matchEolOrDie();

		int integerValue;
		System.out.println("  " + (integerValue = Expression.matchComplex().getIntegerValue()));
		assertEquals(5 - (5 + 9) / 3 * (2 - 5) ^ 2 + 5 | (3 & 9), integerValue);
		ScriptParser.tokenizer.matchEolOrDie();
		System.out.println("  " + (integerValue = Expression.matchComplex().getIntegerValue()));
		assertEquals(44 * 3 / 5 + 9 + 3 + 9 - 2, integerValue);
		ScriptParser.tokenizer.matchEolOrDie();
		System.out.println("  " + (integerValue = Expression.matchComplex().getIntegerValue()));
		assertEquals(11 % 5 * 3 + 9 / ~4 - 3 - 4 + 2, integerValue);
		ScriptParser.tokenizer.matchEolOrDie();
		System.out.println("  " + (integerValue = Expression.matchComplex().getIntegerValue()));
		assertEquals(9 << 2 >> 1 + (3 << 2) >> 1, integerValue);
		ScriptParser.tokenizer.matchEolOrDie();

		boolean booleanValue;
		System.out.println("  " + (booleanValue = Expression.matchComplex().getBooleanValue()));
		assertEquals(true || false || true && false, booleanValue);
		ScriptParser.tokenizer.matchEolOrDie();
		System.out.println("  " + (booleanValue = Expression.matchComplex().getBooleanValue()));
		assertEquals(true == false || false != true || true == false && false != true, booleanValue);
		ScriptParser.tokenizer.matchEolOrDie();
	}

	/** Test of the format() assemble time function, of class Expression. */
	@Test
	public void testFormat() {
		System.out.println("format");

		// A substitution that leaves the string shorter than it was.
		assertEquals("\"1\"", evaluate("format('{0}', 1)"));

		// One that leaves it longer, with and without literal text around it.
		assertEquals("\"a-LONGVALUE-b\"", evaluate("format('a-{0}-b', 'LONGVALUE')"));
		assertEquals(
				"\"AAAAAAAAAA-and-BBBBBBBBBB-end\"",
				evaluate("format('{0}-and-{1}-end', 'AAAAAAAAAA', 'BBBBBBBBBB')"));

		// Adjacent placeholders: nothing between them to resynchronise on.
		assertEquals("\"AAAAABBBBB\"", evaluate("format('{0}{1}', 'AAAAA', 'BBBBB')"));

		// An argument may be used more than once, and in any order.
		assertEquals("\"b a b\"", evaluate("format('{1} {0} {1}', 'a', 'b')"));

		// Inserted text is not rescanned, so a substituted brace stays literal.
		assertEquals("\"{0} x\"", evaluate("format('{0} {1}', '{0}', 'x')"));

		// {{ escapes a brace.
		assertEquals("\"{0}\"", evaluate("format('{{0}', 1)"));
		assertEquals("\"{x}\"", evaluate("format('{{{0}}', 'x')"));

		// Nothing to do.
		assertEquals("\"no placeholders\"", evaluate("format('no placeholders', 1)"));
	}

	/** Test of the errors reported by the format() assemble time function. */
	@Test
	public void testFormatErrors() {
		System.out.println("format errors");

		// An unterminated placeholder, with and without a parameter number, used to
		// run off the end of the string instead of being reported.
		assertFormatError("format('a{', 1)");
		assertFormatError("format('a{0', 1)");
		assertFormatError("format('{}', 1)");
		assertFormatError("format('{a}', 1)");
		assertFormatError("format('{5}', 1)");
	}

	/** Asserts that the given expression is rejected by the assembler. */
	private static void assertFormatError(String expression) {
		try {
			String result = evaluate(expression);
			fail(expression + " was accepted and gave " + result);
		} catch (NslException e) {
			System.out.println("  " + expression + " -> " + e.getMessage());
		}
	}
}
