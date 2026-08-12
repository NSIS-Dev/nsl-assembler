/*
 * MiscInstructionTest.java
 */

package nsl.instruction;

import static nsl.Assembler.*;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Target and manifest declarations, DLLs, and the address family.
 *
 * <p>Most of these are global-scope declarations that pass their arguments through untouched, so
 * what is worth pinning is the quoting: a value NSIS parses as a token rather than a string - a
 * function name, a label, a version pair - must not come out quoted, and the assembler decides that
 * per instruction rather than per type.
 *
 * @author Jan T. Sott
 */
public class MiscInstructionTest {
	private static String nsi;

	@BeforeClass
	public static void assembleOnce() throws Exception {
		nsi =
				assemble(
						lines(
								"Name(\"t\");",
								"OutFile(\"t.exe\");",
								"Target(\"x86-unicode\");",
								"Unicode(true);",
								"CPU(\"x86\");",
								"SilentInstall(\"normal\");",
								"SilentUninstall(\"normal\");",
								"SetCompressionLevel(9);",
								"BGGradient(\"000000\", \"0000FF\", \"FFFFFF\");",
								"BGFont(\"Tahoma\", 40);",
								"LicenseText(\"l\");",
								"LicenseData(\"l.txt\");",
								"LicenseLangString(\"LS\", 1033, \"l.txt\");",
								"ManifestDPIAware(true);",
								"ManifestDPIAwareness(\"PerMonitorV2\");",
								"ManifestDisableWindowFiltering(true);",
								"ManifestGdiScaling(true);",
								"ManifestLongPathAware(true);",
								"ManifestMaxVersionTested(\"10.0.19041.0\");",
								"ManifestSupportedOS(\"Win10\");",
								"PEDllCharacteristics(0, 0);",
								"PESubsysVer(\"5.1\");",
								"",
								"function Helper()",
								"{",
								"  DetailPrint(\"h\");",
								"}",
								"",
								"section Test(\"t\")",
								"{",
								"  $R0 = GetKnownFolderPath(\"{F1B32785-6FBA-4FCF-9D55-7B8E7F157091}\");",
								"  RegDLL($INSTDIR.\"\\\\a.dll\");",
								"  UnRegDLL($INSTDIR.\"\\\\a.dll\");",
								"  CallInstDLL($INSTDIR.\"\\\\a.dll\", \"Func\");",
								"  Helper();",
								"  Call(\"Helper\");",
								"  $R1 = GetFunctionAddress(\"Helper\");",
								"  $R2 = GetCurrentAddress();",
								"  #nsis",
								"    corpus_label:",
								"  #nsisend",
								"  $R3 = GetLabelAddress(\"corpus_label\");",
								"  Reboot();",
								"  Quit();",
								"}"));
	}

	@Test
	public void Target() {
		assertThat(nsi, containsLine("Target \"x86-unicode\""));
	}

	@Test
	public void CPU() {
		assertThat(nsi, containsLine("CPU \"x86\""));
	}

	/** A Boolean here, unlike the neighbouring target declarations which take strings. */
	@Test
	public void Unicode() {
		assertThat(nsi, containsLine("Unicode true"));
	}

	@Test
	public void SilentInstall() {
		assertThat(nsi, containsLine("SilentInstall \"normal\""));
	}

	@Test
	public void SilentUninstall() {
		assertThat(nsi, containsLine("SilentUninstall \"normal\""));
	}

	@Test
	public void SetCompressionLevel() {
		assertThat(nsi, containsLine("SetCompressionLevel 9"));
	}

	/** Three colours, none of which gets reordered. */
	@Test
	public void BGGradient() {
		assertThat(nsi, containsLine("BGGradient \"000000\" \"0000FF\" \"FFFFFF\""));
	}

	@Test
	public void BGFont() {
		assertThat(nsi, containsLine("BGFont \"Tahoma\" 40"));
	}

	@Test
	public void LicenseText() {
		assertThat(nsi, containsLine("LicenseText \"l\""));
	}

	@Test
	public void LicenseData() {
		assertThat(nsi, containsLine("LicenseData \"l.txt\""));
	}

	/** Puts the language id between the name and the file. */
	@Test
	public void LicenseLangString() {
		assertThat(nsi, containsLine("LicenseLangString \"LS\" 1033 \"l.txt\""));
	}

	/** Across the manifest family, Booleans stay bare and strings stay quoted. */
	@Test
	public void ManifestDPIAware() {
		assertThat(nsi, containsLine("ManifestDPIAware true"));
	}

	@Test
	public void ManifestDPIAwareness() {
		assertThat(nsi, containsLine("ManifestDPIAwareness \"PerMonitorV2\""));
	}

	@Test
	public void ManifestDisableWindowFiltering() {
		assertThat(nsi, containsLine("ManifestDisableWindowFiltering true"));
	}

	@Test
	public void ManifestGdiScaling() {
		assertThat(nsi, containsLine("ManifestGdiScaling true"));
	}

	@Test
	public void ManifestLongPathAware() {
		assertThat(nsi, containsLine("ManifestLongPathAware true"));
	}

	@Test
	public void ManifestMaxVersionTested() {
		assertThat(nsi, containsLine("ManifestMaxVersionTested \"10.0.19041.0\""));
	}

	@Test
	public void ManifestSupportedOS() {
		assertThat(nsi, containsLine("ManifestSupportedOS \"Win10\""));
	}

	@Test
	public void PEDllCharacteristics() {
		assertThat(nsi, containsLine("PEDllCharacteristics 0 0"));
	}

	/** Written as a string in nsL but emitted bare, since NSIS wants a version pair. */
	@Test
	public void PESubsysVer() {
		assertThat(nsi, containsLine("PESubsysVer 5.1"));
	}

	@Test
	public void GetKnownFolderPath() {
		assertThat(
				nsi, containsLine("GetKnownFolderPath $R0 \"{F1B32785-6FBA-4FCF-9D55-7B8E7F157091}\""));
	}

	@Test
	public void RegDLL() {
		assertThat(nsi, containsLine("RegDLL \"$INSTDIR\\a.dll\""));
	}

	@Test
	public void UnRegDLL() {
		assertThat(nsi, containsLine("UnRegDLL \"$INSTDIR\\a.dll\""));
	}

	@Test
	public void CallInstDLL() {
		assertThat(nsi, containsLine("CallInstDLL \"$INSTDIR\\a.dll\" \"Func\""));
	}

	/**
	 * A function name is a token to NSIS, not a string, so it is emitted unquoted whether it came
	 * from an nsL call or from an explicit Call(). Both spellings collapse to the same line.
	 */
	@Test
	public void Call() {
		assertThat(nsi, containsLines("Call Helper", "Call Helper"));
	}

	/** The address family's arguments are likewise unquoted despite being written as strings. */
	@Test
	public void GetFunctionAddress() {
		assertThat(nsi, containsLine("GetFunctionAddress $R1 Helper"));
	}

	@Test
	public void GetCurrentAddress() {
		assertThat(nsi, containsLine("GetCurrentAddress $R2"));
	}

	@Test
	public void GetLabelAddress() {
		assertThat(nsi, containsLine("GetLabelAddress $R3 corpus_label"));
	}
}
