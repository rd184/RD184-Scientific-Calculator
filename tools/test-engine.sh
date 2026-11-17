#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="${TMPDIR:-/tmp}/rd184-engine-checks.jar"
kotlinc "$ROOT/app/src/main/java/com/rd184/calculator/CalculatorEngine.kt" "$ROOT/tools/EngineChecks.kt" -include-runtime -d "$OUT"
java -jar "$OUT"
