/*
 * Assembler.java
 */

package nsl;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * Runs the assembler in a subprocess and returns what it emitted.
 *
 * <p>In-process assembly is not repeatable: {@code Scope.global} is {@code static final} and picks
 * up {@code $INSTDIR} and {@code $OUTDIR} from every {@link RegisterList} constructed, so a second
 * script in the same JVM starts with initialisation state left over from the first. Forking gives
 * perfect isolation for the price of about a third of a second, the same trade e2e/run.sh makes.
 *
 * @author Jan T. Sott
 */
public final class Assembler {
	/** Path to the assembler jar, supplied by the build; see build.gradle. */
	private static final String JAR = System.getProperty("nsl.jar", "build/libs/nsL.jar");

	private Assembler() {}

	/** The result of one assembler run. */
	public static final class Result {
		/** The process exit code: 0 success, 1 parse error, 2 assemble error. */
		public final int exitCode;

		/** Everything the assembler printed, both streams merged, newlines normalised. */
		public final String output;

		/** Contents of the emitted .nsi, newlines normalised, or null if none was written. */
		public final String nsi;

		Result(int exitCode, String output, String nsi) {
			this.exitCode = exitCode;
			this.output = output;
			this.nsi = nsi;
		}
	}

	/** Assembles the given source and returns the emitted .nsi, failing if the run did not. */
	public static String assemble(String source) throws Exception {
		Result result = run(source);
		if (result.exitCode != 0) fail("assembler exited " + result.exitCode + ":\n" + result.output);
		return result.nsi;
	}

	/** Assembles the given source expecting failure, and returns the exit code and output. */
	public static Result assembleExpectingError(String source) throws Exception {
		Result result = run(source);
		if (result.exitCode == 0) fail("assembler accepted the source and emitted:\n" + result.nsi);
		return result;
	}

	/**
	 * Writes the source into a scratch directory of its own and assembles it there. The working
	 * directory matters: {@code #include} resolves against the process CWD, and the .nsi lands next
	 * to the .nsl.
	 */
	private static Result run(String source) throws Exception {
		File dir = File.createTempFile("nsl-test", "");
		assertTrue(dir.delete());
		assertTrue(dir.mkdir());
		try {
			File nsl = new File(dir, "case.nsl");
			write(nsl, source);

			// java.home rather than a bare "java", so the subprocess stays on the toolchain
			// Gradle selected for this run.
			ProcessBuilder builder =
					new ProcessBuilder(
							new File(new File(System.getProperty("java.home"), "bin"), "java").getPath(),
							"-jar",
							new File(JAR).getAbsolutePath(),
							nsl.getName(),
							"/nomake",
							"/nopause");
			builder.directory(dir);
			builder.redirectErrorStream(true);

			Process process = builder.start();
			String output = drain(process.getInputStream());
			int exitCode = process.waitFor();

			File nsi = new File(dir, "case.nsi");
			return new Result(exitCode, output, nsi.exists() ? read(nsi) : null);
		} finally {
			delete(dir);
		}
	}

	/** Joins the given lines with newlines. Java 8, so there are no text blocks to use instead. */
	public static String lines(String... lines) {
		StringBuilder builder = new StringBuilder();
		for (String line : lines) builder.append(line).append('\n');
		return builder.toString();
	}

	/** Wraps the body in the smallest script that gives an instruction a section to live in. */
	public static String inSection(String... body) {
		return lines("Name(\"t\");", "OutFile(\"t.exe\");", "", "section Test(\"t\")", "{")
				+ lines(body)
				+ "}\n";
	}

	/** Wraps the body in a script that gives an instruction a function to live in. */
	public static String inFunction(String... body) {
		return lines("Name(\"t\");", "OutFile(\"t.exe\");", "", "function Test()", "{")
				+ lines(body)
				+ lines("}", "", "section S(\"s\")", "{", "  Test();", "}");
	}

	/** Matches an assembled script that contains the given line, ignoring the ones around it. */
	public static Matcher<String> containsLine(final String line) {
		return new BaseMatcher<String>() {
			@Override
			public boolean matches(Object item) {
				return item != null && Arrays.asList(item.toString().split("\n")).contains(line);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("a script containing the line ").appendValue(line);
			}

			@Override
			public void describeMismatch(Object item, Description description) {
				description.appendText("was\n").appendText(String.valueOf(item));
			}
		};
	}

	/** Matches an assembled script that contains the given lines, in order and adjacent. */
	public static Matcher<String> containsLines(final String... lines) {
		return new BaseMatcher<String>() {
			@Override
			public boolean matches(Object item) {
				if (item == null) return false;
				List<String> actual = Arrays.asList(item.toString().split("\n"));
				List<String> expected = Arrays.asList(lines);
				for (int i = 0; i + expected.size() <= actual.size(); i++)
					if (actual.subList(i, i + expected.size()).equals(expected)) return true;
				return false;
			}

			@Override
			public void describeTo(Description description) {
				description
						.appendText("a script containing the lines ")
						.appendValueList("\n", "\n", "", lines);
			}

			@Override
			public void describeMismatch(Object item, Description description) {
				description.appendText("was\n").appendText(String.valueOf(item));
			}
		};
	}

	private static void write(File file, String content) throws IOException {
		OutputStream stream = new FileOutputStream(file);
		try {
			stream.write(content.getBytes("UTF-8"));
		} finally {
			stream.close();
		}
	}

	private static String read(File file) throws IOException {
		InputStream stream = new java.io.FileInputStream(file);
		try {
			return drain(stream);
		} finally {
			stream.close();
		}
	}

	private static String drain(InputStream stream) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] chunk = new byte[8192];
		int read;
		while ((read = stream.read(chunk)) != -1) buffer.write(chunk, 0, read);
		return new String(buffer.toByteArray(), "UTF-8").replace("\r\n", "\n");
	}

	private static void delete(File file) {
		File[] children = file.listFiles();
		if (children != null) for (File child : children) delete(child);
		file.delete();
	}
}
