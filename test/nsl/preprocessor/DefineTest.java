/*
 * DefineTest.java
 */

package nsl.preprocessor;

import static nsl.NslTestSupport.evaluate;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import nsl.NslTestSupport;
import nsl.expression.Expression;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link DefineList}, and what the expression engine does with the {@link Expression} objects
 * it hands out - which are shared, so anything that mutates one rewrites the constant.
 *
 * @author Jan T. Sott
 */
public class DefineTest {
	@Before
	public void setUp() {
		NslTestSupport.resetParserState();
	}

	@After
	public void tearDown() {
		// The current list is a static with no setter, so tests clean up by name.
		DefineList.getCurrent().remove("B");
		DefineList.getCurrent().remove("N");
		NslTestSupport.resetParserState();
	}

	/** add reports whether it added or replaced, and counts only the additions. */
	@Test
	public void addReportsReplacement() {
		DefineList list = new DefineList();
		assertEquals(0, list.getCount());

		assertTrue(list.add("A", Expression.fromInteger(1)));
		assertEquals(1, list.getCount());

		assertFalse(list.add("A", Expression.fromInteger(2)));
		assertEquals(1, list.getCount());
		assertEquals("2", list.get("A").toString());

		assertTrue(list.add("B", Expression.fromInteger(3)));
		assertEquals(2, list.getCount());
	}

	/**
	 * remove reports whether it removed anything, but never decrements the counter - so getCount is a
	 * count of adds, not of live constants. Pinned as it stands; ISSUES.md notes getCount is dead
	 * code, which is why nothing has noticed.
	 */
	@Test
	public void removeDoesNotDecrementTheCount() {
		DefineList list = new DefineList();
		list.add("A", Expression.fromInteger(1));

		assertTrue(list.remove("A"));
		assertNull(list.get("A"));
		assertFalse(list.remove("A"));

		assertEquals(1, list.getCount());
	}

	/**
	 * getNames returns names in the order they were defined. NSISDirective emits !define/!undef in
	 * that order, so a hash ordering here would reorder the output.
	 */
	@Test
	public void getNamesKeepsInsertionOrder() {
		DefineList list = new DefineList();
		list.add("Z", Expression.fromInteger(1));
		list.add("A", Expression.fromInteger(2));
		list.add("M", Expression.fromInteger(3));

		assertEquals(new ArrayList<String>(java.util.Arrays.asList("Z", "A", "M")), names(list));

		// Redefining moves nothing: LinkedHashMap keeps the first insertion's place.
		list.add("Z", Expression.fromInteger(4));
		assertEquals(new ArrayList<String>(java.util.Arrays.asList("Z", "A", "M")), names(list));

		// Removing and re-adding does move it to the end.
		list.remove("A");
		list.add("A", Expression.fromInteger(5));
		assertEquals(new ArrayList<String>(java.util.Arrays.asList("Z", "M", "A")), names(list));
	}

	/**
	 * lookup consults the current macro's list before the global one. Only the global half is
	 * reachable here: MacroEvaluated.current is set solely by evaluating a real macro, so the
	 * shadowing half belongs in a codegen test.
	 */
	@Test
	public void lookupReadsTheGlobalList() {
		assertNull(DefineList.lookup("N"));

		DefineList.getCurrent().add("N", Expression.fromInteger(9));
		assertEquals("9", DefineList.lookup("N").toString());
		assertEquals("9", evaluate("N"));
	}

	/**
	 * Regression, e1977c8: "!" used to flip booleanValue in place, and the object it flipped was the
	 * one DefineList holds - so a constant read after a negation of it had the wrong value.
	 */
	@Test
	public void negatingAConstantDoesNotRewriteIt() {
		DefineList.getCurrent().add("B", Expression.fromBoolean(true));

		assertEquals("false", evaluate("!B"));
		assertEquals("true", evaluate("B"));

		// Negating twice must not accumulate either.
		assertEquals("false", evaluate("!B"));
		assertEquals("false", evaluate("!B"));
		assertEquals("true", evaluate("B"));
		assertEquals("true", DefineList.lookup("B").toString());
	}

	/** The same sharing applies to a constant used in a folded comparison. */
	@Test
	public void comparingAConstantDoesNotRewriteIt() {
		DefineList.getCurrent().add("N", Expression.fromInteger(-1));

		assertEquals("true", evaluate("N >u 1"));
		assertEquals("-1", evaluate("N"));
		assertEquals("-1", DefineList.lookup("N").toString());
	}

	private static ArrayList<String> names(DefineList list) {
		ArrayList<String> names = new ArrayList<String>();
		for (Iterator<String> i = list.getNames().iterator(); i.hasNext(); ) names.add(i.next());
		return names;
	}
}
