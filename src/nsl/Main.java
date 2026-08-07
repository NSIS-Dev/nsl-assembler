package nsl;

import java.io.IOException;

/**
 * @author Stuart
 */
public class Main {
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws IOException {
		String scriptPath = null;
		boolean noPauseOnError = false;
		boolean noMakeNSIS = false;

		for (String arg : args) {
			if (arg.equalsIgnoreCase("/nomake")) {
				noMakeNSIS = true;
			} else if (arg.equalsIgnoreCase("/nopause")) {
				noPauseOnError = true;
			} else {
				scriptPath = arg.trim();
			}
		}

		if (scriptPath == null || scriptPath.isEmpty()) showUsage();

		System.exit(ScriptParser.parse(scriptPath, noPauseOnError, noMakeNSIS));
	}

	/** Shows command line usage for the nsL assembler before exiting. */
	private static void showUsage() {
		System.out.println("Usage:");
		System.out.println("  java -jar nsL.jar [Options] script.nsl");
		System.out.println();
		System.out.println("Options:");
		System.out.println("  /nomake   Do not run makensisw.exe after assembling");
		System.out.println("  /nopause  Do not wait for a key press on error");
		System.exit(1);
	}
}
