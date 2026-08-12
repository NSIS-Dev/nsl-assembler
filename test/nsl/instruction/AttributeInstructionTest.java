/*
 * AttributeInstructionTest.java
 */

package nsl.instruction;

import static nsl.Assembler.*;
import static org.junit.Assert.*;

import nsl.Assembler;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Global-scope installer attributes.
 *
 * <p>These take no registers and emit no control flow, so the only things to get wrong are argument
 * order, quoting, and whether the assembler accepts the value in the first place. That last one is
 * not uniform: some of these toggles are spelled as an nsL Boolean and some as the strings NSIS
 * itself uses, with no rule connecting the two - {@code AutoCloseWindow} takes a Boolean while
 * {@code XPStyle} next to it takes "on". Both spellings are pinned below.
 *
 * @author Jan T. Sott
 */
public class AttributeInstructionTest {
	private static String nsi;

	@BeforeClass
	public static void assembleOnce() throws Exception {
		nsi =
				assemble(
						lines(
								"Name(\"t\");",
								"OutFile(\"t.exe\");",
								"Caption(\"c\");",
								"SubCaption(0, \"s\");",
								"BrandingText(\"b\");",
								"Icon(\"i.ico\");",
								"UninstallIcon(\"u.ico\");",
								"WindowIcon(\"on\");",
								"CheckBitmap(\"c.bmp\");",
								"InstallDir($PROGRAMFILES.\"\\\\t\");",
								"InstallDir(\"$PROGRAMFILES\\\\t\");",
								"InstallColors(\"FFFFFF\", \"000000\");",
								"InstallButtonText(\"Go\");",
								"UninstallButtonText(\"Bye\");",
								"UninstallCaption(\"uc\");",
								"UninstallSubCaption(0, \"us\");",
								"UninstallText(\"ut\");",
								"DetailsButtonText(\"d\");",
								"CompletedText(\"done\");",
								"ComponentText(\"ct\");",
								"DirText(\"dt\");",
								"LicenseBkColor(\"FFFFFF\");",
								"LicenseForceSelection(\"checkbox\");",
								"MiscButtonText(\"b\", \"n\", \"c\", \"cl\");",
								"SpaceTexts(\"req\", \"avail\");",
								"FileErrorText(\"fe\");",
								"DirVerify(\"auto\");",
								"AllowRootDirInstall(true);",
								"AllowSkipFiles(\"off\");",
								"AutoCloseWindow(false);",
								"CRCCheck(\"on\");",
								"InstProgressFlags(\"smooth\");",
								"ShowInstDetails(\"show\");",
								"ShowUninstDetails(\"show\");",
								"XPStyle(\"on\");",
								"SetFont(\"Tahoma\", 8);",
								"SetCompressor(\"lzma\");",
								"SetCompressorDictSize(8);",
								"LangString(\"S\", 1033, \"v\");",
								"VIProductVersion(\"1.0.0.0\");",
								"VIFileVersion(\"1.0.0.0\");",
								"VIAddVersionKey(\"ProductName\", \"t\");",
								"ChangeUI(\"all\", \"u.exe\");",
								"LoadLanguageFile(\"English.nlf\");",
								"AddBrandingImage(\"top\", \"32\");",
								"ManifestAppendCustomString(\"ns\", \"x\");",
								"PEAddResource(\"a.dat\", 10, 1);",
								"PERemoveResource(10, 1, 1033);",
								"RequestExecutionLevel(\"user\");",
								"InstType(\"Full\");",
								"",
								"section Test(\"t\")",
								"{",
								"  AddSize(100);",
								"  SectionIn(1);",
								"  DetailPrint(\"x\");",
								"}"));
	}

	@Test
	public void Caption() {
		assertThat(nsi, containsLine("Caption \"c\""));
	}

	/** The two-argument captions take their index first. */
	@Test
	public void SubCaption() {
		assertThat(nsi, containsLine("SubCaption 0 \"s\""));
	}

	@Test
	public void UninstallCaption() {
		assertThat(nsi, containsLine("UninstallCaption \"uc\""));
	}

	@Test
	public void UninstallSubCaption() {
		assertThat(nsi, containsLine("UninstallSubCaption 0 \"us\""));
	}

	@Test
	public void BrandingText() {
		assertThat(nsi, containsLine("BrandingText \"b\""));
	}

	@Test
	public void Icon() {
		assertThat(nsi, containsLine("Icon \"i.ico\""));
	}

	@Test
	public void UninstallIcon() {
		assertThat(nsi, containsLine("UninstallIcon \"u.ico\""));
	}

	@Test
	public void WindowIcon() {
		assertThat(nsi, containsLine("WindowIcon \"on\""));
	}

	@Test
	public void CheckBitmap() {
		assertThat(nsi, containsLine("CheckBitmap \"c.bmp\""));
	}

	/**
	 * A constant concatenated onto a string stays a constant. Written inside the string it is escaped
	 * to $$ instead, which NSIS reads as a literal dollar - the two spellings mean different things
	 * and only one of them is usually intended.
	 */
	@Test
	public void InstallDir() {
		assertThat(nsi, containsLine("InstallDir \"$PROGRAMFILES\\t\""));
		assertThat(nsi, containsLine("InstallDir \"$$PROGRAMFILES\\t\""));
	}

	@Test
	public void InstallColors() {
		assertThat(nsi, containsLine("InstallColors \"FFFFFF\" \"000000\""));
	}

	@Test
	public void InstallButtonText() {
		assertThat(nsi, containsLine("InstallButtonText \"Go\""));
	}

	@Test
	public void UninstallButtonText() {
		assertThat(nsi, containsLine("UninstallButtonText \"Bye\""));
	}

