package nsl.test;

import java.io.FileReader;
import java.io.StreamTokenizer;
import nsl.Tokenizer;

/**
 * @author Stuart
 */
public class TestMain {
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		if (args.length == 0) return;

		try {
			FileReader fileReader = new FileReader(args[0]);
			Tokenizer tokenizer = new Tokenizer(fileReader, args[0]);

			while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
				if (tokenizer.ttype == StreamTokenizer.TT_WORD)
					System.out.println("word = " + tokenizer.sval);
				else if (tokenizer.ttype == StreamTokenizer.TT_NUMBER)
					System.out.println("number = " + tokenizer.nval);
				else if (tokenizer.ttype == '"') System.out.println("string = " + tokenizer.sval);
				else System.out.println("char = " + (char) tokenizer.ttype);
			}

			fileReader.close();
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}
}
