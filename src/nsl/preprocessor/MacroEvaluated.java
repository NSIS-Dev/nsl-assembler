/*
 * MacroEvaluated.java
 */

package nsl.preprocessor;

import java.io.IOException;
import java.util.ArrayList;
import nsl.*;
import nsl.expression.*;
import nsl.statement.*;

/**
 * The contents of a macro after it has been evaluated.
 *
 * @author Stuart
 */
public class MacroEvaluated extends MultipleReturnValueAssembleExpression {
	private final String name;
	private final DefineList defineList;
	private ArrayList<Expression> returnValues;
	private ArrayList<Register> returnRegisters;
	private StatementList statementList;

	private static MacroEvaluated current = null;

	/**
	 * Gets the current {@link MacroEvaluate} object.
	 *
	 * @return the current {@link MacroEvaluate} object
	 */
	public static MacroEvaluated getCurrent() {
		return current;
	}

	/**
	 * Class constructor.
	 *
	 * @param name the macro name
	 * @param defineList the macro arguments
	 */
	public MacroEvaluated(String name, DefineList defineList) {
		this.name = name;
		this.defineList = defineList;
		this.returnValues = new ArrayList<Expression>();
		this.returnRegisters = new ArrayList<Register>();
		this.statementList = null;
	}

	/**
	 * Gets the macro name.
	 *
	 * @return the macro name
	 */
	public String getName() {
		return this.name;
	}

	/** Evaluates the macro's contents. */
	public void evaluate() {
		MacroEvaluated parent = current;
		current = this;
		this.statementList = StatementList.match();
		current = parent;
	}

	/**
	 * Sets the return values for this macro evaluation.
	 *
	 * @param returnValues the return values for this macro evaluation
	 */
	public void setReturnValues(ArrayList<Expression> returnValues) {
		if (returnValues != null && returnValues.size() == 1) {
			Expression returnValue = returnValues.get(0);
			this.type = returnValue.getType();
			this.booleanValue = returnValue.getBooleanValue();
			this.stringValue = returnValue.getStringValue();
			this.integerValue = returnValue.getIntegerValue();
		}

		this.returnValues = returnValues;
	}

	/**
	 * Gets the return value for this macro evaluation.
	 *
	 * @return the return value for this macro evaluation
	 */
	public ArrayList<Expression> getReturnValues() {
		return this.returnValues;
	}

	/**
	 * Gets the register the return value is being assigned to.
	 *
	 * @return the register the return value is being assigned to
	 */
	public ArrayList<Register> getReturnRegisters() {
		return this.returnRegisters;
	}

	/**
	 * Gets the constant list for this macro.
	 *
	 * @return the constant list for this macro
	 */
	public DefineList getDefineList() {
		return this.defineList;
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		ArrayList<Register> parentReturnVars = ReturnVarExpression.setRegisters(this.returnRegisters);
		this.statementList.assemble();
		ReturnVarExpression.setRegisters(parentReturnVars);
	}

	/**
	 * Assembles the source code.
	 *
	 * @param var the variable to assign the value to
	 */
	@Override
	public void assemble(Register var) throws IOException {
		this.returnRegisters.add(var);

		ArrayList<Register> parentReturnVars = ReturnVarExpression.setRegisters(this.returnRegisters);
		this.statementList.assemble();
		ReturnVarExpression.setRegisters(parentReturnVars);
	}

	/**
	 * Assembles the source code.
	 *
	 * @param vars the variables to assign the values to
	 */
	@Override
	public void assemble(ArrayList<Register> vars) throws IOException {
		this.returnRegisters = vars;

		ArrayList<Register> parentReturnVars = ReturnVarExpression.setRegisters(this.returnRegisters);
		this.statementList.assemble();

		// this.returnValues is only empty if an #nsis directive was used in the
		// macro (which sets it to an empty array).
		int returnValuesCount = this.returnValues.size();
		if (this.returnValues != null && returnValuesCount == vars.size()) {
			for (int i = 0; i < returnValuesCount; i++) {
				Expression returnValue = this.returnValues.get(i);
				Register register = vars.get(i);
				if (returnValue instanceof AssembleExpression) {
					String assign = register.toString();
					((AssembleExpression) returnValue).assemble(register);
					String value = register.toString(); // Returns the register or a substituted value, if any
					if (!value.equals(assign)) ScriptParser.writeLine("StrCpy " + assign + " " + value);
				} else {
					String assign = register.toString();
					String value = returnValue.toString();
					if (!value.equals(assign)) ScriptParser.writeLine("StrCpy " + assign + " " + value);
				}
			}
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
		return this.toString(false);
	}
}
