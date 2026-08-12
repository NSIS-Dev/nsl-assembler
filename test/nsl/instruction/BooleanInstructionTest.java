/*
 * BooleanInstructionTest.java
 */

package nsl.instruction;

import static nsl.Assembler.*;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Instructions whose value is a Boolean.
 *
 * <p>In NSIS these are branch instructions taking a pair of labels; nsL turns them into
 * expressions. So the same wrapper has to assemble one way inside a condition, another way when its
 * result is stored, and a third when it is threaded into a compound condition. Which label of the
 * pair comes first is the whole game: swapping them inverts the condition and makensis cannot tell.
 *
 * <p>The label numbers below are therefore asserted literally, and are positional - inserting a
 * statement into the script renumbers everything after it.
 *
 * @author Jan T. Sott
 */
public class BooleanInstructionTest {
	private static String nsi;

	@BeforeClass
	public static void assembleOnce() throws Exception {
		nsi =
				assemble(
						inSection(
								"  if (Errors())",
								"    DetailPrint(\"a\");",
								"  if (FileExists($EXEDIR))",
								"    DetailPrint(\"b\");",
								"  if (Silent())",
								"    DetailPrint(\"c\");",
								"  if (RebootFlag())",
								"    DetailPrint(\"d\");",
								"  if (IsWindow($HWNDPARENT))",
								"    DetailPrint(\"e\");",
								"  if (AbortCalled())",
								"    DetailPrint(\"f\");",
								"  if (AltRegView())",
								"    DetailPrint(\"g\");",
								"  if (RtlLanguage())",
								"    DetailPrint(\"h\");",
								"  if (ShellVarContextAll())",
								"    DetailPrint(\"i\");",
								"  if (!Errors())",
								"    DetailPrint(\"j\");",
								"  $R0 = Errors();",
								"  $R1 = FileExists($EXEDIR);",
								"  if (FileExists($EXEDIR) && !Errors())",
								"    DetailPrint(\"k\");",
								"  if (Silent() || FileExists($EXEDIR))",
								"    DetailPrint(\"l\");",
								"  if (MessageBox(\"MB_YESNO\", \"q\") == \"IDYES\")",
								"    DetailPrint(\"m\");"));
	}

	/** True label first, body under it. */
	@Test
	public void Errors() {
		assertThat(
				nsi, containsLines("IfErrors _lbl_0 _lbl_1", "_lbl_0:", "DetailPrint \"a\"", "_lbl_1:"));
	}

	@Test
	public void FileExists() {
		assertThat(
				nsi, containsLines("IfFileExists $EXEDIR _lbl_2 _lbl_3", "_lbl_2:", "DetailPrint \"b\""));
	}

	@Test
	public void Silent() {
		assertThat(nsi, containsLines("IfSilent _lbl_4 _lbl_5", "_lbl_4:", "DetailPrint \"c\""));
	}

	@Test
	public void RebootFlag() {
		assertThat(nsi, containsLines("IfRebootFlag _lbl_6 _lbl_7", "_lbl_6:", "DetailPrint \"d\""));
	}

	/** The one in this family that takes an operand as well as the label pair. */
	@Test
	public void IsWindow() {
		assertThat(
				nsi, containsLines("IsWindow $HWNDPARENT _lbl_8 _lbl_9", "_lbl_8:", "DetailPrint \"e\""));
	}

	/** Spelled AbortCalled in nsL and IfAbort in NSIS. */
	@Test
	public void AbortCalled() {
		assertThat(nsi, containsLines("IfAbort _lbl_10 _lbl_11", "_lbl_10:", "DetailPrint \"f\""));
	}

	@Test
	public void AltRegView() {
		assertThat(nsi, containsLines("IfAltRegView _lbl_12 _lbl_13", "_lbl_12:", "DetailPrint \"g\""));
	}

	@Test
	public void RtlLanguage() {
		assertThat(
				nsi, containsLines("IfRtlLanguage _lbl_14 _lbl_15", "_lbl_14:", "DetailPrint \"h\""));
	}

	@Test
	public void ShellVarContextAll() {
		assertThat(
				nsi,
				containsLines("IfShellVarContextAll _lbl_16 _lbl_17", "_lbl_16:", "DetailPrint \"i\""));
	}

	/** Negation swaps the label pair rather than emitting a jump around the body. */
	@Test
	public void testNegated() {
		assertThat(
				nsi,
				containsLines("IfErrors _lbl_19 _lbl_18", "_lbl_18:", "DetailPrint \"j\"", "_lbl_19:"));
	}

	/**
	 * Stored in a variable rather than branched on, the Boolean has to be materialised: branch over a
	 * pair of StrCpy, using relative offsets rather than labels.
	 */
	@Test
	public void testStoredInVariable() {
		assertThat(
				nsi, containsLines("IfErrors 0 +3", "StrCpy $R0 true", "Goto +2", "StrCpy $R0 false"));
		assertThat(
				nsi,
				containsLines(
						"IfFileExists $EXEDIR 0 +3", "StrCpy $R1 true", "Goto +2", "StrCpy $R1 false"));
	}

	/**
	 * && falls through on true, so the first test's true label is 0 and its false label is the end.
	 */
	@Test
	public void testAndChain() {
		assertThat(
				nsi,
				containsLines(
						"IfFileExists $EXEDIR 0 _lbl_21",
						"IfErrors _lbl_21 _lbl_20",
						"_lbl_20:",
						"DetailPrint \"k\"",
						"_lbl_21:"));
	}

	/** || is the mirror image: jump to the body on true, fall through to the next test otherwise. */
	@Test
	public void testOrChain() {
		assertThat(
				nsi,
				containsLines(
						"IfSilent _lbl_22 0",
						"IfFileExists $EXEDIR _lbl_22 _lbl_23",
						"_lbl_22:",
						"DetailPrint \"l\"",
						"_lbl_23:"));
	}

	/**
	 * MessageBox is the odd one out: its value is a button name, so comparing it against a literal
	 * turns into NSIS's own IDxxx label arguments rather than a StrCmp.
	 */
	@Test
	public void MessageBox() {
		assertThat(
				nsi,
				containsLines(
						"MessageBox MB_YESNO \"q\" IDYES _lbl_24 IDNO _lbl_25",
						"_lbl_24:",
						"DetailPrint \"m\""));
	}
}
