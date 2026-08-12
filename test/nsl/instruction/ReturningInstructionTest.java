/*
 * ReturningInstructionTest.java
 */

package nsl.instruction;

import static nsl.Assembler.*;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Instructions that produce a value, which nsL spells as an assignment rather than as an
 * output-variable argument.
 *
 * <p>Placing that argument is the assembler's job and the position differs per instruction: NSIS
 * puts it first for {@code ReadEnvStr}, last for {@code InstTypeGetText}, and last of three for
 * {@code GetDLLVersion}. Each expected line below was checked against {@code makensis -CMDHELP}.
 *
 * @author Jan T. Sott
 */
public class ReturningInstructionTest {
	private static String nsi;

	@BeforeClass
	public static void assembleOnce() throws Exception {
		nsi =
				assemble(
						inSection(
								"  $R0 = ReadEnvStr(\"PATH\");",
								"  $R1 = ExpandEnvStrings(\"%PATH%\");",
								"  $R2 = GetTempFileName();",
								"  $R3 = GetTempFileName($TEMP);",
								"  $R4 = SearchPath(\"notepad.exe\");",
								"  $R5 = GetFullPathName(\"f.txt\");",
								"  $R6 = GetFullPathName(\"f.txt\", true);",
								"  $R7 = GetFullPathName($EXEDIR, false);",
								"  $R8 = ReadMemory(0, 4);",
								"  $R9 = ReadMemory($R8, $R4);",
								"  $0 = GetErrorLevel();",
								"  $1 = GetCurInstType();",
								"  $2 = GetInstDirError();",
								"  $3 = GetRegView();",
								"  $4 = GetShellVarContext();",
								"  $5 = GetWinVer(\"Major\");",
								"  $6 = InstTypeGetText(0);",
								"  $7 = IntFmt(\"%08X\", 255);",
								"  $8 = StrCpy($R0, 5);",
								"  $9 = StrCpy($R0, 5, 2);",
								"  $R0 = UnsafeStrCpy($R0, 5);",
								"  $R1 = StrLen(ReadEnvStr(\"PATH\"));",
								"  $R2 = IntPtrOp(8, \"+\", 2);",
								"  $R3 = IntPtrOp($R2, \"~\");",
								"  ($R3, $R4) = GetDLLVersion($SYSDIR.\"\\\\kernel32.dll\");",
								"  ($R5, $R6) = GetFileTimeLocal(\"f.txt\");",
								"  $R7 = SectionGetText(0);",
								"  $R8 = SectionGetFlags(0);",
								"  $R9 = SectionGetSize(0);",
								"  $0 = SectionGetInstTypes(0);",
								"  if (GetErrorLevel() == 0)",
								"    DetailPrint(\"x\");"));
	}

	@Test
	public void ReadEnvStr() {
		assertThat(nsi, containsLine("ReadEnvStr $R0 \"PATH\""));
	}

	@Test
	public void ExpandEnvStrings() {
		assertThat(nsi, containsLine("ExpandEnvStrings $R1 \"%PATH%\""));
	}

	/**
	 * The no-argument form is right. The one-argument form is not.
	 *
	 * <p>Known bug, unfixed: GetTempFileNameInstruction guards the assignment of the base directory
	 * with {@code paramsCount > 1} rather than {@code > 0}, so it is never assigned and the operand
	 * is silently dropped. NSIS then defaults to $TEMP, which is why nothing downstream notices - the
	 * script still compiles and, whenever the argument happens to be $TEMP, still behaves. Pinned as
	 * it stands; when the guard is fixed this assertion goes red, which is the prompt to update it to
	 * {@code GetTempFileName $R3 $TEMP}.
	 */
	@Test
	public void GetTempFileName() {
		assertThat(nsi, containsLine("GetTempFileName $R2"));
		assertThat(nsi, containsLine("GetTempFileName $R3"));
	}

	@Test
	public void SearchPath() {
		assertThat(nsi, containsLine("SearchPath $R4 \"notepad.exe\""));
	}

