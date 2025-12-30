/*
 * ScriptParser.java
 */

package nsl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Stack;
import nsl.statement.*;

/**
 * Parses an nsL script.
 *
 * @author Stuart
 */
public class ScriptParser {
	private ScriptParser() {}

	public static Tokenizer tokenizer;
	public static Stack<Tokenizer> tokenizers = new Stack<Tokenizer>();
	private static Writer writer;
	private static String scriptPath;

	/**
	 * Parses an nsL script.
	 *
	 * @param path the script file path
	 * @param noPauseOnError do not pause on error
	 * @param noMakeNSIS do not run makensisw.exe
	 * @return the exit code
	 */
	public static int parse(String path, boolean noPauseOnError, boolean noMakeNSIS)
			throws IOException {
		int exitCode = 0;
		scriptPath = path;

		Statement statement = null;
		PrintWriter stdout = new PrintWriter(System.out, true);
		PrintWriter stderr = new PrintWriter(System.err, true);

		try {
			pushTokenizer(new Tokenizer(new FileReader(path), "script \"" + path + "\""));
			try {
				statement = StatementList.match();
			} catch (NslException ex) {
				if (ex.getInner() != null) stderr.println(ex.getInner().toString());
				else stderr.println(ex.getMessage());
				if (!noPauseOnError) System.in.read();
			}
			tokenizer.getReader().close();
		} catch (IOException ex) {
			stderr.println(ex);
			if (!noPauseOnError) System.in.read();
		}

		if (statement != null) {
			File outputFile = new File(getOutputPath(path));

			try {
				writer = new OutputStreamWriter(new FileOutputStream(outputFile));

				// Insert any Var instructions at the top.
				RegisterList.getCurrent().defineVars();

				// Write the NSIS script.
				statement.assemble();

				// Insert .onInit/un.onInit with global assignments if required.
				boolean anyGlobalAssignments = !Statement.getGlobal().isEmpty();
				boolean anyGlobalUninstallerAssignments = !Statement.getGlobalUninstaller().isEmpty();
				if (anyGlobalAssignments || anyGlobalUninstallerAssignments) {
					int onInitDefined = FunctionInfo.isOnInitDefined();

					if (anyGlobalAssignments && (onInitDefined & 1) == 0) {
						writeLine("Function .onInit");
						for (Statement globalStatement : Statement.getGlobal()) globalStatement.assemble();
						writeLine("FunctionEnd");
					}

					if (anyGlobalUninstallerAssignments && (onInitDefined & 2) == 0) {
						writeLine("Function un.onInit");
						for (Statement globalStatement : Statement.getGlobalUninstaller())
							globalStatement.assemble();
						writeLine("FunctionEnd");
					}
				}

				writer.close();
				writer = null;
			} catch (Exception ex) {
				if (writer != null) {
					try {
						writer.close();
					} finally {
					}
					outputFile.delete();
				}

				exitCode = 2;

				if (ex instanceof NslException) stderr.println(ex.getMessage());
				else ex.printStackTrace(stderr);

				if (!noPauseOnError) System.in.read();
			}

			// Successfully written NSIS script file.
			if (exitCode == 0) {
				stdout.println();
				stdout.println("Wrote \"" + outputFile.getCanonicalPath() + "\".");
				stdout.println("Assembled successfully.");
				stdout.println();

				// Build the NSIS script.
				if (!noMakeNSIS) {
					File makensisw = new File("..\\makensisw.exe");
					if (makensisw.exists()) {
						Runtime.getRuntime()
								.exec(
										"\""
												+ makensisw.getAbsolutePath()
												+ "\" \""
												+ outputFile.getCanonicalPath()
												+ "\"");
					} else {
						stderr.println("Unable to compile \"" + outputFile.getCanonicalPath() + "\":");
						stderr.println(
								"  \"makensisw.exe\" not found in \""
										+ (new File(makensisw.getParent())).getCanonicalPath()
										+ "\".");
						if (!noPauseOnError) System.in.read();
					}
				}
			}
		}

		return exitCode;
	}

	/**
	 * Pushes the given tokenizer onto the tokenizer stack and sets the <code>ScriptParser.tokenizer
	 * </code> static variable to it.
	 *
	 * @param push the tokenizer to push
	 */
	public static void pushTokenizer(Tokenizer push) {
		if (tokenizer != null) tokenizers.push(tokenizer);
		tokenizer = push;
		tokenizer.tokenNext("a token");
	}

	/**
	 * Pops a tokenizer off the tokenizer stack sets the <code>ScriptParser.tokenizer</code> static
	 * variable to it.
	 *
	 * @return the tokenizer that was popped or <code>null</code>
	 */
	public static Tokenizer popTokenizer() {
		if (tokenizers.isEmpty()) return null;

		try {
			tokenizer.getReader().close();
		} catch (IOException ex) {
			throw new NslException(ex.getMessage(), true);
		}

		tokenizer = tokenizers.pop();
		return tokenizer;
	}

	/**
	 * Determines if the parser is currently in the global context (i.e. not in a function or
	 * section).
	 *
	 * @return <code>true</code> if in the global context
	 */
	public static boolean inGlobalContext() {
		return !FunctionInfo.in() && !SectionInfo.in() && !PageExInfo.in();
	}

	/**
	 * Write text to the output writer.
	 *
	 * @param text the text to write
	 */
	public static void write(String text) throws IOException {
		writer.write(text);
	}

	/**
	 * Write a line to the output writer.
	 *
	 * @param line the line to write
	 */
	public static void writeLine(String line) throws IOException {
		writer.write(line + "\r\n");
	}

	/**
	 * Write a line to the output writer.
	 *
	 * @param line the line to write
	 */
	public static void writeLine() throws IOException {
		writer.write("\r\n");
	}

	/**
	 * Gets the source script path.
	 *
	 * @return the source script path
	 */
	public static String getScriptPath() {
		return scriptPath;
	}

	/**
	 * Gets the output file path from the given script file path.
	 *
	 * @param scriptPath the script file path
	 * @return the output file path
	 */
	private static String getOutputPath(String scriptPath) {
		int i = scriptPath.lastIndexOf('.');
		if (i == -1) return scriptPath + ".nsi";
		return scriptPath.substring(0, i) + ".nsi";
	}
}
