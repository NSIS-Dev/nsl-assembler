/*
 * FunctionCallExpression.java
 */

package nsl.expression;

import java.io.IOException;
import java.util.ArrayList;
import nsl.*;

/**
 * Describes a function call.
 *
 * @author Stuart
 */
public class FunctionCallExpression extends MultipleReturnValueAssembleExpression {
	private final ArrayList<Expression> params;
	private final int lineNo;

	/**
	 * Class constructor.
	 *
	 * @param name the function name
	 */
	public FunctionCallExpression(String name) {
		this.stringValue = name;
		this.params = Expression.matchList();
		this.lineNo = ScriptParser.tokenizer.lineno();
	}

	/**
	 * Class constructor.
	 *
	 * @param name the function name
	 * @param params the function parameters
	 */
	public FunctionCallExpression(String name, ArrayList<Expression> params) {
		this.stringValue = name;
		this.params = params;
		this.lineNo = ScriptParser.tokenizer.lineno();
	}

	/**
	 * Gets the function call parameters.
	 *
	 * @return the function call parameters
	 */
	public ArrayList<Expression> getParams() {
		return this.params;
	}

	/** Assembles the source code. */
	public void assemble() throws IOException {
		this.assemble(new ArrayList<Register>());
	}

	/**
	 * Assembles the source code.
	 *
	 * @param var the variable to assign the value to
	 */
	public void assemble(Register var) throws IOException {
		ArrayList<Register> vars = new ArrayList<Register>();
		vars.add(var);
		this.assemble(vars);
	}

	/**
	 * Assembles the source code.
	 *
	 * @param vars the variables to assign the values to
	 */
	public void assemble(ArrayList<Register> vars) throws IOException {
		FunctionInfo functionInfo =
				FunctionInfo.find(this.stringValue, this.params.size(), vars.size());
		if (functionInfo == null)
			throw new NslException(
					"Function \""
							+ this.stringValue
							+ "\" not found that expects "
							+ this.params.size()
							+ " parameters and returns "
							+ vars.size()
							+ " values",
					this.lineNo);

		ArrayList<Register> parentReturnVars = ReturnVarExpression.setRegisters(vars);

		// Any variables used in the function that aren't used to call it must be pushed onto the stack.
		ArrayList<Expression> usedVarsList = new ArrayList<Expression>();
		for (Register param : functionInfo.getUsedVars().values()) {
			if (Expression.findRegister(CodeInfo.getCurrent().getUsedVars(), param) != null
					&& Expression.findRegister(this.params, param) == null
					&& Expression.findRegister(vars, param) == null) {
				ScriptParser.writeLine("Push " + param);
				usedVarsList.add(param);
			}
		}

		// Function parameters.
		for (int i = this.params.size() - 1; i >= 0; i--) {
			Expression param = this.params.get(i);
			if (param instanceof AssembleExpression) {
				Register varLeft;
				if (vars.isEmpty()) varLeft = RegisterList.getCurrent().getNext();
				else varLeft = RegisterList.getCurrent().get(vars.get(0).integerValue);
				((AssembleExpression) param).assemble(varLeft);
				ScriptParser.writeLine("Push " + varLeft.toString(true));
			} else {
				ScriptParser.writeLine("Push " + param);
			}
		}

		// Actual function call.
		ScriptParser.writeLine("Call " + functionInfo.getName());

		// Popping return values.
		if (vars.isEmpty()) // Not explicitly getting any return values.
		{
			// Got to clear the stack.
			if (functionInfo.getUsedVars().isEmpty()) {
				Register var = RegisterList.getCurrent().getNext();
				for (int i = 0; i < functionInfo.getReturns(); i++) ScriptParser.writeLine("Pop " + var);
				var.setInUse(false);
			} else {
				for (int i = 0; i < functionInfo.getReturns(); i++)
					ScriptParser.writeLine("Pop " + functionInfo.getUsedVars().get(0).toString());
			}
		} else {
			for (Expression var : vars) {
				ScriptParser.writeLine("Pop " + var.toString());
			}
		}

		// Any variables used in the function that aren't used to call it must be popped back off the
		// stack.
		for (int i = usedVarsList.size() - 1; i >= 0; i--)
			ScriptParser.writeLine("Pop " + usedVarsList.get(i));

		ReturnVarExpression.setRegisters(parentReturnVars);
	}

	/**
	 * Returns a string representation of the current object.
	 *
	 * @return a string representation of the current object
	 */
	@Override
	public String toString() {
		String args = "";
		int argsCount = this.params.size();
		for (int i = 0; i < argsCount; i++) {
			if (i == argsCount - 1) args += this.params.get(i);
			else args += this.params.get(i) + ", ";
		}
		return this.stringValue + "(" + args + ")";
	}
}
