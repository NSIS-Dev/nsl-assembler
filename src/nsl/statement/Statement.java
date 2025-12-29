/*
 * Statement.java
 */

package nsl.statement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;
import nsl.instruction.*;
import nsl.preprocessor.*;

/**
 * Parses a generic nsL statement.
 * @author Stuart
 */
public abstract class Statement
{
  protected static ArrayList<Statement> globalAssignmentStatements = new ArrayList<Statement>();
  protected static ArrayList<Statement> globalUninstallerAssignmentStatements = new ArrayList<Statement>();

  /**
   * Adds a statement to the global assignments list.
   * @param add the statement to add
   */
  public static void addGlobal(Statement add)
  {
    if (Scope.inUninstaller())
      globalUninstallerAssignmentStatements.add(add);
    else
      globalAssignmentStatements.add(add);
  }

  /**
   * Gets the list of global assignment statements.
   * @return the list of global assignment statements
   */
  public static ArrayList<Statement> getGlobal()
  {
    return globalAssignmentStatements;
  }

  /**
   * Gets the list of global uninstaller assignment statements.
   * @return the list of global uninstaller assignment statements
   */
  public static ArrayList<Statement> getGlobalUninstaller()
  {
    return globalUninstallerAssignmentStatements;
  }

  /**
   * Abstract class constructor.
   */
  protected Statement()
  {
  }

