/*
 * ScriptParserTest.java
 */

package nsl;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests {@link ScriptParser}, which turns a script path into the .nsi path beside it.
 *
 * @author Jan T. Sott
 */
public class ScriptParserTest {
	/** The extension is swapped, whatever it was. */
	@Test
	public void theExtensionIsReplaced() {
		assertEquals("script.nsi", ScriptParser.getOutputPath("script.nsl"));
		assertEquals("script.nsi", ScriptParser.getOutputPath("script.txt"));
		assertEquals("dir/script.nsi", ScriptParser.getOutputPath("dir/script.nsl"));
		assertEquals("dir\\script.nsi", ScriptParser.getOutputPath("dir\\script.nsl"));
	}

	/** A script with no extension gains one rather than losing part of its name. */
	@Test
	public void anExtensionlessScriptGainsOne() {
		assertEquals("script.nsi", ScriptParser.getOutputPath("script"));
		assertEquals("dir/script.nsi", ScriptParser.getOutputPath("dir/script"));
	}

	/** ISSUES.md #4: a dot in a directory is not the extension of the file name. */
	@Test
	public void aDottedDirectoryIsNotAnExtension() {
		assertEquals("../my.dir/script.nsi", ScriptParser.getOutputPath("../my.dir/script"));
		assertEquals("..\\my.dir\\script.nsi", ScriptParser.getOutputPath("..\\my.dir\\script"));
		assertEquals("../my.dir/script.nsi", ScriptParser.getOutputPath("../my.dir/script.nsl"));
		assertEquals("../script.nsi", ScriptParser.getOutputPath("../script"));
	}
}
