# ktreport

A tool for organizing and displaying Kotlin test results.

# Motivation

Java/Kotlin test output is verbose, which can overwhelm the context when AI Agents run tests. 
Therefore, there's a need to organize and provide only the necessary information.

# Usage

1. Place KtreportTestListener.kt in your kotlin test directory
   - Modify the package in the code to match your desired location
2. Register the TestListener in kotlin resources
   - Specify the location from step 1
3. Build ktreport with Go and place it in the repository root
4. `./gradlew :test || ./ktreport` (or `./gradlew :test &> /dev/null || ./ktreport`)

# How it works

## TestListener

Records test results to `build/test-results/ktreport.json`.

## ktreport

Reads `build/test-results/ktreport.json` from the current directory and outputs a summary via standard output.
