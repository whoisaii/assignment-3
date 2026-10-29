#!/bin/sh
set -eu

cd "$(dirname "$0")"

mkdir -p out
find src tests -name '*.java' -print > out/sources.txt
javac --release 17 -Xlint:all -d out @out/sources.txt

case "${1:-demo}" in
    test)
        java -cp out tests.GraphTests
        ;;
    demo)
        java -cp out Main
        ;;
    *)
        echo "Usage: sh run.sh [test|demo]"
        exit 1
        ;;
esac