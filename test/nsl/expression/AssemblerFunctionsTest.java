/*
 * AssemblerFunctionsTest.java
 */

package nsl.expression;

import static nsl.NslTestSupport.assertRejected;
import static nsl.NslTestSupport.evaluate;
import static org.junit.Assert.*;

import java.util.ArrayList;
import nsl.NslTestSupport;
import nsl.Register;
import nsl.RegisterList;
import nsl.ScriptParser;
import nsl.preprocessor.DefineList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the assembler functions that run at parse time: toint, length, type, defined, eval and
 * returnvar. format is covered by {@link ExpressionTest} and is not repeated here.
 *
 * @author Jan T. Sott
 */
public class AssemblerFunctionsTest {
	@Before
	public void setUp() {
		NslTestSupport.resetParserState();
	}

	@After
	public void tearDown() {
		// DefineList has no way to swap or clear the current list, so anything a test
		// defines has to be taken back out by name.
		DefineList.getCurrent().remove("FOO");
		DefineList.getCurrent().remove("BAR");
		NslTestSupport.resetParserState();
	}

	/** toint on the types it converts. */
	@Test
	public void toint() {
		assertEquals("42", evaluate("toint('42')"));
		assertEquals("-42", evaluate("toint('-42')"));
		assertEquals("1", evaluate("toint(true)"));
		assertEquals("0", evaluate("toint(false)"));

		// An integer passes straight through; a register gives its internal index,
		// which for $0 is 0 because it is the first register the list is built with.
		assertEquals("7", evaluate("toint(7)"));
		assertEquals("0", evaluate("toint($0)"));
		assertEquals("10", evaluate("toint($R0)"));
	}

	/** The second argument replaces both the failure case and, oddly, a false Boolean. */
	@Test
	public void tointDefaultValue() {
		assertEquals("0", evaluate("toint('nonsense')"));
		assertEquals("7", evaluate("toint('nonsense', 7)"));

		// false is routed through the same "no value" return as a parse failure, so a
		// default displaces it. true is not.
		assertEquals("7", evaluate("toint(false, 7)"));
		assertEquals("1", evaluate("toint(true, 7)"));
	}

	/**
	 * toint reads a string with the same spellings a number written into the source has, both going
	 * through {@link nsl.Tokenizer#parseNumber}.
	 */
	@Test
	public void tointHex() {
		assertEquals("255", evaluate("toint('0xFF')"));
		assertEquals("255", evaluate("toint('0xff')"));
		assertEquals("-16", evaluate("toint('-0x10')"));

		// NSIS integers are DWORDs, so the top half of the range wraps negative -
		// the same value the literal 0xFFFFFFFF carries.
		assertEquals("-1", evaluate("toint('0xFFFFFFFF')"));
		assertEquals("7", evaluate("toint('0x1FFFFFFFF', 7)"));

		// A bare "FF" is not hexadecimal: reading it as such would leave "11"
		// ambiguous. The prefix is what selects the radix.
		assertEquals("7", evaluate("toint('FF', 7)"));
		assertEquals("11", evaluate("toint('11')"));
	}

	/** toint rejects the wrong arity and a non-integer default. */
	@Test
	public void tointArgumentErrors() {
		assertRejected("toint()");
		assertRejected("toint('1', 2, 3)");
		assertRejected("toint('1', 'x')");
	}

	/** length measures the escaped form, which is what ends up in the .nsi. */
	@Test
	public void length() {
		assertEquals("3", evaluate("length($R0)"));
		assertEquals("2", evaluate("length(99)"));
		assertEquals("4", evaluate("length('true')"));
		assertEquals("5", evaluate("length(false)"));

		// $ becomes $$ and a quote becomes $\", so both count for more than the one
		// character written in the source.
		assertEquals("2", evaluate("length('$')"));
		assertEquals("3", evaluate("length('\"')"));
		assertEquals("0", evaluate("length('')"));
	}

