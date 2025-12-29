/*
 * FileWriteWordInstruction.java
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
public class FileWriteWordInstruction extends AssembleExpression
{
  public static final String name = "FileWriteWord";
  private final Expression handle;
  private final Expression value;

  /**
   * Class constructor.
   * @param returns the number of values to return
   */
  public FileWriteWordInstruction(int returns)
  {
    if (!SectionInfo.in() && !FunctionInfo.in())
      throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
    if (returns > 0)
      throw new NslReturnValueException(name);

    ArrayList<Expression> paramsList = Expression.matchList();
    int paramsCount = paramsList.size();
    if (paramsCount != 2)
      throw new NslArgumentException(name, 2);

    this.handle = paramsList.get(0);
    if (!this.handle.getType().equals(ExpressionType.Register))
      throw new NslArgumentException(name, 1, ExpressionType.Register);

    this.value = paramsList.get(1);
  }

  /**
   * Assembles the source code.
   */
  @Override
  public void assemble() throws IOException
  {
    AssembleExpression.assembleIfRequired(this.handle);
    Expression varOrValue = AssembleExpression.getRegisterOrExpression(this.value);
    ScriptParser.writeLine(name + " " + this.handle + " " + varOrValue);
    varOrValue.setInUse(false);
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
