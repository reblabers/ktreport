# Kotlin + JUnit5 Sample Project

This project is created as a sample for the ktreport tool using Kotlin + JUnit5.

## Project Structure

- `src/main/kotlin/com/example/Calculator.kt` - Simple calculator class
- `src/test/kotlin/com/example/CalculatorTest.kt` - Test class using JUnit5
- `src/test/kotlin/com/example/KtreportTestListener.kt` - Test listener for ktreport
- `src/test/resources/META-INF/services/org.junit.platform.launcher.TestExecutionListener` - Test listener registration

## Execution

You can run tests and display test results with ktreport using the following command:

```bash
./gradlew :test || ./ktreport
```

## Test Content

This sample includes the following tests:

- Addition tests (positive numbers, negative numbers, parameterized tests)
- Subtraction tests (positive numbers, negative numbers)
- Multiplication tests (positive numbers, negative numbers, deliberately failing tests)
- Division tests (positive numbers, exception tests for division by zero)

## How ktreport Works

1. KtreportTestListener is called during test execution and records test results to `build/test-results/ktreport.json`
2. The ktreport command reads this JSON file and displays formatted results
