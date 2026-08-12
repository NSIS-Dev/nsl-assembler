/*
 * FileInstructionTest.java
 */

package nsl.instruction;

import static nsl.Assembler.*;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * File handle I/O and directory search.
 *
 * <p>The trap in this family is that the handle and the output variable are both registers, so
 * transposing them produces a line makensis accepts and that silently reads into the handle. NSIS
 * is also inconsistent about which comes first - {@code FileOpen} outputs the handle and so puts it
 * first, while everything downstream takes the handle as input and puts the output second - and
 * {@code FileSeek} puts its output last of all, behind an optional mode.
 *
 * @author Jan T. Sott
 */
public class FileInstructionTest {
	private static String nsi;

	@BeforeClass
	public static void assembleOnce() throws Exception {
		nsi =
				assemble(
						inSection(
								"  $R0 = FileOpen($INSTDIR.\"\\\\f.txt\", \"w\");",
								"  FileWrite($R0, \"line\");",
								"  FileWriteByte($R0, 13);",
								"  FileWriteWord($R0, 65);",
								"  FileWriteUTF16LE($R0, \"u\");",
								"  FileClose($R0);",
								"  $R0 = FileOpen($INSTDIR.\"\\\\f.txt\", \"r\");",
								"  $R1 = FileRead($R0);",
								"  $R2 = FileRead($R0, 10);",
								"  $R3 = FileReadByte($R0);",
								"  $R4 = FileReadWord($R0);",
								"  $R5 = FileReadUTF16LE($R0);",
								"  $R6 = FileReadUTF16LE($R0, 10);",
								"  $R7 = FileSeek($R0, 0, \"END\");",
								"  FileSeek($R0, 0, \"SET\");",
								"  FileClose($R0);",
								"  ($R8, $R9) = FindFirst($INSTDIR.\"\\\\*.txt\");",
								"  $0 = FindNext($R8);",
								"  FindClose($R8);",
								"  ($R1, $R2) = GetFileTime($INSTDIR.\"\\\\f.txt\");"));
	}

	/** The handle is an output here, so it comes first - the opposite of every reader below. */
	@Test
	public void FileOpen() {
		assertThat(nsi, containsLine("FileOpen $R0 \"$INSTDIR\\f.txt\" \"w\""));
		assertThat(nsi, containsLine("FileOpen $R0 \"$INSTDIR\\f.txt\" \"r\""));
	}

	@Test
	public void FileClose() {
		assertThat(nsi, containsLine("FileClose $R0"));
	}

	/** Writers take the handle first and the value second, with no output variable. */
	@Test
	public void FileWrite() {
		assertThat(nsi, containsLine("FileWrite $R0 \"line\""));
	}

	@Test
	public void FileWriteByte() {
		assertThat(nsi, containsLine("FileWriteByte $R0 13"));
	}

	@Test
	public void FileWriteWord() {
		assertThat(nsi, containsLine("FileWriteWord $R0 65"));
	}

	@Test
	public void FileWriteUTF16LE() {
		assertThat(nsi, containsLine("FileWriteUTF16LE $R0 \"u\""));
	}

	/** Readers take the handle first and the output second, with maxlen after it where allowed. */
	@Test
	public void FileRead() {
		assertThat(nsi, containsLine("FileRead $R0 $R1"));
		assertThat(nsi, containsLine("FileRead $R0 $R2 10"));
	}

	@Test
	public void FileReadByte() {
		assertThat(nsi, containsLine("FileReadByte $R0 $R3"));
	}

	@Test
	public void FileReadWord() {
		assertThat(nsi, containsLine("FileReadWord $R0 $R4"));
	}

	@Test
	public void FileReadUTF16LE() {
		assertThat(nsi, containsLine("FileReadUTF16LE $R0 $R5"));
		assertThat(nsi, containsLine("FileReadUTF16LE $R0 $R6 10"));
	}

	/**
	 * The output variable goes last, behind the optional mode, and is omitted entirely when the
	 * result is unused. Regression test for be3b53b, where it was emitted joined to the mode.
	 */
	@Test
	public void FileSeek() {
		assertThat(nsi, containsLine("FileSeek $R0 0 \"END\" $R7"));
		assertThat(nsi, containsLine("FileSeek $R0 0 \"SET\""));
	}

	/** Two outputs, handle then filename, with the search pattern behind both. */
	@Test
	public void FindFirst() {
		assertThat(nsi, containsLine("FindFirst $R8 $R9 \"$INSTDIR\\*.txt\""));
	}

	/** Handle in, filename out - the reverse of FindFirst's pair. */
	@Test
	public void FindNext() {
		assertThat(nsi, containsLine("FindNext $R8 $0"));
	}

	@Test
	public void FindClose() {
		assertThat(nsi, containsLine("FindClose $R8"));
	}

	/** Input first, then both halves of the time, unlike the readers above. */
	@Test
	public void GetFileTime() {
		assertThat(nsi, containsLine("GetFileTime \"$INSTDIR\\f.txt\" $R1 $R2"));
	}
}
