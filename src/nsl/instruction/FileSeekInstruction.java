/*
 * FileSeekInstruction.java
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
public class FileSeekInstruction extends AssembleExpression {
	public static final String name = "FileSeek";
	private final Expression handle;
	private final Expression offset;
	private final Expression mode;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public FileSeekInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 1) throw new NslReturnValueException(name, 0, 1);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 2 || paramsCount > 3) throw new NslArgumentException(name, 2, 3);

		this.handle = paramsList.get(0);
		if (!this.handle.getType().equals(ExpressionType.Register))
			throw new NslArgumentException(name, 1, ExpressionType.Register);

		this.offset = paramsList.get(1);

		if (paramsCount > 2) {
			this.mode = paramsList.get(2);
			if (!ExpressionType.isString(this.mode))
				throw new NslArgumentException(name, 3, ExpressionType.String);
		} else {
			if (returns == 1) throw new NslReturnValueException(name, 0);
			this.mode = null;
		}
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		AssembleExpression.assembleIfRequired(this.handle);
		Expression varOrOffset = AssembleExpression.getRegisterOrExpression(this.offset);
		if (this.mode == null) {
			ScriptParser.writeLine(name + " " + this.handle + " " + varOrOffset);
		} else {
			AssembleExpression.assembleIfRequired(this.mode);
			ScriptParser.writeLine(name + " " + this.handle + " " + varOrOffset + " " + this.mode);
		}
		varOrOffset.setInUse(false);
	}

	/**
	 * Assembles the source code.
	 *
	 * @param var the variable to assign the value to
	 */
	@Override
	public void assemble(Register var) throws IOException {
		AssembleExpression.assembleIfRequired(this.handle);
		Expression varOrOffset = AssembleExpression.getRegisterOrExpression(this.offset);
		if (this.mode == null) {
			ScriptParser.writeLine(name + " " + this.handle + " " + varOrOffset + var);
		} else {
			AssembleExpression.assembleIfRequired(this.mode);
			ScriptParser.writeLine(name + " " + this.handle + " " + varOrOffset + " " + this.mode + var);
		}
		varOrOffset.setInUse(false);
	}
}
