/*
 * SetCompressionLevelInstruction.java
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
public class SetCompressionLevelInstruction extends AssembleExpression
{
  public static final String name = "SetCompressionLevel";
  private final Expression level;

  /**
   * Class constructor.
   * @param returns the number of values to return
   */
  public SetCompressionLevelInstruction(int returns)
  {
    if (!ScriptParser.inGlobalContext())
      throw new NslContextException(EnumSet.of(NslContext.Global), name);
    if (returns > 0)
      throw new NslReturnValueException(name);

    ArrayList<Expression> paramsList = Expression.matchList();
    if (paramsList.size() != 1)
      throw new NslArgumentException(name, 1);

    this.level = paramsList.get(0);
    if (!ExpressionType.isInteger(this.level))
      throw new NslArgumentException(name, 1, ExpressionType.Integer);

    // Validate that level is between 0 and 9
    int levelValue = this.level.getIntegerValue();
    if (levelValue < 0 || levelValue > 9)
    {
      throw new NslException(String.format(
        "%s: Invalid compression level %d. Valid range is 0-9.",
        name, levelValue), true);
    }
  }

  /**
   * Assembles the source code.
   */
  @Override
  public void assemble() throws IOException
  {
    AssembleExpression.assembleIfRequired(this.level);
    ScriptParser.writeLine(name + " " + this.level);
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
