/*
 * BGFontInstruction.java
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
public class BGFontInstruction extends AssembleExpression {
	public static final String name = "BGFont";
	private final Expression fontFace;
	private final Expression height;
	private final Expression weight;
	private final Expression italic;
	private final Expression underline;
	private final Expression strike;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public BGFontInstruction(int returns) {
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 1 || paramsCount > 6) throw new NslArgumentException(name, 1, 6);

		this.fontFace = paramsList.get(0);
		if (!ExpressionType.isString(this.fontFace))
			throw new NslArgumentException(name, 1, ExpressionType.String);

		if (paramsCount > 1) {
			this.height = paramsList.get(1);
			if (!ExpressionType.isInteger(this.height))
				throw new NslArgumentException(name, 2, ExpressionType.Integer);

			if (paramsCount > 2) {
				this.weight = paramsList.get(2);
				if (!ExpressionType.isInteger(this.weight))
					throw new NslArgumentException(name, 3, ExpressionType.Integer);

				if (paramsCount > 3) {
					this.italic = paramsList.get(3);
					if (!ExpressionType.isBoolean(this.italic))
						throw new NslArgumentException(name, 4, ExpressionType.Boolean);

					if (paramsCount > 4) {
						this.underline = paramsList.get(4);
						if (!ExpressionType.isBoolean(this.underline))
							throw new NslArgumentException(name, 5, ExpressionType.Boolean);

						if (paramsCount > 5) {
							this.strike = paramsList.get(5);
							if (!ExpressionType.isBoolean(this.strike))
								throw new NslArgumentException(name, 6, ExpressionType.Boolean);
						} else {
							this.strike = null;
						}
					} else {
						this.underline = null;
						this.strike = null;
					}
				} else {
					this.italic = null;
					this.underline = null;
					this.strike = null;
				}
			} else {
				this.weight = null;
				this.italic = null;
				this.underline = null;
				this.strike = null;
			}
		} else {
			this.height = null;
			this.weight = null;
			this.italic = null;
			this.underline = null;
			this.strike = null;
		}
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.fontFace);
		String write = name + " " + this.fontFace;

		if (this.height != null) {
			AssembleExpression.assembleIfRequired(this.height);
			write += " " + this.height;

			if (this.weight != null) {
				AssembleExpression.assembleIfRequired(this.weight);
				write += " " + this.weight;

				if (this.italic != null) {
					AssembleExpression.assembleIfRequired(this.italic);
					if (this.italic.getBooleanValue() == true) write += " /ITALIC";

					if (this.underline != null) {
						AssembleExpression.assembleIfRequired(this.underline);
						if (this.underline.getBooleanValue() == true) write += " /UNDERLINE";

						if (this.strike != null) {
							AssembleExpression.assembleIfRequired(this.strike);
							if (this.strike.getBooleanValue() == true) write += " /STRIKE";
						}
					}
				}
			}
		}

		ScriptParser.writeLine(write);
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
