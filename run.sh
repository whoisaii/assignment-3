#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p build
find src tests -name '*.java' | sort > build/sources.txt
javac --release 17 -Xlint:all -d build @build/sources.txt
case "${1:-demo}" in
  demo) java -cp build app.Main ;;
  test) java -cp build TestSuite ;;
  benchmark) java -cp build app.Benchmark ;;
  *) echo "Usage: sh run.sh [demo|test|benchmark]" >&2; exit 2 ;;
esac
