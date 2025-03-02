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

# Output Example

```
$ ./gradlew :test &> /dev/null || ./ktreport
= ktreport ===============================================================================================

com.example.CalculatorBehaviorSpecTest .. (0.006s)
com.example.CalculatorDescribeSpecTest F.. (0.010s)
com.example.CalculatorFreeSpecTest ....... (0.000s)
com.example.CalculatorFunSpecTest .... (0.000s)
com.example.CalculatorShouldSpecTest ...... (0.000s)
com.example.CalculatorWordSpecTest ..... (0.000s)

= failures ===============================================================================================

com.example.CalculatorDescribeSpecTest/Multiplication tests/Deliberately failing test (0.010s)

io.kotest.assertions.AssertionFailedError: expected:<7> but was:<6>
        at com.example.CalculatorDescribeSpecTest$1$1$3.invokeSuspend(CalculatorTest.kt:96)
        at com.example.CalculatorDescribeSpecTest$1$1$3.invoke(CalculatorTest.kt)
        at com.example.CalculatorDescribeSpecTest$1$1$3.invoke(CalculatorTest.kt)
        at io.kotest.core.spec.style.scopes.DescribeSpecContainerScope$it$3.invokeSuspend(DescribeSpecContainerScope.kt:112)
        at io.kotest.core.spec.style.scopes.DescribeSpecContainerScope$it$3.invoke(DescribeSpecContainerScope.kt)
        at io.kotest.core.spec.style.scopes.DescribeSpecContainerScope$it$3.invoke(DescribeSpecContainerScope.kt)
        at io.kotest.engine.test.TestCaseExecutor$execute$innerExecute$1.invokeSuspend(TestCaseExecutor.kt:91)
... (269 lines omitted)

= short test summary info =================================================================================
FAILED 26 passed, 1 failed in 0.273s
===========================================================================================================
```
