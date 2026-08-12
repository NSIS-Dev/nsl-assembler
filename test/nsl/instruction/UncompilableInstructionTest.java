/*
 * UncompilableInstructionTest.java
 */

package nsl.instruction;

import static nsl.Assembler.*;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The instructions the e2e corpus cannot reach, and the one that needs a page.
 *
 * <p>{@code LogSet} and {@code LogText} need an NSIS built with NSIS_CONFIG_LOG, {@code Int64Fmt}
 * needs a 64-bit target, and {@code GetDLLVersionLocal} reads a real DLL while compiling. makensis
 * rejects all four against the stock build, so e2e/KNOWN-GAPS.md records them as uncovered.
 *
 * <p>This tier does not run makensis, only the assembler - which emits the lines regardless. So the
 * wrappers can be pinned here even though nothing downstream can compile them, and this is the only
 * coverage they have.
 *
 * @author Jan T. Sott
 */
public class UncompilableInstructionTest {
	private static String nsi;
	private static String pageNsi;

	@BeforeClass
	public static void assembleOnce() throws Exception {
		nsi =
				assemble(
						inSection(
								"  LogSet(\"on\");",
								"  LogText(\"hello\");",
								"  $R0 = Int64Fmt(\"%I64u\", \"4294967296\");",
								"  ($R1, $R2) = GetDLLVersionLocal(\"a.dll\");"));
		pageNsi =
				assemble(
						lines(
								"Name(\"t\");",
								"OutFile(\"t.exe\");",
								"",
								"page Directory(\"\")",
								"{",
								"  DirVar($INSTDIR);",
								"  DirText(\"in a page\");",
								"}",
								"",
								"section Test(\"t\")",
								"{",
								"  DetailPrint(\"x\");",
								"}"));
	}

	/** Takes on/off as a string rather than as a Boolean, unlike most toggles in the language. */
	@Test
	public void LogSet() {
		assertThat(nsi, containsLine("LogSet \"on\""));
	}

	@Test
	public void LogText() {
		assertThat(nsi, containsLine("LogText \"hello\""));
	}

	/** Same shape as IntFmt: output variable, format, input. */
	@Test
	public void Int64Fmt() {
		assertThat(nsi, containsLine("Int64Fmt $R0 \"%I64u\" \"4294967296\""));
	}

	/** Input first, then both halves - the same order as the run-time GetDLLVersion. */
	@Test
	public void GetDLLVersionLocal() {
		assertThat(nsi, containsLine("GetDLLVersionLocal \"a.dll\" $R1 $R2"));
	}

	/** Only legal inside a PageEx body, which nsL spells as a page with a block. */
	@Test
	public void DirVar() {
		assertThat(
				pageNsi,
				containsLines(
						"PageEx Directory",
						"PageCallbacks \"\"",
						"DirVar $INSTDIR",
						"DirText \"in a page\"",
						"PageExEnd"));
	}
}
