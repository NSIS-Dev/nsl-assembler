# End-to-end corpus

nsL scripts that exist to be assembled and then compiled. The assertion is
narrow and cheap: **every script here assembles cleanly and `makensis` accepts
the result.** Nothing is run, and nothing is diffed against a stored `.nsi`.

`makensis` is the oracle. A textual diff only proves the output changed the way
the author expected; it says nothing about whether the output is valid NSIS.
This corpus catches operands in the wrong order, off-by-one arity, a directive
emitted where NSIS rejects it, and an instruction that does not exist in the
installed NSIS version. See [E2E_TESTING_PLAN.md](../E2E_TESTING_PLAN.md) for
the design, and [KNOWN-GAPS.md](KNOWN-GAPS.md) for what the corpus deliberately
does not cover and why.

## Running it

```bash
mise run test:e2e             # the whole corpus
mise run test:e2e switch      # just the scripts whose name contains "switch"
```

`mise run checks` includes it, alongside the formatter and the unit tests.

[run.sh](run.sh) does the work and can be called directly. Per script it copies
`e2e/` into `build/e2e-run/<name>/`, assembles and compiles there, checks the
guard survived, and deletes the directory. On failure it prints the compiler
output *and* the generated `.nsi`, because the error line means nothing without
the line it is complaining about.

| Environment | Effect |
| --- | --- |
| `E2E_REQUIRE_MAKENSIS=1` | Fail instead of skipping when `makensis` is missing. For CI |
| `E2E_KEEP=1` | Keep the run directories under `build/e2e-run/` for inspection |

Without `makensis` on the `PATH` the task skips with a message rather than
passing quietly - the corpus is only meaningful with the real compiler behind
it. Warnings are reported but do not fail the run.

### Running one by hand

