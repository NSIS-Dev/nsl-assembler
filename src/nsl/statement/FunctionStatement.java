/*
 * FunctionStatement.java
 */

package nsl.statement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;

/**
 * Describes an nsL function. This includes its name, parameters, return values and its used
 * registers.
 *
 * @author Stuart
 */
public class FunctionStatement extends Statement {
	private final FunctionInfo current;
	private final ArrayList<Register> params;
	private final BlockStatement blockStatement;

	/** Class constructor. */
	public FunctionStatement() {
		// We can't have a function within a function or section.
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), "function");

		// Function name.
		String name;
		if (ScriptParser.tokenizer.match('.')) {
			name = '.' + ScriptParser.tokenizer.matchAWord("a function name");
			if (Scope.inUninstaller()) name = "un" + name;
		} else {
			name = ScriptParser.tokenizer.matchAWord("a function name");
			if (Scope.inUninstaller()) name = "un." + name;
		}

		// Function call parameters.
		this.params = new ArrayList<Register>();
		ArrayList<Expression> paramsList = Expression.matchRegisterList();
		for (Expression ret : paramsList) {
			if (!ExpressionType.isRegister(ret))
				throw new NslException("Parameters in a function definition must be variables", true);
			Scope.getCurrent().addVar(ret.getIntegerValue());
			this.params.add(RegisterList.getCurrent().get(ret.getIntegerValue()));
		}

		this.current = FunctionInfo.create(name, this.params);
		FunctionInfo.setCurrent(this.current);
		this.blockStatement = new BlockStatement();
		FunctionInfo.setCurrent(null);
		RegisterList.getCurrent().setAllInUse(false);

		// If the last statement is a return statement, tell it.
		Statement last = this.blockStatement.getLast();
		if (last instanceof ReturnStatement) ((ReturnStatement) last).setLast(true);

		if (this.current.isCallback()) {
			if (name.equalsIgnoreCase(".onMouseOverSection")) {
				if (this.params.size() != 1)
					throw new NslException(
							"Callback function \"" + name + "\" requires 1 parameter, $0", true);

				Register var = RegisterList.getCurrent().get(this.params.get(0).getIntegerValue());
				if (var == null || !var.toString().equals("$0"))
					throw new NslException(
							"Callback function \"" + name + "\" requires 1 parameter, $0", true);
			} else {
				if (!this.params.isEmpty())
					throw new NslException("Callback function \"" + name + "\" requires 0 parameters", true);
			}

			if (this.current.getReturns() > 0)
				throw new NslException("Callback function \"" + name + "\" requires 0 return values", true);
		}
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		ScriptParser.writeLine("Function " + this.current.getName());
		FunctionInfo.setCurrent(this.current);

		// Assemble global assignments if we're in .onInit.
		if (this.current.getName().equalsIgnoreCase(".onInit")) {
			for (Statement statement : Statement.globalAssignmentStatements) statement.assemble();
		}

		// Assemble global uninstaller assignments if we're in un.onInit.
		if (this.current.getName().equalsIgnoreCase("un.onInit")) {
			for (Statement statement : Statement.globalUninstallerAssignmentStatements)
				statement.assemble();
		}

		for (Expression param : this.params) ScriptParser.writeLine("Pop " + param);

		this.blockStatement.assemble();

		FunctionInfo.setCurrent(null);
		LabelList.getCurrent().reset();
		ScriptParser.writeLine("FunctionEnd");
	}
}
