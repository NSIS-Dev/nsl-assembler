/*
 * PageStatement.java
 */

package nsl.statement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;

/**
 * Inserts an NSIS Page or PageEx instruction.
 *
 * @author Stuart
 */
public class PageStatement extends Statement {
	private final String pageName;
	private final Expression function1, function2, function3OrCaption, enableCancel;
	private final PageExInfo pageExInfo;
	private final BlockStatement pageEx;
	private final boolean uninstall;

	/** Class constructor. */
	public PageStatement() {
		// Must be outside a section or function.
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), "page");

		// Page name.
		this.pageName = ScriptParser.tokenizer.matchAWord("a page name");

		// Validate the page name.
		if (!this.pageName.equals("Custom")
				&& !this.pageName.equals("UninstConfirm")
				&& !this.pageName.equals("License")
				&& !this.pageName.equals("Components")
				&& !this.pageName.equals("Directory")
				&& !this.pageName.equals("InstFiles"))
			throw new NslException("\"" + this.pageName + "\" is not a valid page name", true);

		// Additional options.
		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount > 4) throw new NslArgumentException("page", 0, 4);

		if (paramsCount > 0) {
			this.function1 = paramsList.get(0);
			if (!ExpressionType.isString(this.function1))
				throw new NslArgumentException("page", 1, ExpressionType.String);
		} else this.function1 = null;

		if (paramsCount > 1) {
			this.function2 = paramsList.get(1);
			if (!ExpressionType.isString(this.function2))
				throw new NslArgumentException("page", 2, ExpressionType.String);
		} else this.function2 = null;

		if (paramsCount > 2) {
			this.function3OrCaption = paramsList.get(2);
			if (!ExpressionType.isString(this.function3OrCaption))
				throw new NslArgumentException("page", 3, ExpressionType.String);
		} else this.function3OrCaption = null;

		if (paramsCount > 3) {
			this.enableCancel = paramsList.get(3);
			if (!ExpressionType.isBoolean(this.enableCancel))
				throw new NslArgumentException("page", 4, ExpressionType.Boolean);
		} else this.enableCancel = null;

		// PageEx block.
		if (ScriptParser.tokenizer.tokenIs('{')) {
			this.pageExInfo = new PageExInfo();
			PageExInfo.setCurrent(this.pageExInfo);
			this.pageEx = new BlockStatement();
			PageExInfo.setCurrent(null);
		} else {
			ScriptParser.tokenizer.matchEolOrDie();
			this.pageExInfo = null;
			this.pageEx = null;
		}

		this.uninstall = Scope.inUninstaller();
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		if (this.pageEx == null) {
			String write;

			if (this.uninstall) write = "UninstPage " + this.pageName;
			else write = "Page " + this.pageName;

			if (this.function1 != null) {
				write += " " + this.function1;

				if (this.function2 != null) {
					write += " " + this.function2;

					if (this.function3OrCaption != null) {
						write += " " + this.function3OrCaption;

						if (this.enableCancel != null && this.enableCancel.getBooleanValue() == true) {
							write += " /ENABLECANCEL";
						}
					}
				}
			}

			ScriptParser.writeLine(write);
		} else {
			if (this.uninstall) ScriptParser.writeLine("PageEx un." + this.pageName);
			else ScriptParser.writeLine("PageEx " + this.pageName);

			if (this.function1 != null) {
				String write = "PageCallbacks " + this.function1;

				if (this.function2 != null) {
					write += " " + this.function2;

					if (!this.pageName.equals("Custom") && this.function3OrCaption != null) {
						write += " " + this.function3OrCaption;
					}
				}

				ScriptParser.writeLine(write);
			}

			PageExInfo.setCurrent(this.pageExInfo);
			this.pageEx.assemble();
			PageExInfo.setCurrent(null);

			ScriptParser.writeLine("PageExEnd");
		}
	}
}
