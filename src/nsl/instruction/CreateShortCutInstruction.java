/*
 * CreateShortCutInstruction.java
 */

package nsl.instruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;

/**
 * @author Stuart
 */
public class CreateShortCutInstruction extends AssembleExpression {
	public static final String name = "CreateShortCut";
	private final Expression path;
	private final Expression target;
	private final Expression parameters;
	private final Expression iconFile;
	private final Expression iconIndex;
	private final Expression startOptions;
	private final Expression keyboardShortcut;
	private final Expression description;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public CreateShortCutInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 2 || paramsCount > 8) throw new NslArgumentException(name, 1);

		this.path = paramsList.get(0);

		this.target = paramsList.get(1);

		if (paramsCount > 2) {
			this.parameters = paramsList.get(2);

			if (paramsCount > 3) {
				this.iconFile = paramsList.get(3);

				if (paramsCount > 4) {
					this.iconIndex = paramsList.get(4);

					if (paramsCount > 5) {
						this.startOptions = paramsList.get(5);
						if (!ExpressionType.isString(this.startOptions))
							throw new NslArgumentException(name, 6, ExpressionType.String);

						if (paramsCount > 6) {
							this.keyboardShortcut = paramsList.get(6);
							if (!ExpressionType.isString(this.keyboardShortcut))
								throw new NslArgumentException(name, 7, ExpressionType.String);

							if (paramsCount > 7) {
								this.description = paramsList.get(7);
							} else this.description = null;
						} else {
							this.keyboardShortcut = null;
							this.description = null;
						}
					} else {
						this.startOptions = null;
						this.keyboardShortcut = null;
						this.description = null;
					}
				} else {
					this.iconIndex = null;
					this.startOptions = null;
					this.keyboardShortcut = null;
					this.description = null;
				}
			} else {
				this.iconFile = null;
				this.iconIndex = null;
				this.startOptions = null;
				this.keyboardShortcut = null;
				this.description = null;
			}
		} else {
			this.parameters = null;
			this.iconFile = null;
			this.iconIndex = null;
			this.startOptions = null;
			this.keyboardShortcut = null;
			this.description = null;
		}
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		Expression varOrPath = AssembleExpression.getRegisterOrExpression(this.path);
		Expression varOrTarget = AssembleExpression.getRegisterOrExpression(this.target);

		String write = "";

		if (this.parameters != null) {
			AssembleExpression.assembleIfRequired(this.parameters);
			write += " " + this.parameters;

			if (this.iconFile != null) {
				AssembleExpression.assembleIfRequired(this.iconFile);
				write += " " + this.iconFile;

				if (this.iconIndex != null) {
					AssembleExpression.assembleIfRequired(this.iconIndex);
					write += " " + this.iconIndex;

					if (this.startOptions != null) {
						AssembleExpression.assembleIfRequired(this.startOptions);
						write += " " + this.startOptions;

						if (this.keyboardShortcut != null) {
							AssembleExpression.assembleIfRequired(this.keyboardShortcut);
							write += " " + this.keyboardShortcut;

							if (this.description != null) {
								AssembleExpression.assembleIfRequired(this.description);
								write += " " + this.description;
							}
						}
					}
				}
			}
		}

		ScriptParser.writeLine(name + " " + varOrPath + " " + varOrTarget + write);

		varOrPath.setInUse(false);
		varOrTarget.setInUse(false);
	}

	/**
	 * Assembles the source code.
	 *
	 * @param var the variable to assign the value to
	 */
	@Override
	public void assemble(Register var) throws IOException {
		throw new UnsupportedOperationException("Not supported.");
	}
}
