/*
 * ManifestAppendCustomStringInstruction.java
 */

package nsl.instruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;

/**
 * @author Jan
 */
public class ManifestAppendCustomStringInstruction extends AssembleExpression
{
  public static final String name = "ManifestAppendCustomString";
  private final Expression path;
  private final Expression string;

  /**
   * Class constructor.
   * @param returns the number of values to return
   */
  public ManifestAppendCustomStringInstruction(int returns)
  {
    if (!ScriptParser.inGlobalContext())
      throw new NslContextException(EnumSet.of(NslContext.Global), name);
    if (returns > 0)
      throw new NslReturnValueException(name);

    ArrayList<Expression> paramsList = Expression.matchList();
    if (paramsList.size() != 2)
      throw new NslArgumentException(name, 2);

    this.path = paramsList.get(0);
    if (!ExpressionType.isString(this.path))
      throw new NslArgumentException(name, 1, ExpressionType.String);

    this.string = paramsList.get(1);
    if (!ExpressionType.isString(this.string))
      throw new NslArgumentException(name, 2, ExpressionType.String);
  }

  /**
   * Assembles the source code.
   */
  @Override
  public void assemble() throws IOException
  {
    AssembleExpression.assembleIfRequired(this.path);
    AssembleExpression.assembleIfRequired(this.string);
    ScriptParser.writeLine(name + " " + this.path + " " + this.string);
  }

  /**
   * Assembles the source code.
   * @param var the variable to assign the value to
   */
  @Override
  public void assemble(Register var) throws IOException
  {
    throw new UnsupportedOperationException("Not supported.");
  }
}
