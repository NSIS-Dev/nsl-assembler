#!/usr/bin/env bash
#
# Runs the end-to-end corpus: assemble each script, compile the result with
# makensis, and check that the runtime guard survived into the output.
#
# Usage: e2e/run.sh [filter]
#
# The filter is a substring matched against the script name, for iterating on
# one area: e2e/run.sh switch
#
# Environment:
#   E2E_REQUIRE_MAKENSIS=1   fail instead of skipping when makensis is missing
#   E2E_KEEP=1               keep the run directories for inspection

set -uo pipefail

repo=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)
corpus=$repo/e2e
runs=$repo/build/e2e-run
jar=$repo/build/libs/nsL.jar
filter=${1:-}

if ! command -v makensis >/dev/null 2>&1; then
  # Skipped rather than passed. The corpus is only meaningful with the real
  # compiler behind it, and on Windows the assembler shells out to makensisw
  # instead, which is a GUI and is never waited on.
  echo "SKIP: makensis is not on the PATH; the corpus needs it as its oracle."
  echo "      Install NSIS, or set E2E_REQUIRE_MAKENSIS=1 to make this a failure."
  [[ ${E2E_REQUIRE_MAKENSIS:-0} == 1 ]] && exit 1
  exit 0
fi

"$repo/gradlew" --project-dir "$repo" jar -q || exit 1
[[ -f $jar ]] || { echo "FAIL: $jar was not built."; exit 1; }

rm -rf "$runs"
mkdir -p "$runs"

# The guard is written in the language under test, so a codegen regression could
# drop it and leave a live installer behind. All four pieces have to survive:
# checking only the callbacks would miss a Call that named the wrong namespace.
check_guard() {
  awk '
    # The assembler writes CRLF line endings, so strip the CR before matching.
    { sub(/\r$/, "") }
    /^Function / { fn = $2; next }
    /^FunctionEnd$/ { fn = ""; next }
    fn == "RuntimeGuard"    && $0 == "Abort"                { a = 1 }
    fn == "un.RuntimeGuard" && $0 == "Abort"                { b = 1 }
    fn == ".onInit"         && $0 == "Call RuntimeGuard"    { c = 1 }
    fn == "un.onInit"       && $0 == "Call un.RuntimeGuard" { d = 1 }
    END {
      if (!a) print "    missing: Abort inside Function RuntimeGuard"
      if (!b) print "    missing: Abort inside Function un.RuntimeGuard"
      if (!c) print "    missing: Call RuntimeGuard inside Function .onInit"
      if (!d) print "    missing: Call un.RuntimeGuard inside Function un.onInit"
      exit (a && b && c && d) ? 0 : 1
    }
  ' "$1"
}

ran=0
failed=0

for source in "$corpus"/*.nsl; do
  name=$(basename "$source" .nsl)
  [[ $name == guard ]] && continue
  [[ -n $filter && $name != *"$filter"* ]] && continue

  dir=$runs/$name
  mkdir -p "$dir"
  cp -R "$corpus"/. "$dir"/

  # Not /nomake: running the compiler is the entire point. The working directory
  # has to be the copy, because #include resolves relative to the process rather
  # than to the including file, and because both the .nsi and the .exe are
  # written next to the source.
  output=$(cd "$dir" && java -jar "$jar" "$name.nsl" /nopause 2>&1)
  status=$?
  ran=$((ran + 1))

  if [[ $status -ne 0 ]]; then
    failed=$((failed + 1))
    case $status in
      3) echo "FAIL $name: makensis rejected the assembled script" ;;
      *) echo "FAIL $name: the assembler exited $status" ;;
    esac
    # Both halves, always: the compiler error means nothing without the line it
    # is complaining about.
    echo "$output" | sed 's/^/    /'
    if [[ -f $dir/$name.nsi ]]; then
      echo "  --- $name.nsi ---"
      sed 's/^/    /' "$dir/$name.nsi"
    fi
    continue
  fi

  if ! guard_errors=$(check_guard "$dir/$name.nsi"); then
    failed=$((failed + 1))
    echo "FAIL $name: the runtime guard did not survive assembly"
    echo "$guard_errors"
    continue
  fi

  # Warnings do not fail the run, but they are not swallowed either.
  if warnings=$(echo "$output" | grep -E '^[0-9]+ warnings?:$' -A100); then
    echo "WARN $name"
    echo "$warnings" | sed 's/^/    /'
  else
    echo "ok   $name"
  fi

  [[ ${E2E_KEEP:-0} == 1 ]] || rm -rf "$dir"
done

echo
if [[ $ran -eq 0 ]]; then
  echo "No scripts matched${filter:+ \"$filter\"}."
  exit 1
fi

echo "$ran script(s), $failed failed."
[[ ${E2E_KEEP:-0} == 1 ]] && echo "Run directories kept under $runs"
exit $((failed > 0))
