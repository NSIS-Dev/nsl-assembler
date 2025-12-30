/*
 * Tokenizer.java
 */

package nsl;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;

/**
 * Tokenizes a stream.
 *
 * @author Stuart
 */
public class Tokenizer extends StreamTokenizer {
	private final Reader theReader;
	private final String source;

	private int lineNumberAdd;
	private boolean autoPop;

	/**
	 * Class constructor specifying the {@link Reader} object and the script file path.
	 *
	 * @param reader the {@link Reader} being read from
	 * @param source the source file name or macro name being tokenized
	 */
	public Tokenizer(Reader reader, String source) {
		super(reader);

		this.resetSyntax();
		this.slashSlashComments(true);
		this.slashStarComments(true);
		this.wordChars('a', 'z');
		this.wordChars('A', 'Z');
		this.wordChars('0', '9');
		this.wordChars(160, 255);
		this.wordChars('_', '_');
		this.wordChars('$', '$');
		this.wordChars('#', '#');
		this.whitespaceChars(0, 32);
		// this.ordinaryChar('.');
		// this.ordinaryChar('/');
		// this.ordinaryChar('-');
		this.quoteChar('"');
		this.quoteChar('\'');
		this.quoteChar('`');

		this.theReader = reader;
		this.source = source;

		this.lineNumberAdd = 0;
		this.autoPop = true;
	}

	/**
	 * Gets the {@link Reader} being read from.
	 *
	 * @return the {@link Reader} being read from
	 */
	public Reader getReader() {
		return this.theReader;
	}

	/**
	 * Gets the source file name or macro name being tokenized.
	 *
	 * @return the source file name or macro name being tokenized
	 */
	public String getSource() {
		return this.source;
	}

	/**
	 * Returns the current line number.
	 *
	 * @return the current line number
	 */
	@Override
	public int lineno() {
		return super.lineno() + this.lineNumberAdd;
	}

	/**
	 * Sets whether or not the tokenizer will automatically pop itself off the tokenizer stack when it
	 * reaches the end of its input stream.
	 *
	 * @param autoPop whether or not this tokenizer will auto pop
	 */
	public void setAutoPop(boolean autoPop) {
		this.autoPop = autoPop;
	}

	/**
	 * Checks if the current token matches the given word.
	 *
	 * @param s the string to match
	 * @return <code>true</code> if the word matches the current token
	 */
	public boolean tokenIs(String word) {
		return this.tokenIsWord() && this.sval.equals(word);
	}

	/**
	 * Checks if the current token matches the given character.
	 *
	 * @param c the character to match.
	 * @return <code>true</code> if the character matches the current token
	 */
	public boolean tokenIs(char c) {
		return (char) this.ttype == c;
	}

	/**
	 * Checks if the current token is a character.
	 *
	 * @return <code>true</code> if the current token is a character
	 */
	public boolean tokenIsChar() {
		return !tokenIsWord() && !tokenIsNumber() && !tokenIsString();
	}

	/**
	 * Checks if the current token is a word.
	 *
	 * @return <code>true</code> if the current token is a word
	 */
	public boolean tokenIsWord() {
		return this.ttype == TT_WORD;
	}

	/**
	 * Checks if the current token is a number.
	 *
	 * @return <code>true</code> if the current token is a number
	 */
	public boolean tokenIsNumber() {
		return this.ttype == TT_NUMBER;
	}

	/**
	 * Checks if the current token is a string.
	 *
	 * @return <code>true</code> if the current token is a string
	 */
	public boolean tokenIsString() {
		return this.ttype == '"' || this.ttype == '\'' || this.ttype == '`';
	}

	/**
	 * Reads all characters from the tokenizer stream until the given string is matched.
	 *
	 * @param match the string to match
	 * @return all characters read up until the match string
	 */
	public String readUntil(String match) {
		char[] matchChars = match.toCharArray();
		String text = "";
		try {
			int c = 0;
			while (true) {
				int b = this.theReader.read();

				if (b == -1) {
					this.ttype = TT_EOF;
					throw new NslExpectedException(match);
				}

				if (b == '\r') {
					this.lineNumberAdd++;
					if (!text.isEmpty()) text += '\n';
					b = this.theReader.read();
					continue;
				}

				if (b == '\n') {
					this.lineNumberAdd++;
					if (!text.isEmpty()) text += '\n';
					continue;
				}

				if (b == matchChars[c]) {
					c++;
					if (c == matchChars.length) {
						text = text.substring(0, text.length() - matchChars.length + 1);
						break;
					}
				} else c = 0;

				text += (char) b;
			}
		} catch (IOException ex) {
			throw new NslException(ex.getMessage(), true);
		}
		return text;
	}

