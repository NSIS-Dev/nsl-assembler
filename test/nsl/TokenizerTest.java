/*
 * TokenizerTest.java
 */

package nsl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the {@link nsl.Tokenizer} class.
 *
 * @author Stuart
 */
public class TokenizerTest {
	public TokenizerTest() {}

	@BeforeClass
	public static void setUpClass() throws Exception {}

	@AfterClass
	public static void tearDownClass() throws Exception {}

	@Before
	public void setUp() {}

	@After
	public void tearDown() throws IOException {}

	/** Test of tokenIs method, of class Tokenizer. */
	@Test
	public void testTokenIs_String() {
		System.out.println("tokenIs");
		Tokenizer instance = new Tokenizer(new StringReader("hello\nhello\n"), "TokenizerTest");
		instance.tokenNext();
		assertEquals(instance.tokenIs("hello"), true);
		instance.tokenNext();
		assertEquals(instance.tokenIs("goodbye"), false);
	}

	/** Test of tokenIs method, of class Tokenizer. */
	@Test
	public void testTokenIs_char() {
		System.out.println("tokenIs");
		Tokenizer instance = new Tokenizer(new StringReader("+\n- *( a\n"), "TokenizerTest");
		instance.tokenNext();
		assertEquals(instance.tokenIs('+'), true);
		instance.tokenNext();
		assertEquals(instance.tokenIs('-'), true);
		instance.tokenNext();
		assertEquals(instance.tokenIs('*'), true);
		instance.tokenNext();
		assertEquals(instance.tokenIs('('), true);
		instance.tokenNext();
		assertEquals(instance.tokenIs('a'), false); // a is a word
	}

	/** Test of tokenIsChar method, of class Tokenizer. */
	@Test
	public void testTokenIsChar() {
		System.out.println("tokenIsChar");
		Tokenizer instance = new Tokenizer(new StringReader("+blah- blah\n"), "TokenizerTest");
		instance.tokenNext();
		assertEquals(instance.tokenIsChar(), true);
		instance.tokenNext();
		assertEquals(instance.tokenIsChar(), false);
		instance.tokenNext();
		assertEquals(instance.tokenIsChar(), true);
		instance.tokenNext();
		assertEquals(instance.tokenIsChar(), false);
	}

	/** Test of tokenIsWord method, of class Tokenizer. */
	@Test
	public void testTokenIsWord() {
		System.out.println("tokenIsWord");
		Tokenizer instance = new Tokenizer(new StringReader("+blah 99 blah\n"), "TokenizerTest");
		instance.tokenNext();
		assertEquals(instance.tokenIsWord(), false);
		instance.tokenNext();
		assertEquals(instance.tokenIsWord(), true);
		instance.tokenNext();
		assertEquals(instance.tokenIsWord(), false);
		instance.tokenNext();
		assertEquals(instance.tokenIsWord(), true);
	}

	/** Test of tokenIsNumber method, of class Tokenizer. */
	@Test
	public void testTokenIsNumber() {
		System.out.println("tokenIsNumber");
		Tokenizer instance = new Tokenizer(new StringReader("+8 1\n0 hello"), "TokenizerTest");
		instance.tokenNext();
		assertEquals(instance.tokenIsNumber(), false);
		instance.tokenNext();
		assertEquals(instance.tokenIsNumber(), true);
		instance.tokenNext();
		assertEquals(instance.tokenIsNumber(), true);
		instance.tokenNext();
		assertEquals(instance.tokenIsNumber(), true);
		instance.tokenNext();
		assertEquals(instance.tokenIsNumber(), false);
	}

	/** Test of tokenIsString method, of class Tokenizer. */
	@Test
	public void testTokenIsString() {
		System.out.println("tokenIsString");
		Tokenizer instance =
				new Tokenizer(
						new StringReader("this_is_a_word \"this is a string\" 'so is this!' `and this`"),
						"TokenizerTest");
		instance.tokenNext();
		assertEquals(instance.tokenIsString(), false);
		instance.tokenNext();
		assertEquals(instance.tokenIsString(), true);
		instance.tokenNext();
		assertEquals(instance.tokenIsString(), true);
		instance.tokenNext();
		assertEquals(instance.tokenIsString(), true);
	}

	/** Test of tokenNext method, of class Tokenizer. */
	@Test
	public void testTokenNext() {
		System.out.println("tokenNext");
		Tokenizer instance = new Tokenizer(new StringReader("a b"), "TokenizerTest");
		instance.tokenNext();
		assertEquals(instance.tokenIs("a"), true);
		instance.tokenNext();
		assertEquals(instance.tokenIs("b"), true);
		assertEquals(instance.tokenNext(), false);
	}

	/** Test of matchOrDie method, of class Tokenizer. */
	@Test
	public void testMatchOrDie_char() {
		System.out.println("matchOrDie");
		Tokenizer instance = new Tokenizer(new StringReader("! ~"), "TokenizerTest");
		instance.tokenNext();
		instance.matchOrDie('!');
		instance.matchOrDie('~');
	}

	/** Test of matchOrDie method, of class Tokenizer. */
	@Test
	public void testMatchOrDie_String() {
		System.out.println("matchOrDie");
		Tokenizer instance = new Tokenizer(new StringReader("hello world"), "TokenizerTest");
		instance.tokenNext();
		instance.matchOrDie("hello");
		instance.matchOrDie("world");
	}

	/** Test of match method, of class Tokenizer. */
	@Test
	public void testMatch_char() {
		System.out.println("match");
		Tokenizer instance = new Tokenizer(new StringReader("! ~"), "TokenizerTest");
		instance.tokenNext();
		assertEquals(instance.match('!'), true);
		assertEquals(instance.match('~'), true);
	}

	/** Test of match method, of class Tokenizer. */
	@Test
	public void testMatch_String() {
		System.out.println("match");
		Tokenizer instance = new Tokenizer(new StringReader("hello world"), "TokenizerTest");
		instance.tokenNext();
		assertEquals(instance.match("hello"), true);
		assertEquals(instance.match("world"), true);
	}

	/** Test of matchEolOrDie method, of class Tokenizer. */
	@Test
	public void testMatchEolOrDie() {
		System.out.println("matchEolOrDie");
		Tokenizer instance = new Tokenizer(new StringReader("hello();"), "TokenizerTest");
		instance.tokenNext();
		assertEquals(instance.match("hello"), true);
		assertEquals(instance.match('('), true);
		assertEquals(instance.match(')'), true);
		instance.matchEolOrDie();
	}

	/** Test of matchAWord method, of class Tokenizer. */
	@Test
	public void testMatchAWord() {
		System.out.println("matchAWord");
		Tokenizer instance = new Tokenizer(new StringReader("hello there;"), "TokenizerTest");
		instance.tokenNext();
		String result = instance.matchAWord();
		assertEquals(result.compareTo("hello"), 0);
		result = instance.matchAWord();
		assertEquals(result.compareTo("there"), 0);
		instance.matchEolOrDie();
	}

	/** Test of matchAString method, of class Tokenizer. */
	@Test
	public void testMatchAString() {
		System.out.println("matchAString");
		Tokenizer instance = new Tokenizer(new StringReader("\"hello\" 'the\\'re';"), "TokenizerTest");
		instance.tokenNext();
		String result = instance.matchAString();
		assertEquals(result.compareTo("hello"), 0);
		result = instance.matchAString();
		assertEquals(result.compareTo("the're"), 0);
		instance.matchEolOrDie();
	}
}
