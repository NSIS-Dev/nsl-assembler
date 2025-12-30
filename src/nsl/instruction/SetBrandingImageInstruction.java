/*
 * SetBrandingImageInstruction.java
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
public class SetBrandingImageInstruction extends AssembleExpression {
	public static final String name = "SetBrandingImage";
	private final Expression file;
	private final Expression resizeToFit;
	private final Expression imgId;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public SetBrandingImageInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 0) throw new NslReturnValueException(name);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 1 || paramsCount > 3) throw new NslArgumentException(name, 1, 3);

		this.file = paramsList.get(0);

		if (paramsCount > 1) {
			this.resizeToFit = paramsList.get(1);
			if (!ExpressionType.isBoolean(this.resizeToFit))
				throw new NslArgumentException(name, 2, ExpressionType.Boolean);

			if (paramsCount > 2) {
				this.imgId = paramsList.get(2);
				if (!ExpressionType.isInteger(this.imgId))
					throw new NslArgumentException(name, 3, ExpressionType.Integer);
			} else {
				this.imgId = null;
			}
		} else {
			this.resizeToFit = null;
			this.imgId = null;
		}
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		throw new UnsupportedOperationException("Not supported.");
	}

	/**
	 * Assembles the source code.
	 *
	 * @param var the variable to assign the value to
	 */
	@Override
	public void assemble(Register var) throws IOException {
		Expression varOrFile = AssembleExpression.getRegisterOrExpression(this.file);
		String write = name + " " + var + " " + varOrFile;

		if (this.resizeToFit != null) {
			AssembleExpression.assembleIfRequired(this.resizeToFit);

			if (this.imgId != null) {
				AssembleExpression.assembleIfRequired(this.imgId);
				if (this.imgId.getBooleanValue() == true) write += " /IMGID=" + this.imgId;
			}

			if (this.resizeToFit.getBooleanValue() == true) write += " /RESIZETOFIT";
		}

		ScriptParser.writeLine(write);
		varOrFile.setInUse(false);
	}
}
