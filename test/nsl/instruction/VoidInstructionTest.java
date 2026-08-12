/*
 * VoidInstructionTest.java
 */

package nsl.instruction;

import static nsl.Assembler.*;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Instructions that take arguments and return nothing.
 *
 * <p>One fork for the whole class: the script is assembled once in {@link #assembleOnce()} and
 * every test asserts against the same output. Adding a case therefore costs an assertion, not a
 * third of a second.
 *
 * <p>A test named for an instruction covers that instruction; the ones still carrying a {@code
 * test} prefix cover a behaviour that spans several.
 *
 * @author Jan T. Sott
 */
public class VoidInstructionTest {
	private static String nsi;

	@BeforeClass
	public static void assembleOnce() throws Exception {
		nsi =
				assemble(
						inSection(
								"  DetailPrint(\"d\");",
								"  SetDetailsPrint(\"both\");",
								"  SetDetailsView(\"show\");",
								"  SetAutoClose(\"false\");",
								"  ClearErrors();",
								"  SetErrors();",
								"  SetErrorLevel(0);",
								"  SetRebootFlag(false);",
								"  SetSilent(\"normal\");",
								"  SetShellVarContext(\"all\");",
								"  SetRegView(\"64\");",
								"  SetOverwrite(\"try\");",
								"  SetCompress(\"off\");",
								"  InitPluginsDir();",
								"  SetPluginUnload(\"alwaysoff\");",
								"  Nop();",
								"  Push(\"p\");",
								"  Exch();",
								"  $R1 = \"s\";",
								"  Exch($R1);",
								"  Exch(1);",
								"  $R0 = Pop();",
								"  Sleep(10);",
								"  $R2 = 5;",
								"  Sleep($R2 * 2);",
								"  SetCurInstType(0);",
								"  BringToFront();",
								"  HideWindow();",
								"  SetDateSave(\"on\");",
								"  SetDatablockOptimize(\"on\");",
								"  SetFileAttributes(\"f.txt\", \"NORMAL\");",
								"  FileBufSize(16384);",
								"  Quit();",
								"  Abort();",
								"  Reboot();"));
	}

	@Test
	public void DetailPrint() {
		assertThat(nsi, containsLine("DetailPrint \"d\""));
	}

	@Test
	public void SetDetailsPrint() {
		assertThat(nsi, containsLine("SetDetailsPrint \"both\""));
	}

	@Test
	public void SetDetailsView() {
		assertThat(nsi, containsLine("SetDetailsView \"show\""));
	}

	@Test
	public void SetAutoClose() {
		assertThat(nsi, containsLine("SetAutoClose \"false\""));
	}

	/** Takes no operand at all, so the only thing to get wrong is emitting one. */
	@Test
	public void ClearErrors() {
		assertThat(nsi, containsLine("ClearErrors"));
	}

	@Test
	public void SetErrors() {
		assertThat(nsi, containsLine("SetErrors"));
	}

	@Test
	public void SetErrorLevel() {
		assertThat(nsi, containsLine("SetErrorLevel 0"));
	}

	/** The Boolean stays a bare true/false here rather than becoming a flag. */
	@Test
	public void SetRebootFlag() {
		assertThat(nsi, containsLine("SetRebootFlag false"));
	}

	@Test
	public void SetSilent() {
		assertThat(nsi, containsLine("SetSilent \"normal\""));
	}

	@Test
	public void SetShellVarContext() {
		assertThat(nsi, containsLine("SetShellVarContext \"all\""));
	}

	@Test
	public void SetRegView() {
		assertThat(nsi, containsLine("SetRegView \"64\""));
	}

	@Test
	public void SetOverwrite() {
		assertThat(nsi, containsLine("SetOverwrite \"try\""));
	}

	@Test
	public void SetCompress() {
		assertThat(nsi, containsLine("SetCompress \"off\""));
	}

	@Test
	public void InitPluginsDir() {
		assertThat(nsi, containsLine("InitPluginsDir"));
	}

	@Test
	public void SetPluginUnload() {
		assertThat(nsi, containsLine("SetPluginUnload \"alwaysoff\""));
	}

	/** Does nothing, so the only thing to get wrong is emitting an operand. */
	@Test
	public void Nop() {
		assertThat(nsi, containsLine("Nop"));
	}

	@Test
	public void Push() {
		assertThat(nsi, containsLine("Push \"p\""));
	}

	/**
	 * Three arities off one wrapper: no operand swaps the top two stack items, a variable swaps with
	 * the top one, an index swaps the top with that item.
	 */
	@Test
	public void Exch() {
		assertThat(nsi, containsLine("Exch"));
		assertThat(nsi, containsLine("Exch $R1"));
		assertThat(nsi, containsLine("Exch 1"));
	}

	@Test
	public void Pop() {
		assertThat(nsi, containsLine("Pop $R0"));
	}

	/** A literal passes straight through; an expression has to be assembled into a register first. */
	@Test
	public void Sleep() {
		assertThat(nsi, containsLine("Sleep 10"));
		assertThat(nsi, containsLines("IntOp $0 $R2 * 2", "Sleep $0"));
	}

	@Test
	public void SetCurInstType() {
		assertThat(nsi, containsLine("SetCurInstType 0"));
	}

	@Test
	public void BringToFront() {
		assertThat(nsi, containsLine("BringToFront"));
	}

	@Test
	public void HideWindow() {
		assertThat(nsi, containsLine("HideWindow"));
	}

	@Test
	public void SetDateSave() {
		assertThat(nsi, containsLine("SetDateSave \"on\""));
	}

	@Test
	public void SetDatablockOptimize() {
		assertThat(nsi, containsLine("SetDatablockOptimize \"on\""));
	}

	@Test
	public void SetFileAttributes() {
		assertThat(nsi, containsLine("SetFileAttributes \"f.txt\" \"NORMAL\""));
	}

	@Test
	public void FileBufSize() {
		assertThat(nsi, containsLine("FileBufSize 16384"));
	}

	@Test
	public void Quit() {
		assertThat(nsi, containsLine("Quit"));
	}

	@Test
	public void Abort() {
		assertThat(nsi, containsLine("Abort"));
	}

	@Test
	public void Reboot() {
		assertThat(nsi, containsLine("Reboot"));
	}
}
