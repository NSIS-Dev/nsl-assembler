/*
 * RegisterListTest.java
 */

package nsl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link RegisterList}, the pool $0-$9 and $R0-$R9 are handed out from.
 *
 * @author Jan T. Sott
 */
public class RegisterListTest {
	/** The number of built in NSIS registers, $0-$9 and $R0-$R9. */
	private static final int BUILT_IN = 20;

	@Before
	public void setUp() {
		NslTestSupport.resetParserState();
	}

	@After
	public void tearDown() {
		NslTestSupport.resetParserState();
	}

	/** Temporaries are handed out in declaration order, $0 first and $R9 last. */
	@Test
	public void getNextAllocatesInOrder() {
		RegisterList list = new RegisterList();

		assertEquals("$0", list.getNext().toString());
		assertEquals("$1", list.getNext().toString());
		assertEquals("$2", list.getNext().toString());

		for (int i = 3; i < 10; i++) list.getNext();
		assertEquals("$R0", list.getNext().toString());
	}

	/** Releasing a register with setInUse(false) puts it back at the front of the queue. */
	@Test
	public void releasedRegistersAreReused() {
		RegisterList list = new RegisterList();

		Register first = list.getNext();
		Register second = list.getNext();
		assertEquals("$0", first.toString());
		assertEquals("$1", second.toString());

		first.setInUse(false);
		assertSame(first, list.getNext());

		// The scan restarts each time, so the lowest free register wins whatever
		// order things were released in.
		second.setInUse(false);
		assertSame(second, list.getNext());
	}

	/** $INSTDIR and $OUTDIR are RegisterType.Other and are never handed out as temporaries. */
	@Test
	public void exhaustionIsReported() {
		RegisterList list = new RegisterList();
		for (int i = 0; i < BUILT_IN; i++) list.getNext();

		try {
			Register extra = list.getNext();
			fail("allocated " + extra + " after the pool was exhausted");
		} catch (NslException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("Out of registers"));
		}
	}

	/** setAllInUse covers the built in registers only, and stops at the first that is not one. */
	@Test
	public void setAllInUseCoversTheBuiltInRegisters() {
		RegisterList list = new RegisterList();

		list.setAllInUse(true);
		try {
			list.getNext();
			fail("allocated a register after setAllInUse(true)");
		} catch (NslException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("Out of registers"));
		}

		list.setAllInUse(false);
		assertEquals("$0", list.getNext().toString());
	}

	/** add(name) is idempotent: the same name always gives back the same index. */
	@Test
	public void addReturnsAStableIndex() {
		RegisterList list = new RegisterList();

		int index = list.add("$myVar");
		assertEquals(BUILT_IN + 2, index); // After $INSTDIR and $OUTDIR.
		assertEquals("$myVar", list.get(index).toString());
		assertEquals(RegisterType.Variable, list.get(index).getRegisterType());

		assertEquals(index, list.add("$myVar"));
		assertEquals(BUILT_IN + 3, list.add("$other"));

		// A built in register named through add() is marked in use rather than
		// duplicated, so it stops being available as a temporary.
		assertEquals(0, list.add("$0"));
		assertEquals("$1", list.getNext().toString());
	}

	/**
	 * ISSUES.md #5: getNext() skips only RegisterType.Other, so a user variable is eligible once
	 * something has released it - and instructions do release whatever getRegisterOrExpression()
	 * handed back, including a plain user register. Pinned as it stands; a fix makes getNext() throw
	 * here instead.
	 */
	@Test
	public void getNextCanHandOutAUserVariable() {
		RegisterList list = new RegisterList();
		int index = list.add("$myVar");
		for (int i = 0; i < BUILT_IN; i++) list.getNext();

		// A user variable is created in use, so until it is released the pool is
		// correctly reported as exhausted.
		try {
			list.getNext();
			fail("allocated a register while $myVar was in use");
		} catch (NslException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("Out of registers"));
		}

		list.get(index).setInUse(false);
		assertEquals("$myVar", list.getNext().toString());
	}

	/** getCurrent/setCurrent swap the whole pool, which is how the tests keep each other clean. */
	@Test
	public void currentListIsSwappable() {
		RegisterList list = new RegisterList();
		RegisterList.setCurrent(list);
		assertSame(list, RegisterList.getCurrent());

		assertEquals("$0", RegisterList.getCurrent().getNext().toString());
		RegisterList.setCurrent(new RegisterList());
		assertEquals("$0", RegisterList.getCurrent().getNext().toString());
	}
}
