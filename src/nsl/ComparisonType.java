/*
 * ComparisonType.java
 */

package nsl;

/**
 * How two operands should be compared.
 *
 * @author Stuart
 */
public enum ComparisonType {
	Integer,
	IntegerUnsigned,
	String,
	StringCaseSensitive;

	/**
	 * Matches a {@code ComparisonType} value from the current script tokenizer.
	 *
	 * @return a {@code ComparisonType} value
	 */
	public static ComparisonType match() {
		if (ScriptParser.tokenizer.match("u")) return ComparisonType.IntegerUnsigned;
		if (ScriptParser.tokenizer.match("s")) return ComparisonType.String;
		if (ScriptParser.tokenizer.match("S")) return ComparisonType.StringCaseSensitive;
		return ComparisonType.Integer;
	}
}