  /**
   * Matches an NSIS instruction.
   * @param returns the number of values to return
   * @return the instruction or <code>null</code> if the current token does not
   * match an NSIS instruction
   */
  public static AssembleExpression matchInstruction(int returns)
  {
    if (ScriptParser.tokenizer.match(AbortInstruction.name))
      return new AbortInstruction(returns);
    if (ScriptParser.tokenizer.match(AddBrandingImageInstruction.name))
      return new AddBrandingImageInstruction(returns);
    if (ScriptParser.tokenizer.match(AddSizeInstruction.name))
      return new AddSizeInstruction(returns);
    if (ScriptParser.tokenizer.match(AllowRootDirInstallInstruction.name))
      return new AllowRootDirInstallInstruction(returns);
    if (ScriptParser.tokenizer.match(AllowSkipFilesInstruction.name))
      return new AllowSkipFilesInstruction(returns);
    if (ScriptParser.tokenizer.match(AutoCloseWindowInstruction.name))
      return new AutoCloseWindowInstruction(returns);
    if (ScriptParser.tokenizer.match(BGFontInstruction.name))
      return new BGFontInstruction(returns);
    if (ScriptParser.tokenizer.match(BGGradientInstruction.name))
      return new BGGradientInstruction(returns);
    if (ScriptParser.tokenizer.match(BrandingTextInstruction.name))
      return new BrandingTextInstruction(returns);
    if (ScriptParser.tokenizer.match(BringToFrontInstruction.name))
      return new BringToFrontInstruction(returns);
    if (ScriptParser.tokenizer.match(CaptionInstruction.name))
      return new CaptionInstruction(returns);
    if (ScriptParser.tokenizer.match(CheckBitmapInstruction.name))
      return new CheckBitmapInstruction(returns);
    if (ScriptParser.tokenizer.match(ClearErrorsInstruction.name))
      return new ClearErrorsInstruction(returns);
    if (ScriptParser.tokenizer.match(CompletedTextInstruction.name))
      return new CompletedTextInstruction(returns);
    if (ScriptParser.tokenizer.match(ComponentTextInstruction.name))
      return new ComponentTextInstruction(returns);
    if (ScriptParser.tokenizer.match(CopyFilesInstruction.name))
      return new CopyFilesInstruction(returns);
    if (ScriptParser.tokenizer.match(CRCCheckInstruction.name))
      return new CRCCheckInstruction(returns);
    if (ScriptParser.tokenizer.match(CreateDirectoryInstruction.name))
      return new CreateDirectoryInstruction(returns);
    if (ScriptParser.tokenizer.match(CreateFontInstruction.name))
      return new CreateFontInstruction(returns);
    if (ScriptParser.tokenizer.match(CreateShortCutInstruction.name) || ScriptParser.tokenizer.match("CreateShortcut"))
      return new CreateShortCutInstruction(returns);
    if (ScriptParser.tokenizer.match(DeleteInstruction.name))
      return new DeleteInstruction(returns);
    if (ScriptParser.tokenizer.match(DeleteINISecInstruction.name))
      return new DeleteINISecInstruction(returns);
    if (ScriptParser.tokenizer.match(DeleteINIStrInstruction.name))
      return new DeleteINIStrInstruction(returns);
    if (ScriptParser.tokenizer.match(DeleteRegKeyInstruction.name))
      return new DeleteRegKeyInstruction(returns);
    if (ScriptParser.tokenizer.match(DeleteRegValueInstruction.name))
      return new DeleteRegValueInstruction(returns);
    if (ScriptParser.tokenizer.match(DetailPrintInstruction.name))
      return new DetailPrintInstruction(returns);
    if (ScriptParser.tokenizer.match(DetailsButtonTextInstruction.name))
      return new DetailsButtonTextInstruction(returns);
    if (ScriptParser.tokenizer.match(DirTextInstruction.name))
      return new DirTextInstruction(returns);
    if (ScriptParser.tokenizer.match(DirVarInstruction.name))
      return new DirVarInstruction(returns);
    if (ScriptParser.tokenizer.match(DirVerifyInstruction.name))
      return new DirVerifyInstruction(returns);
    if (ScriptParser.tokenizer.match(EnableWindowInstruction.name))
      return new EnableWindowInstruction(returns);
    if (ScriptParser.tokenizer.match(EnumRegKeyInstruction.name))
      return new EnumRegKeyInstruction(returns);
    if (ScriptParser.tokenizer.match(EnumRegValueInstruction.name))
      return new EnumRegValueInstruction(returns);
    if (ScriptParser.tokenizer.match(ExecInstruction.name))
      return new ExecInstruction(returns);
    if (ScriptParser.tokenizer.match(ExecShellInstruction.name))
      return new ExecShellInstruction(returns);
    if (ScriptParser.tokenizer.match(ExecWaitInstruction.name))
      return new ExecWaitInstruction(returns);
    if (ScriptParser.tokenizer.match(ExpandEnvStringsInstruction.name))
      return new ExpandEnvStringsInstruction(returns);
    if (ScriptParser.tokenizer.match(FileBufSizeInstruction.name))
      return new FileBufSizeInstruction(returns);
    if (ScriptParser.tokenizer.match(FileCloseInstruction.name))
      return new FileCloseInstruction(returns);
    if (ScriptParser.tokenizer.match(FileErrorTextInstruction.name))
      return new FileErrorTextInstruction(returns);
    if (ScriptParser.tokenizer.match(FileInstruction.name))
      return new FileInstruction(returns);
    if (ScriptParser.tokenizer.match(FileOpenInstruction.name))
      return new FileOpenInstruction(returns);
    if (ScriptParser.tokenizer.match(FileReadByteInstruction.name))
      return new FileReadByteInstruction(returns);
    if (ScriptParser.tokenizer.match(FileReadInstruction.name))
      return new FileReadInstruction(returns);
    if (ScriptParser.tokenizer.match(FileRecursiveInstruction.name))
      return new FileRecursiveInstruction(returns);
    if (ScriptParser.tokenizer.match(FileSeekInstruction.name))
      return new FileSeekInstruction(returns);
    if (ScriptParser.tokenizer.match(FileWriteByteInstruction.name))
      return new FileWriteByteInstruction(returns);
    if (ScriptParser.tokenizer.match(FileWriteInstruction.name))
      return new FileWriteInstruction(returns);
    if (ScriptParser.tokenizer.match(FindCloseInstruction.name))
      return new FindCloseInstruction(returns);
    if (ScriptParser.tokenizer.match(FindFirstInstruction.name))
      return new FindFirstInstruction(returns);
    if (ScriptParser.tokenizer.match(FindNextInstruction.name))
      return new FindNextInstruction(returns);
    if (ScriptParser.tokenizer.match(FindWindowInstruction.name))
      return new FindWindowInstruction(returns);
    if (ScriptParser.tokenizer.match(FlushINIInstruction.name))
      return new FlushINIInstruction(returns);
    if (ScriptParser.tokenizer.match(GetCurInstTypeInstruction.name))
      return new GetCurInstTypeInstruction(returns);
    if (ScriptParser.tokenizer.match(GetDlgItemInstruction.name))
      return new GetDlgItemInstruction(returns);
    if (ScriptParser.tokenizer.match(GetDLLVersionInstruction.name))
      return new GetDLLVersionInstruction(returns);
    if (ScriptParser.tokenizer.match(GetDLLVersionLocalInstruction.name))
      return new GetDLLVersionLocalInstruction(returns);
    if (ScriptParser.tokenizer.match(GetErrorLevelInstruction.name))
      return new GetErrorLevelInstruction(returns);
    if (ScriptParser.tokenizer.match(GetFileTimeInstruction.name))
      return new GetFileTimeInstruction(returns);
    if (ScriptParser.tokenizer.match(GetFileTimeLocalInstruction.name))
      return new GetFileTimeLocalInstruction(returns);
    if (ScriptParser.tokenizer.match(GetInstDirErrorInstruction.name))
      return new GetInstDirErrorInstruction(returns);
    if (ScriptParser.tokenizer.match(GetTempFileNameInstruction.name))
      return new GetTempFileNameInstruction(returns);
    if (ScriptParser.tokenizer.match(HideWindowInstruction.name))
      return new HideWindowInstruction(returns);
    if (ScriptParser.tokenizer.match(IconInstruction.name))
      return new IconInstruction(returns);
    if (ScriptParser.tokenizer.match(IfAbortInstruction.name))
      return new IfAbortInstruction(returns);
    if (ScriptParser.tokenizer.match(IfErrorsInstruction.name))
      return new IfErrorsInstruction(returns);
    if (ScriptParser.tokenizer.match(IfFileExistsInstruction.name))
      return new IfFileExistsInstruction(returns);
    if (ScriptParser.tokenizer.match(IfRebootFlagInstruction.name))
      return new IfRebootFlagInstruction(returns);
    if (ScriptParser.tokenizer.match(IfSilentInstruction.name))
      return new IfSilentInstruction(returns);
    if (ScriptParser.tokenizer.match(InitPluginsDirInstruction.name))
      return new InitPluginsDirInstruction(returns);
    if (ScriptParser.tokenizer.match(InstProgressFlagsInstruction.name))
      return new InstProgressFlagsInstruction(returns);
    if (ScriptParser.tokenizer.match(InstallDirInstruction.name))
      return new InstallDirInstruction(returns);
    if (ScriptParser.tokenizer.match(InstallDirRegKeyInstruction.name))
      return new InstallDirRegKeyInstruction(returns);
    if (ScriptParser.tokenizer.match(InstallButtonTextInstruction.name))
      return new InstallButtonTextInstruction(returns);
    if (ScriptParser.tokenizer.match(InstallColorsInstruction.name))
      return new InstallColorsInstruction(returns);
    if (ScriptParser.tokenizer.match(InstTypeGetTextInstruction.name))
      return new InstTypeGetTextInstruction(returns);
    if (ScriptParser.tokenizer.match(InstTypeInstruction.name))
      return new InstTypeInstruction(returns);
    if (ScriptParser.tokenizer.match(InstTypeSetTextInstruction.name))
      return new InstTypeSetTextInstruction(returns);
    if (ScriptParser.tokenizer.match(IntFmtInstruction.name))
      return new IntFmtInstruction(returns);
    if (ScriptParser.tokenizer.match(IsWindowInstruction.name))
      return new IsWindowInstruction(returns);
    if (ScriptParser.tokenizer.match(LangStringInstruction.name))
      return new LangStringInstruction(returns);
    if (ScriptParser.tokenizer.match(LoadLanguageFileInstruction.name))
      return new LoadLanguageFileInstruction(returns);
    if (ScriptParser.tokenizer.match(LockWindowInstruction.name))
      return new LockWindowInstruction(returns);
    if (ScriptParser.tokenizer.match(LogSetInstruction.name))
      return new LogSetInstruction(returns);
    if (ScriptParser.tokenizer.match(LogTextInstruction.name))
      return new LogTextInstruction(returns);
    if (ScriptParser.tokenizer.match(LicenseBkColorInstruction.name))
      return new LicenseBkColorInstruction(returns);
    if (ScriptParser.tokenizer.match(LicenseDataInstruction.name))
      return new LicenseDataInstruction(returns);
    if (ScriptParser.tokenizer.match(LicenseForceSelectionInstruction.name))
      return new LicenseForceSelectionInstruction(returns);
    if (ScriptParser.tokenizer.match(LicenseLangStringInstruction.name))
      return new LicenseLangStringInstruction(returns);
    if (ScriptParser.tokenizer.match(LicenseTextInstruction.name))
      return new LicenseTextInstruction(returns);
    if (ScriptParser.tokenizer.match(MessageBoxInstruction.name))
      return new MessageBoxInstruction(returns);
    if (ScriptParser.tokenizer.match(MiscButtonTextInstruction.name))
      return new MiscButtonTextInstruction(returns);
    if (ScriptParser.tokenizer.match(ManifestDPIAwareInstruction.name))
      return new ManifestDPIAwareInstruction(returns);
    if (ScriptParser.tokenizer.match(ManifestLongPathAwareInstruction.name))
      return new ManifestLongPathAwareInstruction(returns);
    if (ScriptParser.tokenizer.match(ManifestSupportedOSInstruction.name))
      return new ManifestSupportedOSInstruction(returns);
    if (ScriptParser.tokenizer.match(NameInstruction.name))
      return new NameInstruction(returns);
    if (ScriptParser.tokenizer.match(PopInstruction.name))
      return new PopInstruction(returns);
    if (ScriptParser.tokenizer.match(PushInstruction.name))
      return new PushInstruction(returns);
    if (ScriptParser.tokenizer.match(OutFileInstruction.name))
      return new OutFileInstruction(returns);
    if (ScriptParser.tokenizer.match(QuitInstruction.name))
      return new QuitInstruction(returns);
    if (ScriptParser.tokenizer.match(ReadEnvStrInstruction.name))
      return new ReadEnvStrInstruction(returns);
    if (ScriptParser.tokenizer.match(ReadINIStrInstruction.name))
      return new ReadINIStrInstruction(returns);
    if (ScriptParser.tokenizer.match(ReadRegDWORDInstruction.name))
      return new ReadRegDWORDInstruction(returns);
    if (ScriptParser.tokenizer.match(ReadRegStrInstruction.name))
      return new ReadRegStrInstruction(returns);
    if (ScriptParser.tokenizer.match(RebootInstruction.name))
      return new RebootInstruction(returns);
    if (ScriptParser.tokenizer.match(RegDLLInstruction.name))
      return new RegDLLInstruction(returns);
    if (ScriptParser.tokenizer.match(RenameInstruction.name))
      return new RenameInstruction(returns);
    if (ScriptParser.tokenizer.match(RequestExecutionLevelInstruction.name))
      return new RequestExecutionLevelInstruction(returns);
    if (ScriptParser.tokenizer.match(ReserveFileInstruction.name))
      return new ReserveFileInstruction(returns);
    if (ScriptParser.tokenizer.match(ReserveFileRecursiveInstruction.name))
      return new ReserveFileRecursiveInstruction(returns);
    if (ScriptParser.tokenizer.match(RMDirInstruction.name))
      return new RMDirInstruction(returns);
    if (ScriptParser.tokenizer.match(RMDirRecursiveInstruction.name))
      return new RMDirRecursiveInstruction(returns);
    if (ScriptParser.tokenizer.match(SearchPathInstruction.name))
      return new SearchPathInstruction(returns);
    if (ScriptParser.tokenizer.match(SectionGetFlagsInstruction.name))
      return new SectionGetFlagsInstruction(returns);
    if (ScriptParser.tokenizer.match(SectionGetInstTypesInstruction.name))
      return new SectionGetInstTypesInstruction(returns);
    if (ScriptParser.tokenizer.match(SectionGetSizeInstruction.name))
      return new SectionGetSizeInstruction(returns);
    if (ScriptParser.tokenizer.match(SectionGetTextInstruction.name))
      return new SectionGetTextInstruction(returns);
    if (ScriptParser.tokenizer.match(SectionSetFlagsInstruction.name))
      return new SectionSetFlagsInstruction(returns);
    if (ScriptParser.tokenizer.match(SectionSetInstTypesInstruction.name))
      return new SectionSetInstTypesInstruction(returns);
    if (ScriptParser.tokenizer.match(SectionSetSizeInstruction.name))
      return new SectionSetSizeInstruction(returns);
    if (ScriptParser.tokenizer.match(SectionSetTextInstruction.name))
      return new SectionSetTextInstruction(returns);
    if (ScriptParser.tokenizer.match(SectionInInstruction.name))
      return new SectionInInstruction(returns);
    if (ScriptParser.tokenizer.match(SendMessageInstruction.name))
      return new SendMessageInstruction(returns);
    if (ScriptParser.tokenizer.match(SetAutoCloseInstruction.name))
      return new SetAutoCloseInstruction(returns);
    if (ScriptParser.tokenizer.match(SetBrandingImageInstruction.name))
      return new SetBrandingImageInstruction(returns);
    if (ScriptParser.tokenizer.match(SetCompressInstruction.name))
      return new SetCompressInstruction(returns);
    if (ScriptParser.tokenizer.match(SetCompressorDictSizeInstruction.name))
      return new SetCompressorDictSizeInstruction(returns);
    if (ScriptParser.tokenizer.match(SetCompressorInstruction.name))
      return new SetCompressorInstruction(returns);
    if (ScriptParser.tokenizer.match(SetCtlColorsInstruction.name))
      return new SetCtlColorsInstruction(returns);
    if (ScriptParser.tokenizer.match(SetCurInstTypeInstruction.name))
      return new SetCurInstTypeInstruction(returns);
    if (ScriptParser.tokenizer.match(SetDatablockOptimizeInstruction.name))
      return new SetDatablockOptimizeInstruction(returns);
    if (ScriptParser.tokenizer.match(SetDateSaveInstruction.name))
      return new SetDateSaveInstruction(returns);
    if (ScriptParser.tokenizer.match(SetDetailsPrintInstruction.name))
      return new SetDetailsPrintInstruction(returns);
    if (ScriptParser.tokenizer.match(SetDetailsViewInstruction.name))
      return new SetDetailsViewInstruction(returns);
    if (ScriptParser.tokenizer.match(SetErrorLevelInstruction.name))
      return new SetErrorLevelInstruction(returns);
    if (ScriptParser.tokenizer.match(SetErrorsInstruction.name))
      return new SetErrorsInstruction(returns);
    if (ScriptParser.tokenizer.match(SetFileAttributesInstruction.name))
      return new SetFileAttributesInstruction(returns);
    if (ScriptParser.tokenizer.match(SetFontInstruction.name))
      return new SetFontInstruction(returns);
    if (ScriptParser.tokenizer.match(SetOverwriteInstruction.name))
      return new SetOverwriteInstruction(returns);
    if (ScriptParser.tokenizer.match(SetOutPathInstruction.name))
      return new SetOutPathInstruction(returns);
    if (ScriptParser.tokenizer.match(SetRebootFlagInstruction.name))
      return new SetRebootFlagInstruction(returns);
    if (ScriptParser.tokenizer.match(SetRegViewInstruction.name))
      return new SetRegViewInstruction(returns);
    if (ScriptParser.tokenizer.match(SetShellVarContextInstruction.name))
      return new SetShellVarContextInstruction(returns);
    if (ScriptParser.tokenizer.match(SetSilentInstruction.name))
      return new SetSilentInstruction(returns);
    if (ScriptParser.tokenizer.match(ShowInstDetailsInstruction.name))
      return new ShowInstDetailsInstruction(returns);
    if (ScriptParser.tokenizer.match(ShowUninstDetailsInstruction.name))
      return new ShowUninstDetailsInstruction(returns);
    if (ScriptParser.tokenizer.match(ShowWindowInstruction.name))
      return new ShowWindowInstruction(returns);
    if (ScriptParser.tokenizer.match(SilentInstallInstruction.name))
      return new SilentInstallInstruction(returns);
    if (ScriptParser.tokenizer.match(SilentUninstallInstruction.name))
      return new SilentUninstallInstruction(returns);
    if (ScriptParser.tokenizer.match(SpaceTextsInstruction.name))
      return new SpaceTextsInstruction(returns);
    if (ScriptParser.tokenizer.match(StrCpyInstruction.name))
      return new StrCpyInstruction(returns);
    if (ScriptParser.tokenizer.match(StrLenInstruction.name))
      return new StrLenInstruction(returns);
    if (ScriptParser.tokenizer.match(SubCaptionInstruction.name))
      return new SubCaptionInstruction(returns);
    if (ScriptParser.tokenizer.match(TargetInstruction.name))
      return new TargetInstruction(returns);
    if (ScriptParser.tokenizer.match(UnicodeInstruction.name))
      return new UnicodeInstruction(returns);
    if (ScriptParser.tokenizer.match(UninstallButtonTextInstruction.name))
      return new UninstallIconInstruction(returns);
    if (ScriptParser.tokenizer.match(UninstallCaptionInstruction.name))
      return new UninstallCaptionInstruction(returns);
    if (ScriptParser.tokenizer.match(UninstallIconInstruction.name))
      return new UninstallButtonTextInstruction(returns);
    if (ScriptParser.tokenizer.match(UninstallSubCaptionInstruction.name))
      return new UninstallSubCaptionInstruction(returns);
    if (ScriptParser.tokenizer.match(UninstallTextInstruction.name))
      return new UninstallTextInstruction(returns);
    if (ScriptParser.tokenizer.match(UnRegDLLInstruction.name))
      return new UnRegDLLInstruction(returns);
    if (ScriptParser.tokenizer.match(VIAddVersionKeyInstruction.name))
      return new VIAddVersionKeyInstruction(returns);
    if (ScriptParser.tokenizer.match(VIProductVersionInstruction.name))
      return new VIProductVersionInstruction(returns);
    if (ScriptParser.tokenizer.match(WindowIconInstruction.name))
      return new WindowIconInstruction(returns);
    if (ScriptParser.tokenizer.match(WriteINIStrInstruction.name))
      return new WriteINIStrInstruction(returns);
    if (ScriptParser.tokenizer.match(WriteRegBinInstruction.name))
      return new WriteRegBinInstruction(returns);
    if (ScriptParser.tokenizer.match(WriteRegDWORDInstruction.name))
      return new WriteRegDWORDInstruction(returns);
    if (ScriptParser.tokenizer.match(WriteRegExpandStrInstruction.name))
      return new WriteRegExpandStrInstruction(returns);
    if (ScriptParser.tokenizer.match(WriteRegMultiStrInstruction.name))
      return new WriteRegMultiStrInstruction(returns);
    if (ScriptParser.tokenizer.match(WriteRegNoneInstruction.name))
      return new WriteRegNoneInstruction(returns);
    if (ScriptParser.tokenizer.match(WriteRegStrInstruction.name))
      return new WriteRegStrInstruction(returns);
    if (ScriptParser.tokenizer.match(WriteUninstallerInstruction.name))
      return new WriteUninstallerInstruction(returns);
    if (ScriptParser.tokenizer.match(XPStyleInstruction.name))
      return new XPStyleInstruction(returns);
    return null;
  }

