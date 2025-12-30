/*
 * NSISDirective.java
 */

package nsl.preprocessor;

import java.io.IOException;
import java.util.ArrayList;
import nsl.*;
import nsl.expression.Expression;
import nsl.statement.Statement;

/**
 * Allows in-line NSIS script.
 *
 * @author Stuart
 */
public class NSISDirective extends Statement {
	private final String nsis;
	private final MacroEvaluated macroEvaluated;

	/** Class constructor. */
	public NSISDirective() {
		this.nsis = ScriptParser.tokenizer.readUntil("#nsisend");
		ScriptParser.tokenizer.tokenNext();

		this.macroEvaluated = MacroEvaluated.getCurrent();
		if (this.macroEvaluated != null)
			this.macroEvaluated.setReturnValues(new ArrayList<Expression>());
	}

	/**
	 * Assembles the source code.
	 *
	 * @throws IOException
	 */
	@Override
	public void assemble() throws IOException {
		if (this.macroEvaluated != null) {
			for (String name : this.macroEvaluated.getDefineList().getNames())
				ScriptParser.writeLine(
						"!define " + name + " " + this.macroEvaluated.getDefineList().get(name));

			if (this.macroEvaluated.getReturnRegisters() != null) {
				int returnRegisterCount = macroEvaluated.getReturnRegisters().size();
				for (int i = 0; i < returnRegisterCount; i++)
					ScriptParser.writeLine(
							"!define ReturnVar"
									+ (i + 1)
									+ " "
									+ this.macroEvaluated.getReturnRegisters().get(i));
			}
		}

		ScriptParser.writeLine(this.nsis);

		if (this.macroEvaluated != null) {
			for (String name : this.macroEvaluated.getDefineList().getNames())
				ScriptParser.writeLine("!undef " + name);

			if (this.macroEvaluated.getReturnRegisters() != null) {
				int returnRegisterCount = this.macroEvaluated.getReturnRegisters().size();
				for (int i = 0; i < returnRegisterCount; i++)
					ScriptParser.writeLine("!undef ReturnVar" + (i + 1));
			}
		}
	}
}