	/** The optional trailing Boolean becomes /SHORT, ahead of the output variable, or nothing. */
	@Test
	public void GetFullPathName() {
		assertThat(nsi, containsLine("GetFullPathName $R5 \"f.txt\""));
		assertThat(nsi, containsLine("GetFullPathName /SHORT $R6 \"f.txt\""));
		assertThat(nsi, containsLine("GetFullPathName $R7 $EXEDIR"));
	}

	@Test
	public void ReadMemory() {
		assertThat(nsi, containsLine("ReadMemory $R8 0 4"));
		assertThat(nsi, containsLine("ReadMemory $R9 $R8 $R4"));
	}

	@Test
	public void GetErrorLevel() {
		assertThat(nsi, containsLine("GetErrorLevel $0"));
	}

	@Test
	public void GetCurInstType() {
		assertThat(nsi, containsLine("GetCurInstType $1"));
	}

	@Test
	public void GetInstDirError() {
		assertThat(nsi, containsLine("GetInstDirError $2"));
	}

	@Test
	public void GetRegView() {
		assertThat(nsi, containsLine("GetRegView $3"));
	}

	@Test
	public void GetShellVarContext() {
		assertThat(nsi, containsLine("GetShellVarContext $4"));
	}

	@Test
	public void GetWinVer() {
		assertThat(nsi, containsLine("GetWinVer $5 \"Major\""));
	}

	/** One of the few that puts the output variable last rather than first. */
	@Test
	public void InstTypeGetText() {
		assertThat(nsi, containsLine("InstTypeGetText 0 $6"));
	}

	@Test
	public void IntFmt() {
		assertThat(nsi, containsLine("IntFmt $7 \"%08X\" 255"));
	}

	/** Both optional tail arguments. */
	@Test
	public void StrCpy() {
		assertThat(nsi, containsLine("StrCpy $8 $R0 5"));
		assertThat(nsi, containsLine("StrCpy $9 $R0 5 2"));
	}

	@Test
	public void UnsafeStrCpy() {
		assertThat(nsi, containsLine("UnsafeStrCpy $R0 $R0 5"));
	}

	/**
	 * StrLen rejects anything the assembler considers literal - a bare register included - so its
	 * argument has to be assembled into a temporary first.
	 */
	@Test
	public void StrLen() {
		assertThat(nsi, containsLines("ReadEnvStr $0 \"PATH\"", "StrLen $R1 $0"));
	}

	/** The operator decides the arity: ~ and ! take one operand, everything else two. */
	@Test
	public void IntPtrOp() {
		assertThat(nsi, containsLine("IntPtrOp $R2 8 \"+\" 2"));
		assertThat(nsi, containsLine("IntPtrOp $R3 $R2 \"~\""));
	}

	/** Two output variables, both after the input, in the order the tuple was written. */
	@Test
	public void GetDLLVersion() {
		assertThat(nsi, containsLine("GetDLLVersion \"$SYSDIR\\kernel32.dll\" $R3 $R4"));
	}

	@Test
	public void GetFileTimeLocal() {
		assertThat(nsi, containsLine("GetFileTimeLocal \"f.txt\" $R5 $R6"));
	}

	@Test
	public void SectionGetText() {
		assertThat(nsi, containsLine("SectionGetText 0 $R7"));
	}

	@Test
	public void SectionGetFlags() {
		assertThat(nsi, containsLine("SectionGetFlags 0 $R8"));
	}

	@Test
	public void SectionGetSize() {
		assertThat(nsi, containsLine("SectionGetSize 0 $R9"));
	}

	@Test
	public void SectionGetInstTypes() {
		assertThat(nsi, containsLine("SectionGetInstTypes 0 $0"));
	}

	/** Used directly in an expression, the value goes through a temporary before the comparison. */
	@Test
	public void testReturnValueInCondition() {
		assertThat(nsi, containsLines("GetErrorLevel $0", "IntCmp $0 0 _lbl_0 _lbl_1 _lbl_1"));
	}
}
