/*
 * AssignmentExpression.java
 */

package nsl.expression;

import java.io.IOException;
import java.util.ArrayList;
import nsl.*;

/**
 * Describes an assignment statement.
 *
 * @author Stuart
 */
public class AssignmentExpression extends LogicalExpression {
	/** Class constructor. */
	protected AssignmentExpression() {
		super(null, null, null);
	}

	/**
	 * Class constructor
	 *
	 * @param registerIndex the index of the register being assigned to
	 * @param rightOperand the right operand
	 */
	public AssignmentExpression(int registerIndex, Expression rightOperand) {
		super(null, null, rightOperand);

		this.type = ExpressionType.Register;
		this.integerValue = registerIndex;

		if (rightOperand.type.equals(ExpressionType.Register))
			Scope.getCurrent().check(rightOperand.integerValue);
	}

	/**
	 * Returns a string representation of the current object.
	 *
	 * @return a string representation of the current object
	 */
	@Override
	public String toString() {
		if (this.integerValue == -1) return "(?? = " + this.rightOperand + ")";
		return "(" + RegisterList.getCurrent().get(this.integerValue) + " = " + this.rightOperand + ")";
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		Register register;
		if (this.integerValue == -1) register = RegisterList.getCurrent().getNext();
		else register = RegisterList.getCurrent().get(this.integerValue);

		ArrayList<Register> parentReturnVars = ReturnVarExpression.setRegisters(register);

		if (this.rightOperand instanceof AssembleExpression) {
			String assign = register.toString();
			((AssembleExpression) this.rightOperand).assemble(register);
			String value = register.toString(); // Returns the register or a substituted value, if any
			if (!value.equals(assign)) ScriptParser.writeLine("StrCpy " + assign + " " + value);
		} else {
			String assign = register.toString();
			String value = this.rightOperand.toString();
			if (!value.equals(assign)) ScriptParser.writeLine("StrCpy " + assign + " " + value);
		}

		ReturnVarExpression.setRegisters(parentReturnVars);

		if (this.integerValue == -1) register.setInUse(false);
	}

	/**
	 * Assembles the source code.
	 *
	 * @param var the variable to assign the value to
	 */
	@Override
	public void assemble(Register var) throws IOException {
		this.assemble();

		if (var.getIndex() != this.integerValue)
			var.substitute(RegisterList.getCurrent().get(this.integerValue).toString());
	}
}
