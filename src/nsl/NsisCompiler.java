/*
 * NsisCompiler.java
 */

package nsl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * Runs the NSIS compiler over an assembled script.
 *
 * <p>Windows uses <code>makensisw.exe</code>, the GUI compiler shipped with NSIS, which is expected
 * to sit one directory above the assembler. Every other platform uses <code>makensis</code>, the
 * console compiler, resolved from the <code>PATH</code>. The two need different treatment: the GUI
 * owns its own window and is left to run detached, whereas the console compiler writes to this
 * process' streams and must be waited on.
 */
public class NsisCompiler {
	private NsisCompiler() {}

	/** The exit code returned when the NSIS compiler could not be run or reported a failure. */
	public static final int EXIT_COMPILE_FAILED = 3;

	private static final String MAKENSISW = "..\\makensisw.exe";
	private static final String MAKENSIS = "makensis";

	/**
	 * Compiles the given NSIS script.
	 *
	 * @param nsiFile the assembled NSIS script
	 * @param noPauseOnError do not pause on error
	 * @param stdout the standard output writer
	 * @param stderr the standard error writer
	 * @return the exit code
	 */
	public static int compile(
			File nsiFile, boolean noPauseOnError, PrintWriter stdout, PrintWriter stderr)
			throws IOException {
		if (isWindows()) return compileWithMakensisw(nsiFile, noPauseOnError, stderr);
		return compileWithMakensis(nsiFile, stdout, stderr);
	}

	/**
	 * Runs the GUI compiler and returns without waiting for it.
	 *
	 * @param nsiFile the assembled NSIS script
	 * @param noPauseOnError do not pause on error
	 * @param stderr the standard error writer
	 * @return the exit code
	 */
	private static int compileWithMakensisw(File nsiFile, boolean noPauseOnError, PrintWriter stderr)
			throws IOException {
		File makensisw = new File(MAKENSISW);
		if (!makensisw.exists()) {
			stderr.println("Unable to compile \"" + nsiFile.getCanonicalPath() + "\":");
			// getAbsoluteFile() first: getParent() is null for a bare filename,
			// which is what "..\makensisw.exe" is on a non-Windows filesystem.
			stderr.println(
					"  \"makensisw.exe\" not found in \""
							+ makensisw.getAbsoluteFile().getParentFile().getCanonicalPath()
							+ "\".");
			if (!noPauseOnError) System.in.read();
			// Deliberately still 0: makensisw is never waited on, so this branch has
			// no compiler status to report and changing it would alter long-standing
			// behaviour on the only platform that reaches it.
			return 0;
		}

		// Pass the arguments individually; Runtime.exec(String) splits the
		// command on whitespace and does not honour embedded quotes, so any
		// path containing a space would arrive as several arguments.
		new ProcessBuilder(makensisw.getAbsolutePath(), nsiFile.getCanonicalPath()).start();
		return 0;
	}

	/**
	 * Runs the console compiler, waits for it and reports its status.
	 *
	 * @param nsiFile the assembled NSIS script
	 * @param stdout the standard output writer
	 * @param stderr the standard error writer
	 * @return the exit code
	 */
	private static int compileWithMakensis(File nsiFile, PrintWriter stdout, PrintWriter stderr)
			throws IOException {
		ProcessBuilder builder = new ProcessBuilder(MAKENSIS, nsiFile.getCanonicalPath());

		// inheritIO() rather than pipes: makensis prints its progress to stdout and
		// its errors to stderr, and nothing here reads them, so a pipe would fill up
		// and block the compiler forever. Inheriting also hands the bytes straight to
		// the console without this process decoding them in the default charset.
		builder.inheritIO();

		// The compiler writes to the same streams, so flush ours before it starts or
		// the "Assembled successfully." lines appear after its output.
		stdout.flush();
		stderr.flush();

		Process process;
		try {
			process = builder.start();
		} catch (IOException ex) {
			// ProcessBuilder gives a plain IOException whatever went wrong, so this
			// cannot distinguish a missing compiler from a failed exec; in practice it
			// is all but always the former.
			stderr.println("Unable to compile \"" + nsiFile.getCanonicalPath() + "\":");
			stderr.println("  \"" + MAKENSIS + "\" not found on the PATH.");
			// No System.in.read() here, unlike the makensisw branch. That pause exists
			// so a console window opened by a double click does not close before the
			// message can be read; here the terminal outlives the process anyway, and
			// reading from it would suspend a backgrounded build with SIGTTIN.
			return EXIT_COMPILE_FAILED;
		}

		int compilerExitCode;
		try {
			compilerExitCode = process.waitFor();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			process.destroy();
			stderr.println("Interrupted while compiling \"" + nsiFile.getCanonicalPath() + "\".");
			return EXIT_COMPILE_FAILED;
		}

		// makensis has already reported the reason on the inherited stderr; adding
		// another message here would only repeat it.
		if (compilerExitCode != 0) return EXIT_COMPILE_FAILED;
		return 0;
	}

	/**
	 * Determines if the assembler is running on Windows.
	 *
	 * @return <code>true</code> if running on Windows
	 */
	private static boolean isWindows() {
		// Locale.ENGLISH, not the default: under a Turkish locale toLowerCase maps
		// "I" to a dotless "i" and the comparison below never matches.
		return System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("windows");
	}
}
