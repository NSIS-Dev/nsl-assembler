/*
 * SendMessageInstruction.java
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
public class SendMessageInstruction extends AssembleExpression {
	public static final String name = "SendMessage";
	private final Expression hWnd;
	private final Expression msg;
	private final Expression wParam;
	private final Expression lParam;
	private final Expression timeout;

	/**
	 * Class constructor.
	 *
	 * @param returns the number of values to return
	 */
	public SendMessageInstruction(int returns) {
		if (!SectionInfo.in() && !FunctionInfo.in())
			throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
		if (returns > 1) throw new NslReturnValueException(name, 0, 1);

		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();
		if (paramsCount < 4 || paramsCount > 5) throw new NslArgumentException(name, 1);

		this.hWnd = paramsList.get(0);

		this.msg = paramsList.get(1);

		this.wParam = paramsList.get(2);

		this.lParam = paramsList.get(3);

		if (paramsCount > 4) {
			this.timeout = paramsList.get(4);
			if (!ExpressionType.isInteger(this.timeout))
				throw new NslArgumentException(name, 5, ExpressionType.Integer);
		} else this.timeout = null;
	}

	/**
	 * Assembles the source code.
	 *
	 * @throws IOException
	 */
	@Override
	public void assemble() throws IOException {
		Expression varOrHWnd = AssembleExpression.getRegisterOrExpression(this.hWnd);
		Expression varOrMsg = AssembleExpression.getRegisterOrExpression(this.msg);
		Expression varOrWParam = AssembleExpression.getRegisterOrExpression(this.wParam);
		Expression varOrLParam = AssembleExpression.getRegisterOrExpression(this.lParam);
		if (timeout != null) {
			AssembleExpression.assembleIfRequired(this.timeout);
			ScriptParser.writeLine(
					name
							+ " "
							+ varOrHWnd
							+ " "
							+ varOrMsg
							+ " "
							+ varOrWParam
							+ " "
							+ varOrLParam
							+ " /TIMEOUT="
							+ this.timeout);
		} else {
			ScriptParser.writeLine(
					name + " " + varOrHWnd + " " + varOrMsg + " " + varOrWParam + " " + varOrLParam);
		}
		varOrHWnd.setInUse(false);
		varOrMsg.setInUse(false);
		varOrWParam.setInUse(false);
		varOrLParam.setInUse(false);
	}

	/**
	 * Assembles the source code.
	 *
	 * @param var the variable to assign the value to
	 */
	@Override
	public void assemble(Register var) throws IOException {
		Expression varOrHWnd = AssembleExpression.getRegisterOrExpression(this.hWnd);
		Expression varOrMsg = AssembleExpression.getRegisterOrExpression(this.msg);
		Expression varOrWParam = AssembleExpression.getRegisterOrExpression(this.wParam);
		Expression varOrLParam = AssembleExpression.getRegisterOrExpression(this.lParam);
		if (timeout != null) {
			AssembleExpression.assembleIfRequired(this.timeout);
			ScriptParser.writeLine(
					name
							+ " "
							+ varOrHWnd
							+ " "
							+ varOrMsg
							+ " "
							+ varOrWParam
							+ " "
							+ varOrLParam
							+ " "
							+ var
							+ " /TIMEOUT="
							+ this.timeout);
		} else {
			ScriptParser.writeLine(
					name
							+ " "
							+ varOrHWnd
							+ " "
							+ varOrMsg
							+ " "
							+ varOrWParam
							+ " "
							+ varOrLParam
							+ " "
							+ var);
		}
		varOrHWnd.setInUse(false);
		varOrMsg.setInUse(false);
		varOrWParam.setInUse(false);
		varOrLParam.setInUse(false);
	}
}
