/*
 * ExecShellWaitInstruction.java
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
public class ExecShellWaitInstruction extends AssembleExpression
{
  public static final String name = "ExecShellWait";
  private final Expression action;
  private final Expression command;
  private final Expression parameters;
  private final Expression show;

  /**
   * Class constructor.
   * @param returns the number of values to return
   */
  public ExecShellWaitInstruction(int returns)
  {
    if (!SectionInfo.in() && !FunctionInfo.in())
      throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), name);
    if (returns > 1)
      throw new NslReturnValueException(name, 0, 1);

    ArrayList<Expression> paramsList = Expression.matchList();
    int paramsCount = paramsList.size();
    if (paramsCount < 2 || paramsCount > 4)
      throw new NslArgumentException(name, 2, 4);

    this.action = paramsList.get(0);

    this.command = paramsList.get(1);

    if (paramsCount > 2)
    {
      this.parameters = paramsList.get(2);

      if (paramsCount > 3)
        this.show = paramsList.get(3);
      else
        this.show = null;
    }
    else
    {
      this.parameters = null;
      this.show = null;
    }
  }

  /**
   * Assembles the source code.
   */
  @Override
  public void assemble() throws IOException
  {
    Expression varOrAction = AssembleExpression.getRegisterOrExpression(this.action);
    Expression varOrCommand = AssembleExpression.getRegisterOrExpression(this.command);
    if (this.parameters != null)
    {
      AssembleExpression.assembleIfRequired(this.parameters);
      if (this.show != null)
      {
        AssembleExpression.assembleIfRequired(this.show);
        ScriptParser.writeLine(name + " " + varOrAction + " " + varOrCommand + " " + this.parameters + " " + this.show);
      }
      else
      {
        ScriptParser.writeLine(name + " " + varOrAction + " " + varOrCommand + " " + this.parameters);
      }
    }
    else
    {
      ScriptParser.writeLine(name + " " + varOrAction + " " + varOrCommand);
    }
    varOrAction.setInUse(false);
    varOrCommand.setInUse(false);
  }

  /**
   * Assembles the source code.
   * @param var the variable to assign the value to
   */
  @Override
  public void assemble(Register var) throws IOException
  {
    Expression varOrAction = AssembleExpression.getRegisterOrExpression(this.action);
    Expression varOrCommand = AssembleExpression.getRegisterOrExpression(this.command);
    if (this.parameters != null)
    {
      AssembleExpression.assembleIfRequired(this.parameters);
      if (this.show != null)
      {
        AssembleExpression.assembleIfRequired(this.show);
        ScriptParser.writeLine(name + " " + varOrAction + " " + varOrCommand + " " + this.parameters + " " + this.show + " " + var);
      }
      else
      {
        ScriptParser.writeLine(name + " " + varOrAction + " " + varOrCommand + " " + this.parameters + " " + var);
      }
    }
    else
    {
      ScriptParser.writeLine(name + " " + varOrAction + " " + varOrCommand + " " + var);
    }
    varOrAction.setInUse(false);
    varOrCommand.setInUse(false);
  }
}
