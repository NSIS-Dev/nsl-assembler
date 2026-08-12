/*
 * FunctionInfoTest.java
 */

package nsl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link FunctionInfo}: overload resolution, the name NSIS ends up seeing, and the .onInit
 * check that decides whether ScriptParser may synthesise one.
 *
 * @author Jan T. Sott
 */
public class FunctionInfoTest {
	@Before
	public void setUp() {
		NslTestSupport.resetParserState();

		// resolveNsisName reports its failures as NslExceptions, which read the
		// current tokenizer to build their message.
		NslTestSupport.installTokenizer();
	}

	@After
	public void tearDown() {
		NslTestSupport.resetParserState();
	}

	/** Declares a function with the given number of parameters and return values. */
	private static FunctionInfo declare(String name, int params, int returns) {
		ArrayList<Register> registers = new ArrayList<Register>();
		for (int i = 0; i < params; i++) registers.add(RegisterList.getCurrent().get(i));

		FunctionInfo functionInfo = FunctionInfo.create(name, registers);
		functionInfo.setReturns(returns);
		return functionInfo;
	}

	/** find matches on name and parameter count, and on returns only when some are wanted. */
	@Test
	public void findMatchesOnTheSignature() {
		FunctionInfo none = declare("F", 0, 0);
		FunctionInfo one = declare("F", 1, 0);
		FunctionInfo returning = declare("G", 1, 1);

		assertSame(none, FunctionInfo.find("F", 0, 0));
		assertSame(one, FunctionInfo.find("F", 1, 0));
		assertNull(FunctionInfo.find("F", 2, 0));
		assertNull(FunctionInfo.find("H", 0, 0));

		// A call wanting a value only matches a function that returns that many.
		assertSame(returning, FunctionInfo.find("G", 1, 1));
		assertNull(FunctionInfo.find("G", 1, 2));

		// Wanting none matches whatever the function returns.
		assertSame(returning, FunctionInfo.find("G", 1, 0));

		// Names are matched case insensitively, as NSIS matches them.
		assertSame(none, FunctionInfo.find("f", 0, 0));
	}

	/** The name written into the .nsi carries the signature, so overloads do not collide. */
	@Test
	public void getNameManglesOverloads() {
		// Nothing to disambiguate: no parameters and no return values.
		assertEquals("F", declare("F", 0, 0).getName());

		assertEquals("G_1_0", declare("G", 1, 0).getName());
		assertEquals("G_1_2", declare("G", 1, 2).getName());

		// Callbacks keep the name NSIS calls them by, whatever their signature.
		assertEquals(".onInit", declare(".onInit", 0, 0).getName());

		// A function whose return count was never set is left alone as well.
		ArrayList<Register> none = new ArrayList<Register>();
		assertEquals("H", FunctionInfo.create("H", none).getName());
	}

	/** A reference with no argument list resolves by name alone. */
	@Test
	public void resolveNsisNameFindsTheOneFunction() {
		FunctionInfo installer = declare("F", 1, 0);

		assertEquals(installer.getName(), FunctionInfo.resolveNsisName("F", false, 1));
		assertEquals("F_1_0", FunctionInfo.resolveNsisName("F", false, 1));
	}

	/** From the uninstaller, the un. namespace is preferred and the bare name is the fallback. */
	@Test
	public void resolveNsisNamePrefersTheUninstallerNamespace() {
		declare("F", 1, 0);
		FunctionInfo uninstaller = declare("un.F", 1, 0);

		assertEquals(uninstaller.getName(), FunctionInfo.resolveNsisName("F", true, 1));
		assertEquals("un.F_1_0", FunctionInfo.resolveNsisName("F", true, 1));

		// Installer code never reaches the un. namespace.
		assertEquals("F_1_0", FunctionInfo.resolveNsisName("F", false, 1));
	}

	/** With no un. flavour declared, uninstaller code falls back to the installer function. */
	@Test
	public void resolveNsisNameFallsBackToTheBareName() {
		declare("F", 1, 0);
		assertEquals("F_1_0", FunctionInfo.resolveNsisName("F", true, 1));
	}

	/** An overloaded name cannot be resolved without a call signature to pick with. */
	@Test
	public void resolveNsisNameRejectsAnOverload() {
		declare("F", 0, 0);
		declare("F", 1, 0);

		try {
			String resolved = FunctionInfo.resolveNsisName("F", false, 7);
			fail("an overloaded name resolved to " + resolved);
		} catch (NslException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("is overloaded"));
		}
	}

	/** A name that was never declared. */
	@Test
	public void resolveNsisNameRejectsAnUnknownFunction() {
		try {
			String resolved = FunctionInfo.resolveNsisName("Nope", false, 1);
			fail("an undeclared name resolved to " + resolved);
		} catch (NslException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("not found"));
		}
	}

	/** The bitmask ScriptParser reads before synthesising .onInit or un.onInit. */
	@Test
	public void isOnInitDefinedReportsBothFlavours() {
		assertEquals(0, FunctionInfo.isOnInitDefined());

		declare(".onInit", 0, 0);
		assertEquals(1, FunctionInfo.isOnInitDefined());

		FunctionInfo.getList().clear();
		declare("un.onInit", 0, 0);
		assertEquals(2, FunctionInfo.isOnInitDefined());

		declare(".onInit", 0, 0);
		assertEquals(3, FunctionInfo.isOnInitDefined());
	}

	/** The comparison is case insensitive, so ".oninit" counts too. */
	@Test
	public void isOnInitDefinedIgnoresCase() {
		declare(".oninit", 0, 0);
		declare("UN.ONINIT", 0, 0);
		assertEquals(3, FunctionInfo.isOnInitDefined());
	}
}
