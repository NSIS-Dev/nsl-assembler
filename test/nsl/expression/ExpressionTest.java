/*
 * ExpressionTest.java
 */

package nsl.expression;

import static org.junit.Assert.*;

import java.io.OutputStreamWriter;
import java.io.StringReader;
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
}
