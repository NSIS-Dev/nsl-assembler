/*
 * InstructionSignatureTest.java
 */

package nsl;

import static nsl.Assembler.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Asserts the exact NSIS line an instruction wrapper emits.
 *
 * <p>The e2e corpus already proves every wrapper assembles and that makensis accepts the result.
 * What it cannot prove is that the result is <em>right</em>: {@code FileSeek $R0 0 $0} and {@code
 * FileSeek $R0 $0 0} both compile. So this covers one instruction per shape - void,
 * value-returning, Boolean, switch subject - and pins the operand order for each.
 *
 * @author Jan T. Sott
 */
public class InstructionSignatureTest {
	/** Wraps the body in the smallest script that gives an instruction a section to live in. */
	private static String inSection(String body) throws Exception {
		return assemble(
				"Name(\"t\");\nOutFile(\"t.exe\");\n\nsection Test(\"t\")\n{\n" + body + "\n}\n");
	}

	/** Takes arguments, returns nothing: the only thing to get wrong is the operands. */
	@Test
	public void testVoidInstruction() throws Exception {
		String nsi = inSection("  DetailPrint(\"hello\");");
		assertThat(nsi, containsLine("DetailPrint \"hello\""));
	}

	/**
	 * Produces a value, which NSIS spells as an output variable argument. nsL has to place that
	 * argument itself, and where it goes differs per instruction - FileSeek puts it last, after the
	 * optional mode, while FileOpen puts it first.
	 *
	 * <p>Regression test for be3b53b, where the output variable was emitted joined to the preceding
	 * operand.
	 */
	@Test
	public void testValueReturningInstruction() throws Exception {
		String nsi =
				inSection(
						"  $R0 = FileOpen($INSTDIR.\"\\\\f.txt\", \"r\");\n"
								+ "  $0 = FileSeek($R0, 0, \"END\");");
		assertThat(nsi, containsLine("FileOpen $R0 \"$INSTDIR\\f.txt\" \"r\""));
		assertThat(nsi, containsLine("FileSeek $R0 0 \"END\" $0"));
	}

	/**
	 * A Boolean in nsL, a pair of branch labels in NSIS. The true label has to come first and the
	 * body has to land under it, so a swap here is invisible to makensis and inverts the condition.
	 */
	@Test
	public void testBooleanInstruction() throws Exception {
		String nsi = inSection("  if (FileExists($EXEDIR))\n    DetailPrint(\"yes\");");
		assertThat(
				nsi,
				containsLines(
						"IfFileExists $EXEDIR _lbl_0 _lbl_1", "_lbl_0:", "DetailPrint \"yes\"", "_lbl_1:"));
	}

	/**
	 * Where NSIS takes a /FLAG, nsL takes a Boolean. True has to become the flag in the right
	 * position, and false has to emit nothing at all rather than a literal.
	 */
	@Test
	public void testSwitchInstruction() throws Exception {
		String nsi =
				inSection(
						"  Delete($INSTDIR.\"\\\\a.txt\");\n" + "  Delete($INSTDIR.\"\\\\b.txt\", true);");
		assertThat(nsi, containsLine("Delete \"$INSTDIR\\a.txt\""));
		assertThat(nsi, containsLine("Delete /REBOOTOK \"$INSTDIR\\b.txt\""));
	}
}
