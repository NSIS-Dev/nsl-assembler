/*
 * UiInstructionTest.java
 */

package nsl.instruction;

import static nsl.Assembler.*;
import static org.junit.Assert.*;

import nsl.Assembler;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Windows, controls, section state and running other processes.
 *
 * <p>Several here have an optional output variable rather than a mandatory one, so the same wrapper
 * emits a different arity depending on whether the caller assigned the result - and {@code
 * SendMessage} puts a flag <em>behind</em> that output rather than in front of the operands, which
 * is the opposite of the convention everywhere else.
 *
 * @author Jan T. Sott
 */
public class UiInstructionTest {
	private static String nsi;

	@BeforeClass
	public static void assembleOnce() throws Exception {
		nsi =
				assemble(
						inSection(
								"  $R0 = FindWindow(\"#32770\", \"\");",
								"  $R1 = GetDlgItem($HWNDPARENT, 1);",
								"  $R2 = CreateFont(\"Tahoma\", 8);",
								"  EnableWindow($R1, 1);",
								"  EnableWindow($R1, 0);",
								"  ShowWindow($R1, 5);",
								"  LockWindow(\"on\");",
								"  LockWindow(\"off\");",
								"  SendMessage($R1, 12, 0, \"x\");",
								"  $R3 = SendMessage($R1, 13, 0, \"x\");",
								"  $R4 = SendMessage($R1, 13, 0, \"x\", 100);",
								"  InstTypeSetText(0, \"Full\");",
								"  SectionSetText(0, \"x\");",
								"  SectionSetFlags(0, 1);",
								"  SectionSetSize(0, 100);",
								"  SectionSetInstTypes(0, 1);",
								"  Exec($INSTDIR.\"\\\\a.exe\");",
								"  $R5 = ExecWait($INSTDIR.\"\\\\a.exe\");",
								"  ExecWait($INSTDIR.\"\\\\a.exe\");",
								"  MessageBox(\"MB_OK\", \"m\");",
								"  $R6 = MessageBox(\"MB_YESNO\", \"m\");"));
	}

	@Test
	public void FindWindow() {
		assertThat(nsi, containsLine("FindWindow $R0 \"#32770\" \"\""));
	}

	@Test
	public void GetDlgItem() {
		assertThat(nsi, containsLine("GetDlgItem $R1 $HWNDPARENT 1"));
	}

	@Test
	public void CreateFont() {
		assertThat(nsi, containsLine("CreateFont $R2 \"Tahoma\" 8"));
	}

	/** The state is an integer, not a Boolean, so it is not subject to the switch convention. */
	@Test
	public void EnableWindow() {
		assertThat(nsi, containsLine("EnableWindow $R1 1"));
		assertThat(nsi, containsLine("EnableWindow $R1 0"));
	}

	@Test
	public void ShowWindow() {
		assertThat(nsi, containsLine("ShowWindow $R1 5"));
	}

	/** Takes the strings on/off rather than a Boolean, unlike most of the toggles in the language. */
	@Test
	public void LockWindow() {
		assertThat(nsi, containsLine("LockWindow \"on\""));
		assertThat(nsi, containsLine("LockWindow \"off\""));
	}

	/**
	 * Three arities off one wrapper. The output variable appears only when the result is used, and
	 * the timeout becomes /TIMEOUT= <em>after</em> it rather than before the operands.
	 */
	@Test
	public void SendMessage() {
		assertThat(nsi, containsLine("SendMessage $R1 12 0 \"x\""));
		assertThat(nsi, containsLine("SendMessage $R1 13 0 \"x\" $R3"));
		assertThat(nsi, containsLine("SendMessage $R1 13 0 \"x\" $R4 /TIMEOUT=100"));
	}

	@Test
	public void InstTypeSetText() {
		assertThat(nsi, containsLine("InstTypeSetText 0 \"Full\""));
	}

	/** The write half of the section-state family: index first, value second, no output. */
	@Test
	public void SectionSetText() {
		assertThat(nsi, containsLine("SectionSetText 0 \"x\""));
	}

	@Test
	public void SectionSetFlags() {
		assertThat(nsi, containsLine("SectionSetFlags 0 1"));
	}

	@Test
	public void SectionSetSize() {
		assertThat(nsi, containsLine("SectionSetSize 0 100"));
	}

	@Test
	public void SectionSetInstTypes() {
		assertThat(nsi, containsLine("SectionSetInstTypes 0 1"));
	}

	@Test
	public void Exec() {
		assertThat(nsi, containsLine("Exec \"$INSTDIR\\a.exe\""));
	}

	/** The return variable is optional, so the unused form must not emit a stray operand. */
	@Test
	public void ExecWait() {
		assertThat(nsi, containsLine("ExecWait \"$INSTDIR\\a.exe\" $R5"));
		assertThat(nsi, containsLine("ExecWait \"$INSTDIR\\a.exe\""));
	}

	/**
	 * As a statement the flags are all there is. Stored in a variable, the button name has to be
	 * materialised through the same branch-and-StrCpy dance the Boolean instructions use.
	 */
	@Test
	public void MessageBox() {
		assertThat(nsi, containsLine("MessageBox MB_OK \"m\""));
		assertThat(
				nsi,
				containsLines(
						"MessageBox MB_YESNO \"m\" IDNO +3", "StrCpy $R6 IDYES", "Goto +2", "StrCpy $R6 IDNO"));
	}

	/**
	 * SetCtlColors and SetBrandingImage return nothing, so a statement is the only way to call them -
	 * but both implement only {@code assemble(Register)} and throw UnsupportedOperationException from
	 * the statement form. They are therefore unreachable from nsL entirely.
	 *
	 * <p>Known bug, unfixed; see e2e/KNOWN-GAPS.md. Pinned as uncallable so that implementing {@code
	 * assemble()} on either turns this red.
	 */
	@Test
	public void testUncallableInstructions() throws Exception {
		Assembler.assembleExpectingError(
				inSection("  SetCtlColors($HWNDPARENT, \"FFFFFF\", \"000000\");"));
		Assembler.assembleExpectingError(inSection("  SetBrandingImage(\"b.bmp\");"));
	}
}
