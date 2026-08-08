# Known gaps

Everything here was found by writing the corpus in [e2e/](.) and running it
through `makensis`. Each entry is a documented nsL feature that does not work,
with the smallest reproduction found and what the corpus does instead.

None of these are fixed. They are listed so that the exclusions in the corpus
are deliberate and reviewable rather than silent, and so that a fix has a test
waiting for it: removing the workaround in the named file is the regression
test.

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

### `SetCtlColors` and `SetBrandingImage` cannot be called

Both reject being given a return variable and then throw
`UnsupportedOperationException` from the statement form, so there is no way to
write either of them.

*Corpus:* not used; [23-inst-ui.nsl](23-inst-ui.nsl) says why.

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

### Seven instructions are never dispatched

`Call`, `ChangeUI`, `Exch`, `GetCurrentAddress`, `GetFunctionAddress`,
`GetLabelAddress` and `Sleep` all have wrapper classes in
[../src/nsl/instruction/](../src/nsl/instruction/) that nothing references from
`Statement.matchInstruction()`. Using any of them fails with:

```
Function "GetFunctionAddress" not found that expects 1 parameters and returns 1 values.
```

This is the failure mode [CLAUDE.md](../CLAUDE.md) warns about when adding an
instruction. It means indirect calls and `Sleep` have no spelling in nsL.

*Corpus:* [06-functions.nsl](06-functions.nsl) and
[17-inst-void.nsl](17-inst-void.nsl) name them where they would have gone.

### A loop or a nested switch inside a `switch`

The first breakable construct inside a case leaves every later `break` in the
enclosing switch rejected:

```nsl
switch ($R0)
{
  case 1:
    $i = 0;
    while ($i < 2) { $i++; }
  default:
    DetailPrint("d");
    break;              // The "break" statement cannot be used here.
}
```

The assembler separately insists a switch end with a `break`, so no arrangement
of the two assembles. *Corpus:* [05-switch.nsl](05-switch.nsl) keeps case bodies
flat and says so.

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

### `toint()` cannot parse hexadecimal

Documented to accept "a string literal of a decimal or hexadecimal
representation". `toint("0xFF")` reaches `Integer.parseInt("0xFF", 16)`, which
rejects the prefix, and `toint("FF")` is parsed as decimal. Both warn and
return 0. The undocumented second parameter - a fallback value - does work.

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
the `page Directory()` block.

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

| Excluded | Why |
| --- | --- |
| `Icon`, `UninstallIcon`, `WindowIcon`, `CheckBitmap`, `AddBrandingImage` | Need real image files, laid out as NSIS expects |
| `LoadLanguageFile` | Needs an `.nlf` from the NSIS installation |
| `GetDLLVersionLocal` | Reads the file while compiling, so needs a real DLL. [18-inst-returns.nsl](18-inst-returns.nsl) covers the run-time `GetDLLVersion` |
| `LogSet`, `LogText` | Rejected outright unless NSIS was built with `NSIS_CONFIG_LOG` |
| `ManifestAppendCustomString` | `makensis` 3.12 rejects every two-argument spelling, including one written by hand in a bare `.nsi` |
| `PEAddResource`, `PERemoveResource` | `makensis` 3.12 rejects every spelling of both, including the one its own usage line prints. `PESubsysVer` and `PEDllCharacteristics` compile on the same build, so this is these two commands rather than the PE family |
| Plug-in calls | Need actual plug-in DLLs present at compile time |
