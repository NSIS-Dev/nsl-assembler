/*
 * PluginCallExpression.java
 */

package nsl.expression;

import java.io.IOException;
import java.util.ArrayList;
import nsl.*;

/**
 * Describes a plug-in function call.
 *
 * @author Stuart
 */
public class PluginCallExpression extends MultipleReturnValueAssembleExpression {
	private final ArrayList<Expression> params;
	private boolean noUnload;

	/**
	 * Class constructor.
	 *
	 * @param name the plug-in name and function
	 */
	public PluginCallExpression(String name) {
		this(name, Expression.matchList());
	}

	/**
	 * Class constructor.
	 *
	 * @param name the plug-in name and function
	 * @param params the plug-in function parameters
	 */
	public PluginCallExpression(String name, ArrayList<Expression> params) {
		this.stringValue = name;
		this.params = params;

		if (this.params.size() > 0) {
			Expression param = this.params.get(0);
			if (ExpressionType.isBoolean(param) && param.booleanValue == true) this.noUnload = true;
		}
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
		ArrayList<Register> parentReturnVars = ReturnVarExpression.setRegisters(vars);

		// Plug-in function parameters.
		for (int i = this.params.size() - 1; i >= 0; i--) {
			Expression param = this.params.get(i);
			if (param instanceof AssembleExpression) {
				Register varLeft = RegisterList.getCurrent().get(vars.get(0).integerValue);
				((AssembleExpression) param).assemble(varLeft);
				ScriptParser.writeLine("Push " + varLeft);
			} else {
				ScriptParser.writeLine("Push " + param);
			}
		}

		// Actual plug-in function call.
		if (this.noUnload) ScriptParser.writeLine(this.stringValue + " /NOUNLOAD");
		else ScriptParser.writeLine(this.stringValue);

		// Popping return values.
		for (Expression var : vars) {
			ScriptParser.writeLine("Pop " + var.toString(true));
		}

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