  /**
   * Matches the next statement.
   * @return the next statement
   */
  public static Statement match()
  {
    Statement statement = matchInternal();

    // Matched assignment statements in the global scope need to be added to the
    // global statements list. Any function or plug-in calls in the global scope
    // (that aren't in an assignment statement) need to throw an error.
    if (statement != null && (statement instanceof AssignmentStatement || statement instanceof FunctionCallStatement) && !FunctionInfo.in() && !SectionInfo.in())
    {
      if (statement instanceof FunctionCallStatement)
      {
        if (((FunctionCallStatement)statement).getExpression() instanceof FunctionCallExpression)
          throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), "function call");
        if (((FunctionCallStatement)statement).getExpression() instanceof PluginCallExpression)
          throw new NslContextException(EnumSet.of(NslContext.Section, NslContext.Function), "plug-in call");
      }

      // Add the statement to the global statements list and then match another
      // statement. If we are assigning "" to a variable then there is no need
      // to assemble anything. Registers are always initialized to "" in NSIS.
      if (!(statement instanceof AssignmentStatement) || !ExpressionType.isString(((AssignmentStatement)statement).getExpression()) || !((AssignmentStatement)statement).getExpression().getStringValue().isEmpty())
        addGlobal(statement);
      return match();
    }

    return statement;
  }

  /**
   * Matches the next statement.
   * @return the next statement
   */
  private static Statement matchInternal()
  {
    if (ScriptParser.tokenizer.tokenIsWord())
    {
      // Exit if we're in an #if pre-processor directive and tokenizer hits
      // #else, #elseif or #endif.
      if (IfDirective.in())
      {
        if (ScriptParser.tokenizer.tokenIs("#else") || ScriptParser.tokenizer.tokenIs("#elseif") || ScriptParser.tokenizer.tokenIs("#endif"))
          return null;
      }

      // Assignment to a register. We could let Expression.matchConstant(0) do
      // this further down but that's a lot more unecessary string comparisons.
      // We know that this is a register by checking the first character and
      // therefore we must be assigning to it.
      if (ScriptParser.tokenizer.sval.startsWith("$"))
        return new AssignmentStatement();

      // Match a pre-processor directive.
      if (ScriptParser.tokenizer.match("#define"))
        return new DefineDirective();
      if (ScriptParser.tokenizer.match("#redefine"))
        return new RedefineDirective();
      if (ScriptParser.tokenizer.match("#if"))
        return new IfDirective();
      if (ScriptParser.tokenizer.match("#macro"))
        return new MacroDirective();
      if (ScriptParser.tokenizer.match("#include"))
        return new IncludeDirective();
      if (ScriptParser.tokenizer.match("#undef"))
        return new UndefDirective();
      if (ScriptParser.tokenizer.match("#error"))
        return new ErrorDirective();

      // Important that we don't use match() here otherwise it eats into the
      // NSIS code.
      if (ScriptParser.tokenizer.tokenIs("#nsis"))
        return new NSISDirective();

      // #return directive is only valid in #macros.
      if (ScriptParser.tokenizer.match("#return"))
      {
        MacroDirective.matchReturnDirective();
        return match();
      }

      // Prefixed with the uninstall key word.
      if (ScriptParser.tokenizer.match("uninstall"))
      {
        Statement statement = null;
        Scope.setInUninstaller(true);

        if (ScriptParser.tokenizer.match('{'))
        {
          statement = StatementList.match();
          ScriptParser.tokenizer.matchOrDie('}');
        }
        else if(ScriptParser.tokenizer.match("function"))
          statement = new FunctionStatement();
        else if (ScriptParser.tokenizer.match("section"))
          statement = new SectionStatement();
        else if (ScriptParser.tokenizer.match("sectiongroup"))
          statement = new SectionGroupStatement();
        else if (ScriptParser.tokenizer.match("page"))
          statement = new PageStatement();

        if (statement == null)
          throw new NslExpectedException("\"function\", \"section\", \"sectiongroup\" or \"page\"");

        Scope.setInUninstaller(false);
        return statement;
      }

      if (ScriptParser.tokenizer.match("function"))
        return new FunctionStatement();
      if (ScriptParser.tokenizer.match("section"))
        return new SectionStatement();
      if (ScriptParser.tokenizer.match("sectiongroup"))
        return new SectionGroupStatement();
      if (ScriptParser.tokenizer.match("page"))
        return new PageStatement();

      if (ScriptParser.tokenizer.match("if"))
        return new IfStatement();
      if (ScriptParser.tokenizer.match("while"))
        return new WhileStatement();
      if (ScriptParser.tokenizer.match("do"))
        return new DoStatement();
      if (ScriptParser.tokenizer.match("for"))
        return new ForStatement();
      if (ScriptParser.tokenizer.match("return"))
        return new ReturnStatement();
      if (ScriptParser.tokenizer.match("break"))
        return new BreakStatement();
      if (ScriptParser.tokenizer.match("continue"))
        return new ContinueStatement();
      if (ScriptParser.tokenizer.match("switch"))
        return new SwitchStatement();

      // This will always be matched in an IfStatement, unless it's not in one!
      if (ScriptParser.tokenizer.match("else"))
        throw new NslException("\"else\" without matching \"if\" statement", true);

      // Match a constant. This can be an NSIS instruction, defined constant (to
      // evaluate the value of), function or plug-in call or a macro insertion.
      Expression expression = Expression.matchConstant(0);

      // If null then the contents of a defined constant needs parsing.
      if (expression == null)
        return match();

      // If it can't be assembled, then it's no good for us!
      if (!(expression instanceof AssembleExpression))
        throw new NslException("\"" + expression.toString(true) + "\" is not a valid statement", true);

      // Even macro calls need a ; on the end.
      ScriptParser.tokenizer.matchEolOrDie();

      // Wrap the assignment expression within an assignment statement.
      if (expression instanceof AssignmentExpression)
        return new AssignmentStatement(expression);

      // Wrap the function or plug-in call inside a function call statement.
      if (expression instanceof FunctionCallExpression || expression instanceof PluginCallExpression)
        return new FunctionCallStatement(expression);

      return new StatementExpression((AssembleExpression)expression);
    }

    if (ScriptParser.tokenizer.tokenIsChar())
    {
      // EOF found.
      if (ScriptParser.tokenizer.ttype == Tokenizer.TT_EOF)
        return null;

      // Function call with multiple return values.
      if (ScriptParser.tokenizer.tokenIs('('))
        return new FunctionCallStatement();

      // New block of code.
      if (ScriptParser.tokenizer.tokenIs('{'))
        return new BlockStatement();

      // Skip ;
      if (ScriptParser.tokenizer.match(';'))
        return match();

      // Odd character?
      if (!ScriptParser.tokenizer.tokenIs('}'))
        throw new NslExpectedException("a statement");
    }

    return null;
  }

  /**
   * Assembles the source code.
   */
  public abstract void assemble() throws IOException;
}