	/**
	 * Gets the next token.
	 *
	 * @return <code>true</code> if there was another token
	 */
	public boolean tokenNext() {
		return this.tokenNext(null);
	}

	/**
	 * Gets the next token.
	 *
	 * @param expected the message of the exception to throw if no token exists
	 * @return <code>true</code> if there was another token
	 */
	public boolean tokenNext(String expected) {
		try {
			boolean result = this.nextToken() != TT_EOF;

			if (!result) {
				if (this.autoPop && ScriptParser.popTokenizer() != null) return true;
				if (expected != null) throw new NslExpectedException(expected);
			}

			// We translate numbers ourself.
			if (this.ttype == TT_WORD) {
				char c = this.sval.charAt(0);
				if (c >= '0' && c <= '9') {
					this.ttype = TT_NUMBER;

					try {
						if (this.sval.length() > 1 && this.sval.startsWith("0x")) {
							this.nval = Integer.parseInt(this.sval.substring(2), 16);
						} else {
							this.nval = Integer.parseInt(this.sval);
						}
					} catch (NumberFormatException ex) {
						throw new NslException(ex.getMessage(), true);
					}
				}
			}

			// String with no escape sequences.
			if (this.ttype == '@') {
				int b = this.theReader.read();

				if (b == -1) {
					this.ttype = TT_EOF;
					throw new NslExpectedException("a string");
				}

				this.ttype = (char) b;

				if (b != '"' && b != '\'' && b != '`') {
					throw new NslExpectedException("a string");
				}

				this.sval = readUntil(String.valueOf((char) b));
			}

			return result;
		} catch (IOException ex) {
			throw expected == null
					? new NslException(ex.getMessage(), true)
					: new NslExpectedException(expected);
		}
	}

	/**
	 * Returns the current token as a string.
	 *
	 * @return the current token as a string
	 */
	/*public String tokenToString()
	{
		if (this.tokenIsWord())
			return this.sval;
		if (this.tokenIsNumber())
			return Integer.toString((int)this.nval);
		if (this.tokenIsString())
			return '"' + this.sval + '"';
		return Character.toString((char)this.ttype);
	}*/

	/**
	 * Matches the given character and prints an error otherwise.
	 *
	 * @param c the character to match
	 */
	public void matchOrDie(char c) {
		if (!this.tokenIs(c)) throw new NslExpectedException("\"" + c + "\"");
		this.tokenNext();
	}

	/**
	 * Matches the given word and prints an error otherwise.
	 *
	 * @param word the word to match
	 */
	public void matchOrDie(String word) {
		if (!this.tokenIs(word)) throw new NslExpectedException("\"" + word + "\"");
		this.tokenNext();
	}

	/**
	 * Matches the given character while moving to the next token.
	 *
	 * @param c the character to match
	 * @return <code>true</code> on success
	 */
	public boolean match(char c) {
		if (this.tokenIs(c)) {
			this.tokenNext();
			return true;
		}
		return false;
	}

	/**
	 * Matches the given word while moving to the next token.
	 *
	 * @param word the word to match
	 * @return <code>true</code> on success
	 */
	public boolean match(String word) {
		if (this.tokenIs(word)) {
			this.tokenNext();
			return true;
		}
		return false;
	}

	/** Matches a semicolon on the end of the current line or prints an error otherwise. */
	public void matchEolOrDie() {
		if (!this.tokenIs(';')) throw new NslExpectedException("\";\"");
		this.tokenNext();
	}

	/**
	 * Matches any word and returns that word while moving to the next token.
	 *
	 * @return the word if the current token was a word, or <code>null</code> otherwise
	 */
	public String matchAWord() {
		return this.matchAWord(null);
	}

	/**
	 * Matches any word and returns that word while moving to the next token.
	 *
	 * @param expected the message of the exception to throw if no word exists
	 * @return the word if the current token was a word, or <code>null</code> otherwise
	 */
	public String matchAWord(String expected) {
		if (this.tokenIsWord()) {
			String word = this.sval;
			this.tokenNext();
			return word;
		}
		if (expected != null) throw new NslExpectedException(expected);
		return null;
	}

	/**
	 * Matches any string and returns that string while moving to the next token.
	 *
	 * @return the string if the current token was a string, or <code>null</code> otherwise
	 */
	public String matchAString() {
		return this.matchAString(null);
	}

	/**
	 * Matches any string and returns that string while moving to the next token.
	 *
	 * @param expected the message of the exception to throw if no string exists
	 * @return the string if the current token was a string, or <code>null</code> otherwise
	 */
	public String matchAString(String expected) {
		if (this.tokenIsString()) {
			String string = this.sval;
			this.tokenNext();
			return string;
		}
		if (expected != null) throw new NslExpectedException(expected);
		return null;
	}
}
