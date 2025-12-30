/*
 * MultipleReturnValueExpression.java
 */

package nsl.expression;

import java.io.IOException;
import java.util.ArrayList;
import nsl.Register;

/**
 * An expression which can have multiple return values.
 *
 * @author Stuart
 */
public abstract class MultipleReturnValueAssembleExpression extends AssembleExpression {
	/** Assembles the source code. */
	@Override
	public abstract void assemble() throws IOException;

	/**
	 * Assembles the source code.
	 *
	 * @param var the variable to assign the value to
	 */
	@Override
	public abstract void assemble(Register var) throws IOException;

	/**
	 * Assembles the source code.
	 *
	 * @param vars the variables to assign the values to
	 */
	public abstract void assemble(ArrayList<Register> vars) throws IOException;
}
