/*
 * IncludeDirective.java
 */

package nsl.preprocessor;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import nsl.*;
import nsl.statement.Statement;
import nsl.statement.StatementList;

/**
 * Allows the contents of another file to be included.
 *
 * @author Stuart
 */
public class IncludeDirective extends Statement {
	private final StatementList statementList;

	/** Class constructor. */
	public IncludeDirective() {
		if (!ScriptParser.tokenizer.tokenIsString())
			throw new NslExpectedException("a file path to include");

		String path = ScriptParser.tokenizer.sval;
		Reader reader;
		try {
			reader = new FileReader(path);
		} catch (IOException ex) {
			throw new NslException(ex.getMessage(), true);
		}

		ScriptParser.pushTokenizer(new Tokenizer(reader, "included script \"" + path + "\""));
		ScriptParser.tokenizer.setAutoPop(false);
		this.statementList = StatementList.match();
		ScriptParser.popTokenizer();

		ScriptParser.tokenizer.tokenNext();
	}

	@Override
	public void assemble() throws IOException {
		this.statementList.assemble();
	}
}
