/*
 * ConstantFoldingTest.java
 */

package nsl.expression;

import static nsl.NslTestSupport.assertRejected;
import static nsl.NslTestSupport.evaluate;
import static org.junit.Assert.*;

import nsl.NslTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the folding {@link Expression} does at parse time, where both operands are known.
 *
 * @author Jan T. Sott
 */
public class ConstantFoldingTest {
	@Before
	public void setUp() {
		NslTestSupport.resetParserState();
	}

	@After
	public void tearDown() {
		NslTestSupport.resetParserState();
	}

	/** Integer literals, including the DWORD range NSIS allows and Java's int does not. */
	@Test
	public void integerLiterals() {
		assertEquals("0", evaluate("0"));
		assertEquals("2147483647", evaluate("2147483647"));

		// Regression, 33b2f59: NSIS integers are DWORDs, so everything up to
		// 0xFFFFFFFF is a literal. It is parsed wide and narrowed to a negative int.
		assertEquals("-2147483648", evaluate("2147483648"));
		assertEquals("-1", evaluate("4294967295"));
	}

	/** Hexadecimal literals go through the same widen-then-narrow path. */
	@Test
	public void hexLiterals() {
		assertEquals("255", evaluate("0xFF"));
		assertEquals("2147483647", evaluate("0x7FFFFFFF"));
		assertEquals("-2147483648", evaluate("0x80000000"));
		assertEquals("-1", evaluate("0xFFFFFFFF"));
	}

	/** Past the DWORD range, and anything that is not a number at all. */
	@Test
	public void outOfRangeAndMalformedLiterals() {
		assertTrue(assertRejected("4294967296").getMessage().contains("does not fit in 32 bits"));
		assertTrue(assertRejected("0x100000000").getMessage().contains("does not fit in 32 bits"));

		// A word beginning with a digit is committed to the number path, so there is
		// no reading of these as identifiers to fall back on.
		assertTrue(assertRejected("12abc").getMessage().contains("Invalid number"));
		assertTrue(assertRejected("0xG").getMessage().contains("Invalid number"));
	}

	/** The two unary operators, which are matched as prefixes rather than as operators. */
	@Test
	public void unaryOperators() {
		assertEquals("-5", evaluate("~4"));
		assertEquals("-4", evaluate("-4"));

		// Both prefixes at once apply innermost-last: ~ folds first, then the minus
		// multiplies by -1, so this is -(~1) and not ~(-1).
		assertEquals("2", evaluate("~-1"));

		// ~ on a parenthesised expression takes a different path: it is folded as a
		// binary "~" whose right operand is ignored.
		assertEquals("-11", evaluate("~(4 + 6)"));

		// Unary minus is folded as a multiplication by -1.
		assertEquals("-10", evaluate("-(4 + 6)"));
	}

	/** Arithmetic, and the two divisors that have no answer. */
	@Test
	public void arithmetic() {
		assertEquals("7", evaluate("1 + 2 * 3"));
		assertEquals("9", evaluate("(1 + 2) * 3"));
		assertEquals("2", evaluate("7 / 3"));
		assertEquals("1", evaluate("7 % 3"));
		assertEquals("-2", evaluate("-7 / 3"));

		assertTrue(assertRejected("1 / 0").getMessage().contains("Division by zero"));
	}

	/**
	 * Modulo by zero is not checked the way division is, so it escapes as a raw {@link
	 * ArithmeticException} rather than as an {@code NslException} carrying a line number. Pinned as
	 * it stands; if the check is added, this test goes red and should become the same assertion as
	 * the division one above.
	 */
	@Test
	public void moduloByZeroEscapesUnchecked() {
		try {
			String result = evaluate("1 % 0");
			fail("1 % 0 was accepted and gave " + result);
		} catch (ArithmeticException ex) {
			assertEquals("/ by zero", ex.getMessage());
		}
	}

	/** Bitwise and shift operators. */
	@Test
	public void bitwiseOperators() {
		assertEquals("12", evaluate("8 | 4"));
		assertEquals("8", evaluate("12 & 9"));
		assertEquals("5", evaluate("12 ^ 9"));
		assertEquals("32", evaluate("8 << 2"));
		assertEquals("2", evaluate("8 >> 2"));

		// >> is arithmetic, so the sign bit is copied rather than shifted out.
		assertEquals("-1", evaluate("0xFFFFFFFF >> 8"));
	}

	/** Signed and unsigned comparisons of the same two literals disagree, and should. */
	@Test
	public void unsignedComparisonsReinterpretTheOperands() {
		// Regression, 506bf1d: the unsigned forms used to fold through Math.abs,
		// which gives the wrong answer for exactly these operands.
		assertEquals("false", evaluate("-1 > 1"));
		assertEquals("true", evaluate("-1 >u 1"));
		assertEquals("true", evaluate("-1 >=u 1"));
		assertEquals("false", evaluate("-1 <u 1"));
		assertEquals("false", evaluate("-1 <=u 1"));

		// -2 is the larger of the two unsigned, and the smaller signed.
		assertEquals("true", evaluate("-1 >u -2"));
		assertEquals("false", evaluate("-1 <u -2"));

		// Equality is unaffected by the reinterpretation.
		assertEquals("true", evaluate("-1 ==u 4294967295"));
		assertEquals("false", evaluate("-1 !=u 4294967295"));
	}

	/** Ordinary signed comparisons. */
	@Test
	public void integerComparisons() {
		assertEquals("true", evaluate("2 > 1"));
		assertEquals("true", evaluate("1 >= 1"));
		assertEquals("false", evaluate("1 < 1"));
		assertEquals("true", evaluate("1 <= 1"));
		assertEquals("true", evaluate("1 == 1"));
		assertEquals("false", evaluate("1 != 1"));
	}

	/** String comparisons fold too, case insensitively unless an S suffix says otherwise. */
	@Test
	public void stringComparisons() {
		assertEquals("true", evaluate("'abc' == 'ABC'"));
		assertEquals("false", evaluate("'abc' ==S 'ABC'"));
		assertEquals("true", evaluate("'abc' ==S 'abc'"));
		assertEquals("true", evaluate("'a' < 'b'"));
	}

	/** Concatenation with "." folds when both sides are known, whatever their types. */
	@Test
	public void concatenation() {
		assertEquals("\"ab\"", evaluate("'a' . 'b'"));
		assertEquals("\"a1\"", evaluate("'a' . 1"));
		assertEquals("\"12\"", evaluate("1 . 2"));
		assertEquals("\"atrue\"", evaluate("'a' . true"));

		// A backtick string is still escaped by default - setSpecialStringEscape(false)
		// is what turns it into an interpolating one - so its $ doubles like any other.
		assertEquals("\"a$$0\"", evaluate("'a' . `$0`"));
	}

	/** Boolean operators, and the operand a literal lets the parser discard. */
	@Test
	public void booleanFolding() {
		assertEquals("true", evaluate("true && true"));
		assertEquals("false", evaluate("true && false"));
		assertEquals("true", evaluate("false || true"));
		assertEquals("false", evaluate("false || false"));

		assertEquals("false", evaluate("!true"));
		assertEquals("true", evaluate("!(1 == 2)"));

		// A ternary on a known condition collapses to the branch taken.
		assertEquals("1", evaluate("true ? 1 : 2"));
		assertEquals("2", evaluate("false ? 1 : 2"));
	}

	/** "!" is only meaningful on a Boolean. */
	@Test
	public void logicalNegateRequiresABoolean() {
		assertTrue(
				assertRejected("!(1 + 1)")
						.getMessage()
						.contains("must be applied to a Boolean expression"));
	}
}
