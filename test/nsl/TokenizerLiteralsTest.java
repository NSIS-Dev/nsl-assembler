/*
 * TokenizerLiteralsTest.java
 */

package nsl;

import static org.junit.Assert.*;

import java.io.StringReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link Tokenizer} on literals, comments and line numbering.
 *
 * <p>Every tokenizer here is standalone: setAutoPop(false) keeps it off ScriptParser's stack, which
 * it would otherwise pop from on reaching the end of its input. It is still installed as {@code
 * ScriptParser.tokenizer}, because {@link NslExpectedException} builds its message out of whatever
 * that static holds rather than out of the tokenizer that raised it - so a tokenizer that is not
 * the current one cannot report an error without a NullPointerException.
 *
 * @author Jan T. Sott
 */
public class TokenizerLiteralsTest {
	@Before
	public void setUp() {
		NslTestSupport.resetParserState();
	}

	@After
	public void tearDown() {
		NslTestSupport.resetParserState();
	}

	/** Builds a tokenizer over the given source, positioned on its first token. */
	private static Tokenizer tokenizerOver(String source) {
		Tokenizer tokenizer = new Tokenizer(new StringReader(source), "test");
		tokenizer.setAutoPop(false);
		ScriptParser.tokenizer = tokenizer;
		tokenizer.tokenNext();
		return tokenizer;
	}

	/** All three quote characters produce a string token. */
	@Test
	public void allThreeQuoteStylesAreStrings() {
		Tokenizer tokenizer = tokenizerOver("\"a\" 'b' `c`");

		assertTrue(tokenizer.tokenIsString());
		assertEquals("a", tokenizer.sval);
		assertEquals('"', tokenizer.ttype);

		tokenizer.tokenNext();
		assertEquals("b", tokenizer.sval);
		assertEquals('\'', tokenizer.ttype);

		tokenizer.tokenNext();
		assertEquals("c", tokenizer.sval);
		assertEquals('`', tokenizer.ttype);
	}

	/** Backslash escapes are resolved inside every quote style, by StreamTokenizer itself. */
	@Test
	public void escapeSequencesAreResolved() {
		assertEquals("a\nb", tokenizerOver("\"a\\nb\"").sval);
		assertEquals("a\tb", tokenizerOver("'a\\tb'").sval);
		assertEquals("a\rb", tokenizerOver("`a\\rb`").sval);
		assertEquals("a\\b", tokenizerOver("\"a\\\\b\"").sval);

		// A quote of a different style needs no escaping.
		assertEquals("it's", tokenizerOver("\"it's\"").sval);
	}

	/** Strings prefixed with "at" are read raw: the backslash stays a backslash. */
	@Test
	public void rawStringsDoNotProcessEscapes() {
		Tokenizer tokenizer = tokenizerOver("@\"a\\nb\"");
		assertTrue(tokenizer.tokenIsString());
		assertEquals("a\\nb", tokenizer.sval);

		assertEquals("a\\tb", tokenizerOver("@'a\\tb'").sval);
		assertEquals("a\\rb", tokenizerOver("@`a\\rb`").sval);
	}

	/** An unterminated raw string runs to the end of the input and is reported there. */
	@Test
	public void unterminatedRawStringIsReported() {
		try {
			tokenizerOver("@\"never closed");
			fail("an unterminated raw string was accepted");
		} catch (NslExpectedException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("\""));
		}
	}

	/** The raw string prefix must be followed by a quote character. */
	@Test
	public void rawStringPrefixNeedsAQuote() {
		try {
			tokenizerOver("@abc");
			fail("@ without a quote was accepted");
		} catch (NslExpectedException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("a string"));
		}

		try {
			tokenizerOver("@");
			fail("a trailing @ was accepted");
		} catch (NslExpectedException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("a string"));
		}
	}

	/** Both comment styles are skipped. */
	@Test
	public void commentsAreSkipped() {
		Tokenizer tokenizer = tokenizerOver("a // comment\nb /* comment */ c");
		assertEquals("a", tokenizer.sval);
		tokenizer.tokenNext();
		assertEquals("b", tokenizer.sval);
		tokenizer.tokenNext();
		assertEquals("c", tokenizer.sval);
	}

	/**
	 * "#" is a word character, not a comment character: it is how directives such as #define are
	 * spelled, so a line beginning with one is a token and not a comment.
	 */
	@Test
	public void hashIsPartOfAWord() {
		Tokenizer tokenizer = tokenizerOver("#define A");
		assertTrue(tokenizer.tokenIsWord());
		assertEquals("#define", tokenizer.sval);
		tokenizer.tokenNext();
		assertEquals("A", tokenizer.sval);
	}

	/** lineno() counts from 1 and follows the token, since every diagnostic quotes it. */
	@Test
	public void linenoTracksTheCurrentToken() {
		Tokenizer tokenizer = tokenizerOver("a\nb\n\nc");
		assertEquals(1, tokenizer.lineno());
		tokenizer.tokenNext();
		assertEquals(2, tokenizer.lineno());
		tokenizer.tokenNext();
		assertEquals(4, tokenizer.lineno());
	}

	/** A comment spanning lines still moves the count on. */
	@Test
	public void linenoCountsLinesInsideComments() {
		Tokenizer tokenizer = tokenizerOver("a /* one\ntwo\n */ b");
		assertEquals(1, tokenizer.lineno());
		tokenizer.tokenNext();
		assertEquals(3, tokenizer.lineno());
	}

	/**
	 * readUntil consumes newlines behind StreamTokenizer's back, so it keeps its own lineNumberAdd
	 * for them. Without it every line after a raw string or an #nsis block would be misreported.
	 */
	@Test
	public void linenoIncludesLinesConsumedByReadUntil() {
		Tokenizer tokenizer = tokenizerOver("@\"one\ntwo\nthree\" x");
		assertEquals("one\ntwo\nthree", tokenizer.sval);
		assertEquals(3, tokenizer.lineno());

		tokenizer.tokenNext();
		assertEquals("x", tokenizer.sval);
		assertEquals(3, tokenizer.lineno());
	}

	/** With auto-pop off, running out of input is reported rather than papered over. */
	@Test
	public void endOfInputWithoutAutoPop() {
		Tokenizer tokenizer = tokenizerOver("a");
		assertFalse(tokenizer.tokenNext());

		try {
			tokenizer.tokenNext("a token");
			fail("the end of the input was accepted");
		} catch (NslExpectedException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("a token"));
		}
	}
}
