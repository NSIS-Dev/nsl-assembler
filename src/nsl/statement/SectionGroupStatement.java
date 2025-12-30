/*
 * SectionGroupStatement.java
 */

package nsl.statement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;

/**
 * Describes an nsL section group.
 *
 * @author Stuart
 */
public class SectionGroupStatement extends Statement {
	private final boolean uninstall;
	private final String name;
	private final Expression description;
	private final Expression expanded;
	private final Expression bold;
	private final BlockStatement blockStatement;

	/** Class constructor. */
	public SectionGroupStatement() {
		// We can't have a section within a function or section.
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), "section group");

		// Section group name.
		this.name = ScriptParser.tokenizer.matchAWord("a section group name");

		// Section arguments.
		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount > 3) throw new NslArgumentException(name, 0, 3);

		// Section group description.
		if (paramsCount > 0) {
			this.description = paramsList.get(0);
			if (!ExpressionType.isString(this.description))
				throw new NslArgumentException(name, 1, ExpressionType.String);

			// Expanded by default?
			if (paramsCount > 1) {
				this.expanded = paramsList.get(1);
				if (!ExpressionType.isBoolean(this.expanded))
					throw new NslArgumentException(name, 2, ExpressionType.Boolean);

				// Bold?
				if (paramsCount > 2) {
					this.bold = paramsList.get(2);
					if (!ExpressionType.isBoolean(this.bold))
						throw new NslArgumentException(name, 3, ExpressionType.Boolean);
				} else {
					this.bold = null;
				}
			} else {
				this.expanded = null;
				this.bold = null;
			}
		} else {
			this.description = null;
			this.expanded = null;
			this.bold = null;
		}

		this.blockStatement = new BlockStatement();
		this.uninstall = Scope.inUninstaller();
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		String prefix = "";

		if (this.uninstall) prefix += "un.";

		if (this.expanded != null) {
			AssembleExpression.assembleIfRequired(this.expanded);
		}

		if (this.bold != null) {
			AssembleExpression.assembleIfRequired(this.bold);
			if (this.bold.getBooleanValue() == true) prefix += "!";
		}

		if (this.description == null)
			ScriptParser.writeLine("SectionGroup \"" + prefix + "\" " + this.name);
		else
			ScriptParser.writeLine(
					"SectionGroup "
							+ (this.expanded != null && this.expanded.getBooleanValue() == true ? "/e " : "")
							+ "\""
							+ prefix
							+ this.description.toString(true)
							+ "\" "
							+ this.name);

		this.blockStatement.assemble();

		ScriptParser.writeLine("SectionGroupEnd");
	}
}
