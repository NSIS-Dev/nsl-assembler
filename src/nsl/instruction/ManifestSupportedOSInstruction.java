/*
 * ManifestSupportedOSInstruction.java
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
public class ManifestSupportedOSInstruction extends AssembleExpression
{
  public static final String name = "ManifestSupportedOS";
  private final ArrayList<Expression> values;
  private static final Set<String> VALID_OS_VALUES = new HashSet<>(Arrays.asList(
    /**
     * Windows 11 uses the same supportedOS GUID as Windows 10
     *
     * @see https://learn.microsoft.com/en-us/windows/win32/sbscs/application-manifests#supportedOS
     */
    "WinVista", "Win7", "Win8", "Win8.1", "Win10"
  ));

  /**
   * Class constructor.
   * @param returns the number of values to return
   */
  public ManifestSupportedOSInstruction(int returns)
  {
    if (!ScriptParser.inGlobalContext())
      throw new NslContextException(EnumSet.of(NslContext.Global), name);
    if (returns > 0)
      throw new NslReturnValueException(name);

    this.values = Expression.matchList();
    if (this.values.isEmpty())
      throw new NslArgumentException(name, 1, Integer.MAX_VALUE);

    for (int i = 0; i < this.values.size(); i++)
    {
      Expression expr = this.values.get(i);
      if (!ExpressionType.isString(expr))
        throw new NslArgumentException(name, i + 1, ExpressionType.String);

      String osValue = expr.getStringValue();
      if (!VALID_OS_VALUES.contains(osValue))
      {
        throw new NslException(String.format(
          "%s: Invalid Windows version \"%s\" at parameter %d. Valid values are: %s",
          name, osValue, i + 1, String.join(", ", VALID_OS_VALUES)), true);
      }
    }
  }

  /**
   * Assembles the source code.
   */
  @Override
  public void assemble() throws IOException
  {
    StringBuilder output = new StringBuilder(name);
    for (Expression value : this.values)
    {
      AssembleExpression.assembleIfRequired(value);
      output.append(" ").append(value);
    }
    ScriptParser.writeLine(output.toString());
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
