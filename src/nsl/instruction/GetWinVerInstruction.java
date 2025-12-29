/*
 * GetWinVerInstruction.java
 */

package nsl.instruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import nsl.*;
import nsl.expression.*;

/**
 * @author Jan
 */
public class GetWinVerInstruction extends AssembleExpression
{
  public static final String name = "GetWinVer";

  private static final Set<String> VALID_FIELDS = new HashSet<>(Arrays.asList(
    "Major", "Minor", "Build", "ServicePack"
  ));

  private final Expression field;

  /**
   * Class constructor.
   * @param returns the number of values to return
   */
  public GetWinVerInstruction(int returns)
  {
    if (PageExInfo.in())
      throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function, NslContext.Global), name);
    if (returns != 1)
      throw new NslReturnValueException(name, 1);

    ArrayList<Expression> paramsList = Expression.matchList();
    if (paramsList.size() != 1)
      throw new NslArgumentException(name, 1);

    this.field = paramsList.get(0);
    if (!ExpressionType.isString(this.field))
      throw new NslArgumentException(name, 1, ExpressionType.String);

    String fieldValue = this.field.getStringValue();
    if (!VALID_FIELDS.contains(fieldValue))
    {
      throw new NslException(String.format(
        "%s: Invalid field \"%s\". Valid values are: %s",
        name, fieldValue, String.join(", ", VALID_FIELDS)), true);
    }
  }

  /**
   * Assembles the source code.
   */
  @Override
  public void assemble() throws IOException
  {
    throw new UnsupportedOperationException("Not supported.");
  }

  /**
   * Assembles the source code.
   * @param var the variable to assign the value to
   */
  @Override
  public void assemble(Register var) throws IOException
  {
    Expression varOrField = AssembleExpression.getRegisterOrExpression(this.field);
    ScriptParser.writeLine(name + " " + var + " " + varOrField);
    varOrField.setInUse(false);
  }
}