	/** length needs a literal, and exactly one of them. */
	@Test
	public void lengthArgumentErrors() {
		assertRejected("length()");
		assertRejected("length('a', 'b')");
		assertRejected("length($0 + 1)");
	}

	/** type reports the expression type as a string, and Nonliteral for anything computed. */
	@Test
	public void type() {
		assertEquals("\"Integer\"", evaluate("type(1)"));
		assertEquals("\"String\"", evaluate("type('a')"));
		assertEquals("\"String\"", evaluate("type(`a`)"));
		assertEquals("\"Boolean\"", evaluate("type(true)"));
		assertEquals("\"Register\"", evaluate("type($0)"));
		assertEquals("\"Register\"", evaluate("type($myVar)"));

		// Folding happens before type sees the argument, so a computed but knowable
		// expression still reports its folded type.
		assertEquals("\"Integer\"", evaluate("type(1 + 2)"));
		assertEquals("\"Nonliteral\"", evaluate("type($0 + 1)"));
	}

	/** defined is true only when every name in the list is defined. */
	@Test
	public void defined() {
		DefineList.getCurrent().add("FOO", Expression.fromInteger(1));
		DefineList.getCurrent().add("BAR", Expression.fromInteger(2));

		assertEquals("true", evaluate("defined(FOO)"));
		assertEquals("true", evaluate("defined(FOO, BAR)"));
		assertEquals("false", evaluate("defined(NOPE)"));
		assertEquals("false", evaluate("defined(FOO, NOPE)"));
		assertEquals("false", evaluate("defined(NOPE, FOO)"));
	}

	/** defined needs at least one name. */
	@Test
	public void definedArgumentErrors() {
		assertTrue(assertRejected("defined()").getMessage().contains("one or more constant names"));
	}

	/** eval parses its argument as source, through a tokenizer pushed onto the stack. */
	@Test
	public void eval() {
		assertEquals("3", evaluate("eval('1 + 2')"));
		assertEquals("\"abc\"", evaluate("eval('\\'abc\\'')"));
		assertEquals("true", evaluate("eval('1 == 1')"));
	}

	/**
	 * A nested eval pushes a second tokenizer while the first is still live. Both unwind, or the
	 * stack would be left deeper than it started and the next statement would be read from the wrong
	 * source.
	 */
	@Test
	public void nestedEvalUnwindsTheTokenizerStack() {
		int depth = ScriptParser.tokenizers.size();
		assertEquals("3", evaluate("eval(\"eval('1 + 2')\")"));
		assertEquals(depth, ScriptParser.tokenizers.size());
	}

	/** eval takes one string. */
	@Test
	public void evalArgumentErrors() {
		assertRejected("eval()");
		assertRejected("eval(1)");
		assertRejected("eval('1', '2')");
	}

	/** returnvar resolves against the registers the current assignment is writing into. */
	@Test
	public void returnvar() {
		ArrayList<Register> registers = new ArrayList<Register>();
		registers.add(RegisterList.getCurrent().get(0));
		registers.add(RegisterList.getCurrent().get(1));
		ReturnVarExpression.setRegisters(registers);

		assertEquals("$0", evaluate("returnvar(1)"));
		assertEquals("$1", evaluate("returnvar(2)"));
	}

	/**
	 * Both returnvar failures are raised from toString rather than from the constructor, so they land
	 * when the expression is written out and not when it is parsed.
	 */
	@Test
	public void returnvarMisuse() {
		// No assignment in progress at all.
		assertTrue(assertRejected("returnvar(1)").getMessage().contains("no return registers"));

		ReturnVarExpression.setRegisters(RegisterList.getCurrent().get(0));
		assertTrue(assertRejected("returnvar(2)").getMessage().contains("out of range"));
		assertTrue(assertRejected("returnvar(0)").getMessage().contains("out of range"));

		// Wrong arity and a non-integer index are caught at parse time.
		assertRejected("returnvar()");
		assertRejected("returnvar(1, 2)");
		assertRejected("returnvar('x')");
	}
}
