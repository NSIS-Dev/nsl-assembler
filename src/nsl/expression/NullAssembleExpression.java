/*
 * NullAssembleExpression.java
 */

package nsl.expression;

import java.io.IOException;
import nsl.Register;

/**
 * An expression that when assembled, assembles nothing.
 *
 * @author Stuart
 */
public class NullAssembleExpression extends AssembleExpression {
	/** Assembles nothing. */
	@Override
	public void assemble() throws IOException {}

	/** Assembles nothing. */
	@Override
	public void assemble(Register var) throws IOException {}
}
