/*
 * NslContextException.java
 */

package nsl;

import java.util.ArrayList;
import java.util.EnumSet;

/**
 * Thrown when code is used in an incorrect context.
 *
 * @author Stuart
 */
public class NslContextException extends NslException {
	/**
	 * Class constructor.
	 *
	 * @param tokenizer the current script tokenizer
	 * @param context which context the instruction may be used in
	 * @param function name of the function
	 */
	public NslContextException(EnumSet<NslContext> context, String function) {
		super(
				String.format("\"%s\" can only be used in a %s context", function, translate(context)),
				true);
	}

	/**
	 * Translates the given {@link EnumSet} of {@link NslContext} values into a readable string.
	 *
	 * @param context the set of contexts
	 * @return the readable string
	 */
	private static String translate(EnumSet<NslContext> context) {
		ArrayList<String> partsList = new ArrayList<String>();
		if (context.contains(NslContext.Global)) partsList.add("global");
		if (context.contains(NslContext.Function)) partsList.add("function");
		if (context.contains(NslContext.Section)) partsList.add("section");
		if (context.contains(NslContext.PageEx)) partsList.add("page block (PageEx)");

		int count = partsList.size();
		if (count == 0) return "??";

		String value = partsList.get(0);
		for (int i = 1; i < count; i++) {
			if (i == count - 1) value += " or " + partsList.get(i);
			else value += ", " + partsList.get(i);
		}

		return value;
	}
}
