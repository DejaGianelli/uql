#!/bin/bash

shopt -s globstar

# Compiles production code
javac -d out/prd src/app/**/*.java

# Compiles test code
javac -cp "lib/junit-platform-console-standalone-6.0.1.jar;out/prd" \
-d out/test src/test/**/*.java

# Runs JUnit
java -cp "lib/junit-platform-console-standalone-6.0.1.jar;out/prd;out/test" \
org.junit.platform.console.ConsoleLauncher execute --scan-class-path