	@Test
	public void UninstallText() {
		assertThat(nsi, containsLine("UninstallText \"ut\""));
	}

	@Test
	public void DetailsButtonText() {
		assertThat(nsi, containsLine("DetailsButtonText \"d\""));
	}

	@Test
	public void CompletedText() {
		assertThat(nsi, containsLine("CompletedText \"done\""));
	}

	@Test
	public void ComponentText() {
		assertThat(nsi, containsLine("ComponentText \"ct\""));
	}

	@Test
	public void DirText() {
		assertThat(nsi, containsLine("DirText \"dt\""));
	}

	@Test
	public void FileErrorText() {
		assertThat(nsi, containsLine("FileErrorText \"fe\""));
	}

	/** Four button labels in one line, none of which may be reordered. */
	@Test
	public void MiscButtonText() {
		assertThat(nsi, containsLine("MiscButtonText \"b\" \"n\" \"c\" \"cl\""));
	}

	@Test
	public void SpaceTexts() {
		assertThat(nsi, containsLine("SpaceTexts \"req\" \"avail\""));
	}

	@Test
	public void LicenseBkColor() {
		assertThat(nsi, containsLine("LicenseBkColor \"FFFFFF\""));
	}

	@Test
	public void LicenseForceSelection() {
		assertThat(nsi, containsLine("LicenseForceSelection \"checkbox\""));
	}

	/** Spelled as an nsL Boolean, so it stays bare. */
	@Test
	public void AllowRootDirInstall() {
		assertThat(nsi, containsLine("AllowRootDirInstall true"));
	}

	@Test
	public void AutoCloseWindow() {
		assertThat(nsi, containsLine("AutoCloseWindow false"));
	}

	/** Spelled with the strings NSIS itself uses, so the quotes stay. */
	@Test
	public void AllowSkipFiles() {
		assertThat(nsi, containsLine("AllowSkipFiles \"off\""));
	}

	@Test
	public void CRCCheck() {
		assertThat(nsi, containsLine("CRCCheck \"on\""));
	}

	@Test
	public void XPStyle() {
		assertThat(nsi, containsLine("XPStyle \"on\""));
	}

	@Test
	public void DirVerify() {
		assertThat(nsi, containsLine("DirVerify \"auto\""));
	}

	@Test
	public void InstProgressFlags() {
		assertThat(nsi, containsLine("InstProgressFlags \"smooth\""));
	}

	@Test
	public void ShowInstDetails() {
		assertThat(nsi, containsLine("ShowInstDetails \"show\""));
	}

	@Test
	public void ShowUninstDetails() {
		assertThat(nsi, containsLine("ShowUninstDetails \"show\""));
	}

	@Test
	public void SetCompressor() {
		assertThat(nsi, containsLine("SetCompressor \"lzma\""));
	}

	@Test
	public void SetCompressorDictSize() {
		assertThat(nsi, containsLine("SetCompressorDictSize 8"));
	}

	@Test
	public void SetFont() {
		assertThat(nsi, containsLine("SetFont \"Tahoma\" 8"));
	}

	@Test
	public void LangString() {
		assertThat(nsi, containsLine("LangString \"S\" 1033 \"v\""));
	}

	@Test
	public void VIProductVersion() {
		assertThat(nsi, containsLine("VIProductVersion \"1.0.0.0\""));
	}

	@Test
	public void VIFileVersion() {
		assertThat(nsi, containsLine("VIFileVersion \"1.0.0.0\""));
	}

	@Test
	public void VIAddVersionKey() {
		assertThat(nsi, containsLine("VIAddVersionKey \"ProductName\" \"t\""));
	}

	@Test
	public void ChangeUI() {
		assertThat(nsi, containsLine("ChangeUI \"all\" \"u.exe\""));
	}

	@Test
	public void LoadLanguageFile() {
		assertThat(nsi, containsLine("LoadLanguageFile \"English.nlf\""));
	}

	/**
	 * The size is a pixel count, so an integer is the natural spelling. A string stays valid too,
	 * because NSIS's dialog-unit suffix - "32u" - has nowhere else to live.
	 */
	@Test
	public void AddBrandingImage() throws Exception {
		assertThat(nsi, containsLine("AddBrandingImage \"top\" \"32\""));
		assertThat(
				Assembler.assemble(
						lines(
								"Name(\"t\");",
								"OutFile(\"t.exe\");",
								"AddBrandingImage(\"top\", 32, 4);",
								"",
								"section Test(\"t\")",
								"{",
								"  DetailPrint(\"x\");",
								"}")),
				containsLine("AddBrandingImage \"top\" 32 4"));
	}

	@Test
	public void ManifestAppendCustomString() {
		assertThat(nsi, containsLine("ManifestAppendCustomString \"ns\" \"x\""));
	}

	@Test
	public void PEAddResource() {
		assertThat(nsi, containsLine("PEAddResource \"a.dat\" 10 1"));
	}

	@Test
	public void PERemoveResource() {
		assertThat(nsi, containsLine("PERemoveResource 10 1 1033"));
	}

	@Test
	public void RequestExecutionLevel() {
		assertThat(nsi, containsLine("RequestExecutionLevel \"user\""));
	}

	/** Global, unlike the two below it. */
	@Test
	public void InstType() {
		assertThat(nsi, containsLine("InstType \"Full\""));
	}

	/** Belongs to the section it lands in, so it has to be emitted inside one. */
	@Test
	public void AddSize() {
		assertThat(nsi, containsLines("Section \"t\" Test", "AddSize 100"));
	}

	@Test
	public void SectionIn() {
		assertThat(nsi, containsLines("AddSize 100", "SectionIn 1"));
	}
}
