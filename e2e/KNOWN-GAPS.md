# Known gaps

Everything here was found by writing the corpus in [e2e/](.) and running it
through `makensis`. Each entry is a documented nsL feature that does not work,
with the smallest reproduction found and what the corpus does instead.

None of these are fixed. They are listed so that the exclusions in the corpus
are deliberate and reviewable rather than silent, and so that a fix has a test
waiting for it: removing the workaround in the named file is the regression
test.

Several are now also pinned in [test/](../test), which assembles in a
subprocess and never runs `makensis`, so it can cover things the corpus cannot
reach. Where a gap has a unit test standing on it, the entry names it: fixing
the gap turns that test red, which is the reminder to come back here.

---

## Assembler crashes

### Calling a value-returning function as a bare statement

```nsl
function F($n) { return $n; }
section S("s") { F(1); }
```

```
java.lang.NullPointerException: Cannot invoke "nsl.Register.toString()"
  at nsl.expression.FunctionCallExpression.assemble(FunctionCallExpression.java:140)
```

The discard path looks up `getUsedVars().get(0)` - register index 0, `$0` -
rather than the function's first used register, so any function that takes
parameters and returns a value cannot be called for its side effects alone.

*Corpus:* [06-functions.nsl](06-functions.nsl) assigns the result even where
nothing needs it.

---

## Wrong code emitted

### An uninstaller page names a function that does not exist

The callback name is written through verbatim, so an uninstall page declaration
has to name the emitted function rather than the declared one:

```nsl
uninstall function Pre() { }
uninstall page UninstConfirm("un.Pre");   // "Pre" fails to compile
```

*Corpus:* [08-pages.nsl](08-pages.nsl) spells the prefix out.

---

## Features that cannot be used at all

### `sectiongroup`

`SectionGroupStatement` parses its body with a `BlockStatement`, which refuses
to run in global context - so every spelling of the keyword fails:

```
"code block" can only be used in a function or section context.
```

There is no way to write a section group. *Corpus:*
[07-sections.nsl](07-sections.nsl) covers sections only.

---

## Smaller things

### `return` always needs a value

A bare `return;` is a parse error - `Expected an expression, but found ";"` -
even in a function that returns nothing.

### `StrLen()` rejects a plain register

`isLiteral()` is true for anything that is not an `AssembleExpression`, and a
register is not one, so `StrLen($R0)` is refused with "use the length()
assembler function instead". Its argument has to be a nested instruction call:

```nsl
$R3 = StrLen(ReadEnvStr("PATH"));
```

*Pinned:* `ReturningInstructionTest.StrLen` asserts the temporary.

### `length()` measures the escaped form

`length("a\tb")` is 5, not 3: it counts the string after translation into NSIS
form, where a tab is the three characters `$\t`.

### `returnvar()` has to be the whole argument

`DetailPrint(returnvar(1))` works; `DetailPrint("x".returnvar(1))` reports "Use
of returnvar() where no return registers are being used".

### `#if` cannot appear part way through a statement

It is matched where a statement is expected. The ternary operator is the in-line
equivalent and folds the same way.

### `DirVerify` and `DirText` are accepted outside a `PageEx`

Both list `NslContext.Global` as valid, but NSIS rejects them anywhere except
inside a `PageEx`. *Corpus:* [16-attributes.nsl](16-attributes.nsl) puts them in
the `page Directory()` block. *Pinned:* `AttributeInstructionTest` assembles
both at global scope - the unit tier does not run `makensis`, so it records the
acceptance the corpus has to work around - and
`UncompilableInstructionTest.DirVar` covers the page form NSIS actually wants.

### A boolean instruction as a `switch` subject leaves an unused label

`switch (FileExists($EXEDIR))` emits the branch after the case bodies, referring
back to a label ahead of it, and `makensis` warns that the label is not used.
Going through a variable avoids it. *Corpus:*
[19-inst-boolean.nsl](19-inst-boolean.nsl).

### A variable used only in unassembled code is still declared

`for ($k = 0; true && false; $k++)` declares `$k`, and `makensis` warns that it
wastes memory. `Examples/Loops.nsl` notes the same. *Corpus:*
[04-control-flow.nsl](04-control-flow.nsl) reuses a variable that is live
elsewhere.

---

## Not gaps: environment limits

These are excluded for reasons that have nothing to do with the assembler.

Every one of them except the plug-in calls is now covered by a unit test
instead: the assembler emits the line regardless of whether anything downstream
can compile it. `AttributeInstructionTest` holds the attributes and
`UncompilableInstructionTest` the four `makensis` refuses outright, and that is
the only coverage those have.

| Excluded | Why |
| --- | --- |
| `Icon`, `UninstallIcon`, `WindowIcon`, `CheckBitmap`, `AddBrandingImage` | Need real image files, laid out as NSIS expects |
| `LoadLanguageFile` | Needs an `.nlf` from the NSIS installation |
| `GetDLLVersionLocal` | Reads the file while compiling, so needs a real DLL. [18-inst-returns.nsl](18-inst-returns.nsl) covers the run-time `GetDLLVersion` |
| `LogSet`, `LogText` | Rejected outright unless NSIS was built with `NSIS_CONFIG_LOG` |
| `Int64Fmt` | "Instruction only supported by 64-bit targets!", and `Target("amd64-unicode")` fails too - this build's `Stubs/` holds x86 only. [18-inst-returns.nsl](18-inst-returns.nsl) names it where it would have gone |
| `ManifestAppendCustomString` | `makensis` 3.12 rejects every two-argument spelling, including one written by hand in a bare `.nsi` |
| `PEAddResource`, `PERemoveResource` | `makensis` 3.12 rejects every spelling of both, including the one its own usage line prints. `PESubsysVer` and `PEDllCharacteristics` compile on the same build, so this is these two commands rather than the PE family |
| Plug-in calls | Need actual plug-in DLLs present at compile time |
