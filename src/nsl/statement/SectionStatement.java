/*
 * SectionStatement.java
 */

package nsl.statement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import nsl.*;
import nsl.expression.*;

/**
 * Describes an nsL section. This includes its name and type.
 *
 * @author Stuart
 */
public class SectionStatement extends Statement {
	private final boolean uninstall;
	private final SectionInfo current;
	private final String name;
	private final Expression description;
	private final Expression readOnly;
	private final Expression optional;
	private final Expression bold;
	private final ArrayList<Expression> sectionInList;
	private final BlockStatement blockStatement;

	/** Class constructor. */
	public SectionStatement() {
		// We can't have a section within a function or section.
		if (!ScriptParser.inGlobalContext())
			throw new NslContextException(EnumSet.of(NslContext.Global), "section");

		// Section name.
		this.name = ScriptParser.tokenizer.matchAWord("a section name");

		// Section arguments.
		ArrayList<Expression> paramsList = Expression.matchList();
		int paramsCount = paramsList.size();

		this.sectionInList = new ArrayList<Expression>();
		int sectionInListStarts = 4;

		// Section description.
		if (paramsCount > 0) {
			this.description = paramsList.get(0);
			if (!ExpressionType.isString(this.description))
				throw new NslArgumentException(name, 1, ExpressionType.String);

			// Read only?
			if (paramsCount > 1) {
				Expression readOnlyOrSectionIn = paramsList.get(1);

				if (ExpressionType.isBoolean(readOnlyOrSectionIn)) {
					this.readOnly = readOnlyOrSectionIn;

					// Optional?
					if (paramsCount > 2) {
						Expression optionalOrSectionIn = paramsList.get(2);

						if (ExpressionType.isBoolean(optionalOrSectionIn)) {
							this.optional = optionalOrSectionIn;

							// Bold?
							if (paramsCount > 3) {
								Expression boldOrSectionIn = paramsList.get(3);

								if (ExpressionType.isBoolean(boldOrSectionIn)) {
									this.bold = boldOrSectionIn;
								} else {
									if (!ExpressionType.isInteger(boldOrSectionIn))
										throw new NslArgumentException(name, 4, ExpressionType.Integer);

									this.sectionInList.add(boldOrSectionIn);
									sectionInListStarts = 4;

									this.bold = null;
								}
							} else {
								this.bold = null;
							}
						} else {
							if (!ExpressionType.isInteger(optionalOrSectionIn))
								throw new NslArgumentException(name, 3, ExpressionType.Integer);

							this.sectionInList.add(optionalOrSectionIn);
							sectionInListStarts = 3;

							this.optional = null;
							this.bold = null;
						}
					} else {
						this.optional = null;
						this.bold = null;
					}
				} else {
					if (!ExpressionType.isInteger(readOnlyOrSectionIn))
						throw new NslArgumentException(name, 2, ExpressionType.Integer);

					this.sectionInList.add(readOnlyOrSectionIn);
					sectionInListStarts = 2;

					this.readOnly = null;
					this.optional = null;
					this.bold = null;
				}

				for (int i = sectionInListStarts; i < paramsCount; i++) {
					Expression sectionIn = paramsList.get(i);

					if (!ExpressionType.isInteger(sectionIn))
						throw new NslArgumentException(name, i + 1, ExpressionType.Integer);

					this.sectionInList.add(sectionIn);
				}
			} else {
				this.readOnly = null;
				this.optional = null;
				this.bold = null;
			}
		} else {
			this.description = null;
			this.readOnly = null;
			this.optional = null;
			this.bold = null;
		}

		this.current = new SectionInfo();

		SectionInfo.setCurrent(this.current);
		this.blockStatement = new BlockStatement();
		SectionInfo.setCurrent(null);
		RegisterList.getCurrent().setAllInUse(false);

		this.uninstall = Scope.inUninstaller();
	}

	/** Assembles the source code. */
	@Override
	public void assemble() throws IOException {
		String prefix = "";

		if (this.uninstall) prefix += "un.";

		if (this.readOnly != null) {
			AssembleExpression.assembleIfRequired(this.readOnly);
		}

		if (this.optional != null) {
			AssembleExpression.assembleIfRequired(this.optional);
		}

		if (this.bold != null) {
			AssembleExpression.assembleIfRequired(this.bold);
			if (this.bold.getBooleanValue() == true) prefix += "!";
		}

		if (this.description == null) ScriptParser.writeLine("Section \"" + prefix + "\" " + this.name);
		else
			ScriptParser.writeLine(
					"Section "
							+ (this.optional != null && this.optional.getBooleanValue() == true ? "/o " : "")
							+ "\""
							+ prefix
							+ this.description.toString(true)
							+ "\" "
							+ this.name);
		SectionInfo.setCurrent(this.current);

		if (this.readOnly != null && this.readOnly.getBooleanValue() == true) {
			ScriptParser.writeLine("SectionIn RO");
		}

		this.blockStatement.assemble();

		SectionInfo.setCurrent(null);
		LabelList.getCurrent().reset();
		ScriptParser.writeLine("SectionEnd");
	}
}
