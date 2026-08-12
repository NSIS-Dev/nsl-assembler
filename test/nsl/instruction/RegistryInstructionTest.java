/*
 * RegistryInstructionTest.java
 */

package nsl.instruction;

import static nsl.Assembler.*;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The registry and INI families.
 *
 * <p>Both are shaped the same way - root, key, entry, value - and the whole family is one
 * transposition away from writing to the wrong place, which makensis will happily compile. The
 * reading half puts its output variable first, the writing half has none.
 *
 * @author Jan T. Sott
 */
public class RegistryInstructionTest {
	private static String nsi;

	@BeforeClass
	public static void assembleOnce() throws Exception {
		nsi =
				assemble(
						lines(
										"Name(\"t\");",
										"OutFile(\"t.exe\");",
										"InstallDirRegKey(\"HKLM\", \"Software\\\\c\", \"Path\");",
										"",
										"section Test(\"t\")",
										"{")
								+ lines(
										"  WriteRegStr(\"HKLM\", \"Software\\\\c\", \"S\", \"v\");",
										"  WriteRegExpandStr(\"HKLM\", \"Software\\\\c\", \"E\", \"%TEMP%\");",
										"  WriteRegMultiStr(\"HKLM\", \"Software\\\\c\", \"M\", \"a\");",
										"  WriteRegDWORD(\"HKLM\", \"Software\\\\c\", \"D\", 1);",
										"  WriteRegBin(\"HKLM\", \"Software\\\\c\", \"B\", \"0102\");",
										"  WriteRegNone(\"HKLM\", \"Software\\\\c\", \"N\");",
										"  $R0 = ReadRegStr(\"HKLM\", \"Software\\\\c\", \"S\");",
										"  $R1 = ReadRegDWORD(\"HKLM\", \"Software\\\\c\", \"D\");",
										"  $R2 = EnumRegKey(\"HKLM\", \"Software\", 0);",
										"  $R3 = EnumRegValue(\"HKLM\", \"Software\\\\c\", 0);",
										"  DeleteRegValue(\"HKLM\", \"Software\\\\c\", \"S\");",
										"  DeleteRegKey(\"HKLM\", \"Software\\\\c\");",
										"  DeleteRegKey(\"HKLM\", \"Software\\\\c\", true);",
										"  WriteINIStr(\"i.ini\", \"S\", \"K\", \"v\");",
										"  $R4 = ReadINIStr(\"i.ini\", \"S\", \"K\");",
										"  DeleteINIStr(\"i.ini\", \"S\", \"K\");",
										"  DeleteINISec(\"i.ini\", \"S\");",
										"  FlushINI(\"i.ini\");",
										"}"));
	}

	@Test
	public void WriteRegStr() {
		assertThat(nsi, containsLine("WriteRegStr \"HKLM\" \"Software\\c\" \"S\" \"v\""));
	}

	@Test
	public void WriteRegExpandStr() {
		assertThat(nsi, containsLine("WriteRegExpandStr \"HKLM\" \"Software\\c\" \"E\" \"%TEMP%\""));
	}

	/** The only one of the family that carries a mandatory flag of its own. */
	@Test
	public void WriteRegMultiStr() {
		assertThat(
				nsi, containsLine("WriteRegMultiStr /REGEDIT5 \"HKLM\" \"Software\\c\" \"M\" \"a\""));
	}

	@Test
	public void WriteRegDWORD() {
		assertThat(nsi, containsLine("WriteRegDWORD \"HKLM\" \"Software\\c\" \"D\" 1"));
	}

	@Test
	public void WriteRegBin() {
		assertThat(nsi, containsLine("WriteRegBin \"HKLM\" \"Software\\c\" \"B\" \"0102\""));
	}

	/** Takes no value at all, which is what distinguishes it from the rest. */
	@Test
	public void WriteRegNone() {
		assertThat(nsi, containsLine("WriteRegNone \"HKLM\" \"Software\\c\" \"N\""));
	}

	/** The reading half: output variable ahead of the root key, not after it. */
	@Test
	public void ReadRegStr() {
		assertThat(nsi, containsLine("ReadRegStr $R0 \"HKLM\" \"Software\\c\" \"S\""));
	}

	@Test
	public void ReadRegDWORD() {
		assertThat(nsi, containsLine("ReadRegDWORD $R1 \"HKLM\" \"Software\\c\" \"D\""));
	}

	/** Enumeration takes an index last and, like the readers, the output first. */
	@Test
	public void EnumRegKey() {
		assertThat(nsi, containsLine("EnumRegKey $R2 \"HKLM\" \"Software\" 0"));
	}

	@Test
	public void EnumRegValue() {
		assertThat(nsi, containsLine("EnumRegValue $R3 \"HKLM\" \"Software\\c\" 0"));
	}

	@Test
	public void DeleteRegValue() {
		assertThat(nsi, containsLine("DeleteRegValue \"HKLM\" \"Software\\c\" \"S\""));
	}

	/** The Boolean becomes /ifempty, ahead of the root key. */
	@Test
	public void DeleteRegKey() {
		assertThat(nsi, containsLine("DeleteRegKey \"HKLM\" \"Software\\c\""));
		assertThat(nsi, containsLine("DeleteRegKey /ifempty \"HKLM\" \"Software\\c\""));
	}

	/** A global-scope attribute rather than an instruction, so it lands above the section. */
	@Test
	public void InstallDirRegKey() {
		assertThat(
				nsi,
				containsLines("InstallDirRegKey \"HKLM\" \"Software\\c\" \"Path\"", "Section \"t\" Test"));
	}

	/** The INI half, shaped the same way but with a filename where the root key goes. */
	@Test
	public void WriteINIStr() {
		assertThat(nsi, containsLine("WriteINIStr \"i.ini\" \"S\" \"K\" \"v\""));
	}

	@Test
	public void ReadINIStr() {
		assertThat(nsi, containsLine("ReadINIStr $R4 \"i.ini\" \"S\" \"K\""));
	}

	@Test
	public void DeleteINIStr() {
		assertThat(nsi, containsLine("DeleteINIStr \"i.ini\" \"S\" \"K\""));
	}

	@Test
	public void DeleteINISec() {
		assertThat(nsi, containsLine("DeleteINISec \"i.ini\" \"S\""));
	}

	@Test
	public void FlushINI() {
		assertThat(nsi, containsLine("FlushINI \"i.ini\""));
	}
}
