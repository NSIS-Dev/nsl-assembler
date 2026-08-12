/*
 * SwitchInstructionTest.java
 */

package nsl.instruction;

import static nsl.Assembler.*;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The switch convention: where NSIS takes a /FLAG, nsL takes a Boolean.
 *
 * <p>Two things have to hold for each. The flag has to land in the position NSIS expects - before
 * the operands here, though not for every instruction in the language - and passing false has to
 * emit nothing at all rather than a literal "false". Both forms are therefore asserted for every
 * instruction below. Flag names and their order were checked against {@code makensis -CMDHELP}.
 *
 * @author Jan T. Sott
 */
public class SwitchInstructionTest {
	private static String nsi;

	@BeforeClass
	public static void assembleOnce() throws Exception {
		nsi =
				assemble(
						lines("Name(\"t\");", "OutFile(\"t.exe\");", "", "section Test(\"t\")", "{")
								+ lines(
										"  ReserveFile(\"a.txt\");",
										"  ReserveFileRecursive(\"sub\");",
										"  SetOutPath($INSTDIR);",
										"  Delete($INSTDIR.\"\\\\a.txt\");",
										"  Delete($INSTDIR.\"\\\\b.txt\", true);",
										"  RMDir($INSTDIR.\"\\\\e\");",
										"  RMDir($INSTDIR.\"\\\\e\", true);",
										"  RMDirRecursive($INSTDIR.\"\\\\t\");",
										"  RMDirRecursive($INSTDIR.\"\\\\t\", true);",
										"  Rename($INSTDIR.\"\\\\f\", $INSTDIR.\"\\\\g\");",
										"  Rename($INSTDIR.\"\\\\f\", $INSTDIR.\"\\\\g\", true);",
										"  File(\"a.txt\");",
										"  File(\"a.txt\", \"r.txt\");",
										"  FileRecursive(\"sub\");",
										"  CopyFiles($INSTDIR.\"\\\\s\", $INSTDIR.\"\\\\d\");",
										"  CopyFiles($INSTDIR.\"\\\\s\", $INSTDIR.\"\\\\d\", true);",
										"  CopyFiles($INSTDIR.\"\\\\s\", $INSTDIR.\"\\\\d\", true, true);",
										"  CreateDirectory($INSTDIR.\"\\\\m\");",
										"  ExecShell(\"open\", $INSTDIR);",
										"  ExecShell(\"open\", $INSTDIR, \"\");",
										"  ExecShellWait(\"open\", $INSTDIR);",
										"  CreateShortCut($INSTDIR.\"\\\\s.lnk\", $INSTDIR.\"\\\\t.exe\");",
										"  CreateShortCut($INSTDIR.\"\\\\s.lnk\", $INSTDIR.\"\\\\t.exe\", \"-a\");",
										"  CreateShortCut($INSTDIR.\"\\\\s.lnk\", $INSTDIR.\"\\\\t.exe\", \"-a\","
												+ " $INSTDIR.\"\\\\t.exe\", 0, \"SW_SHOWNORMAL\", \"\", \"d\");",
										"  WriteUninstaller($INSTDIR.\"\\\\U.exe\");",
										"}",
										"",
										"uninstall section Uninstall(\"u\")",
										"{",
										"  Delete($INSTDIR.\"\\\\a.txt\", true);",
										"}"));
	}

	@Test
	public void ReserveFile() {
		assertThat(nsi, containsLine("ReserveFile \"a.txt\""));
	}

	/** The recursive variant is a separate nsL wrapper rather than a Boolean argument. */
	@Test
	public void ReserveFileRecursive() {
		assertThat(nsi, containsLine("ReserveFile /r \"sub\""));
	}

	@Test
	public void SetOutPath() {
		assertThat(nsi, containsLine("SetOutPath $INSTDIR"));
	}

	@Test
	public void Delete() {
		assertThat(nsi, containsLine("Delete \"$INSTDIR\\a.txt\""));
		assertThat(nsi, containsLine("Delete /REBOOTOK \"$INSTDIR\\b.txt\""));
	}

	@Test
	public void RMDir() {
		assertThat(nsi, containsLine("RMDir \"$INSTDIR\\e\""));
		assertThat(nsi, containsLine("RMDir /REBOOTOK \"$INSTDIR\\e\""));
	}

	/** Two flags from two different sources: /r from the wrapper name, /REBOOTOK from the Boolean. */
	@Test
	public void RMDirRecursive() {
		assertThat(nsi, containsLine("RMDir /r \"$INSTDIR\\t\""));
		assertThat(nsi, containsLine("RMDir /r /REBOOTOK \"$INSTDIR\\t\""));
	}

	@Test
	public void Rename() {
		assertThat(nsi, containsLine("Rename \"$INSTDIR\\f\" \"$INSTDIR\\g\""));
		assertThat(nsi, containsLine("Rename /REBOOTOK \"$INSTDIR\\f\" \"$INSTDIR\\g\""));
	}

	/** /oname carries a value rather than being a bare flag, and is quoted whole. */
	@Test
	public void File() {
		assertThat(nsi, containsLine("File \"a.txt\""));
		assertThat(nsi, containsLine("File \"/oname=r.txt\" \"a.txt\""));
	}

	@Test
	public void FileRecursive() {
		assertThat(nsi, containsLine("File /r \"sub\""));
	}

	/** Two independent flags, so the second cannot be set without the first. */
	@Test
	public void CopyFiles() {
		assertThat(nsi, containsLine("CopyFiles \"$INSTDIR\\s\" \"$INSTDIR\\d\""));
		assertThat(nsi, containsLine("CopyFiles /SILENT \"$INSTDIR\\s\" \"$INSTDIR\\d\""));
		assertThat(nsi, containsLine("CopyFiles /SILENT /FILESONLY \"$INSTDIR\\s\" \"$INSTDIR\\d\""));
	}

	@Test
	public void CreateDirectory() {
		assertThat(nsi, containsLine("CreateDirectory \"$INSTDIR\\m\""));
	}

	@Test
	public void ExecShell() {
		assertThat(nsi, containsLine("ExecShell \"open\" $INSTDIR"));
		assertThat(nsi, containsLine("ExecShell \"open\" $INSTDIR \"\""));
	}

	/** The waiting form is a separate wrapper, not a flag. */
	@Test
	public void ExecShellWait() {
		assertThat(nsi, containsLine("ExecShellWait \"open\" $INSTDIR"));
	}

	/** A long optional tail rather than a flag: every argument after the second may be omitted. */
	@Test
	public void CreateShortCut() {
		assertThat(nsi, containsLine("CreateShortCut \"$INSTDIR\\s.lnk\" \"$INSTDIR\\t.exe\""));
		assertThat(nsi, containsLine("CreateShortCut \"$INSTDIR\\s.lnk\" \"$INSTDIR\\t.exe\" \"-a\""));
		assertThat(
				nsi,
				containsLine(
						"CreateShortCut \"$INSTDIR\\s.lnk\" \"$INSTDIR\\t.exe\" \"-a\" \"$INSTDIR\\t.exe\" 0"
								+ " \"SW_SHOWNORMAL\" \"\" \"d\""));
	}

	@Test
	public void WriteUninstaller() {
		assertThat(nsi, containsLine("WriteUninstaller \"$INSTDIR\\U.exe\""));
	}

	/**
	 * The convention is unchanged on the uninstaller side, where the section gains its un. prefix.
	 */
	@Test
	public void testUninstallerSide() {
		assertThat(
				nsi, containsLines("Section \"un.u\" Uninstall", "Delete /REBOOTOK \"$INSTDIR\\a.txt\""));
	}
}