The assembler resolves `#include` relative to its own working directory, not to
the including file - see
[IncludeDirective.java:30](../src/nsl/preprocessor/IncludeDirective.java#L30).
So a script has to be run from a directory that has `guard.nsl` and `fixtures/`
next to it, and both the `.nsi` and the `.exe` land beside the source:

```bash
./gradlew jar
cp -r e2e /tmp/e2e-run
cd /tmp/e2e-run
java -jar $OLDPWD/build/libs/nsL.jar 01-expressions.nsl /nopause
```

Not `/nomake` - running the compiler is the entire point. Exit 3 means
`makensis` rejected the output; anything else non-zero is an assembler-level
failure.

## Runtime safety

The corpus writes to the registry, the filesystem and the shell, and each script
builds a real, runnable installer. They are inert, in three independent layers,
because any one of them could be defeated by the very codegen bug the suite
exists to catch.

**1. [guard.nsl](guard.nsl), included first by every script.** It defines
`RuntimeGuard()` once per context - installer and uninstaller are separate NSIS
namespaces - and calls it from both `.onInit` callbacks. The guard shows an
`MB_OK` reading *"This shall never run, exiting."* and then aborts.

The message box is the point. An inert installer that dies silently looks like a
broken installer; this one says why it did nothing, so anyone who double clicks
a stray corpus binary knows it is a test artifact.

`Abort` propagates out of the `Call` - verified by running a probe installer
under Wine, not assumed. Re-run that probe if the shape of the guard changes; it
is the one part of the design resting on NSIS runtime semantics rather than on
emitted syntax.

**2. `RequestExecutionLevel("user")`,** also in `guard.nsl`. A corpus binary
cannot touch `HKLM` or `Program Files` even if layer 1 failed. Every registry
write in the corpus targets `HKCU`, and every file write targets `$INSTDIR` or
`$PLUGINSDIR`.

**3. The binary does not outlive the check.** `OutFile` is a bare filename, so
the `.exe` is written wherever the script was run from - a scratch copy, never
the repo.

### Guard self-check

The guard is written in the language under test, so a codegen regression could
drop it. [run.sh](run.sh) therefore asserts on each generated `.nsi` that all
four pieces survived:

```
Function RuntimeGuard      ... containing Abort
Function un.RuntimeGuard   ... containing Abort
Function .onInit           ... containing Call RuntimeGuard
Function un.onInit         ... containing Call un.RuntimeGuard
```

Checking only the callbacks is not enough now that the `Abort` lives one frame
away: a bug that emitted `Call RuntimeGuard` from `un.onInit` would pass a naive
check and produce an uninstaller that runs. That bug was real - the assembler
resolved every call from uninstaller code to the installer function - and is
what [06-functions.nsl](06-functions.nsl) now pins down.

## Layout

| File | Covers |
| --- | --- |
| [guard.nsl](guard.nsl) | The runtime guard and `RequestExecutionLevel`; included by every script below |
| [fixtures/](fixtures/) | Small real files for `File()`, `LicenseData()`, `ReadINIStr()` and `#include` |
| [01-expressions.nsl](01-expressions.nsl) | Literals, NSIS constants, named variables, register-pool pressure |
| [02-operators.nsl](02-operators.nsl) | Every operator, twice: folded at assemble time and emitted as `IntOp` |
| [03-strings.nsl](03-strings.nsl) | The three quote characters, escapes, `@` verbatim strings, `format()` |
| [04-control-flow.nsl](04-control-flow.nsl) | `if`/`while`/`do`/`for`, `break`, `continue`, folded and unreachable branches |
| [05-switch.nsl](05-switch.nsl) | `switch` over integer, string, boolean and expression subjects; fallthrough |
| [06-functions.nsl](06-functions.nsl) | Parameters, multiple returns, overloading, recursion, the `un.` namespace split |
| [07-sections.nsl](07-sections.nsl) | Every section header argument, `SectionIn`, `AddSize`, uninstall sections |
| [08-pages.nsl](08-pages.nsl) | Both page forms, every callback position, uninstaller pages |
| [09-globals-and-scope.nsl](09-globals-and-scope.nsl) | Global initialisers threaded into `.onInit`, block and loop scope |
| [10-defines.nsl](10-defines.nsl) | `#define`, `#redefine`, `#undef`, `defined()`, definition- vs substitution-time evaluation |
| [11-conditionals.nsl](11-conditionals.nsl) | `#if`/`#elseif`/`#else`, nested, at global and statement scope |
| [12-macros.nsl](12-macros.nsl) | Parameters, returns, overloads, recursion as an assemble-time loop, `Returns` |
| [13-include.nsl](13-include.nsl) | Nested `#include`, and what crosses the file boundary |
| [14-inline-nsis.nsl](14-inline-nsis.nsl) | `#nsis` blocks, at global scope, in a section, and inside a macro |
| [15-assembler-functions.nsl](15-assembler-functions.nsl) | `toint`, `type`, `length`, `defined`, `format`, `nsisconst`, `eval`, `returnvar` |
| [16-attributes.nsl](16-attributes.nsl) | Global installer attributes: compression, text, colours, install types |
| [17-inst-void.nsl](17-inst-void.nsl) | Instructions that take arguments and return nothing |
| [18-inst-returns.nsl](18-inst-returns.nsl) | Instructions that produce one value, and several |
| [19-inst-boolean.nsl](19-inst-boolean.nsl) | Branch instructions as conditions, as values, and in compound expressions |
| [20-inst-switches.nsl](20-inst-switches.nsl) | The Boolean-argument-for-`/FLAG` convention, each instruction with and without |
| [21-inst-registry.nsl](21-inst-registry.nsl) | Registry writes, reads, enumeration, views, root keys; INI files |
| [22-inst-files.nsl](22-inst-files.nsl) | Embedding, the file handle API, directory search, attributes and times |
| [23-inst-ui.nsl](23-inst-ui.nsl) | Message boxes, windows, controls, section text, running processes |
| [24-inst-misc.nsl](24-inst-misc.nsl) | Everything left over: target, manifest, licence text, DLL registration |

One file per feature area, numbered for stable ordering, each self-contained and
compiling alone - so when a run fails, the failing filename names the feature.
Resist writing one large script; a single `makensis` error line then tells you
nothing.

## Instruction coverage

174 of the 189 instructions dispatched by `Statement.matchInstruction()` appear
somewhere in the corpus. The 15 that do not are all listed in
[KNOWN-GAPS.md](KNOWN-GAPS.md), either as assembler defects or as environment
limits.

To recheck after adding an instruction wrapper:

```bash
tr '\n' ' ' < src/nsl/statement/Statement.java \
  | grep -o '[A-Za-z0-9]*Instruction\.name' | sed 's/\.name//' | sort -u \
  | while read c; do
      grep -h 'static final String name' "src/nsl/instruction/$c.java" \
        | sed 's/.*= "//;s/".*//'
    done | sort -u > /tmp/names.txt

cat e2e/*.nsl e2e/fixtures/*.nsl \
  | grep -o '[A-Za-z][A-Za-z0-9]*(' | sed 's/(//' | sort -u > /tmp/used.txt

comm -23 /tmp/names.txt /tmp/used.txt
```

That is a name match, not a parse, so it is a prompt rather than proof - but it
is enough to notice a new wrapper that nothing exercises.
