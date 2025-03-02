package com.example

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.*
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe

/**
 * Calculator class tests (FunSpec)
 */
class CalculatorFunSpecTest : FunSpec({
    val calculator = Calculator()

    context("Addition tests") {
        test("Adding positive numbers") {
            calculator.add(2, 3) shouldBe 5
        }

        test("Adding negative numbers") {
            calculator.add(-2, -3) shouldBe -5
        }

        context("Parameterized addition tests") {
            table(
                headers("a", "b", "expected"),
                row(0, 0, 0),
                row(1, 1, 2),
                row(-1, -2, -3),
                row(10, -5, 5)
            ).forAll { a, b, expected ->
                calculator.add(a, b) shouldBe expected
            }
        }
    }

    context("Subtraction tests") {
        test("Subtracting positive numbers") {
            calculator.subtract(5, 3) shouldBe 2
        }

        test("Subtracting negative numbers") {
            calculator.subtract(-2, -3) shouldBe 1
        }
    }
})

/**
 * Calculator class tests (StringSpec)
 */
class CalculatorStringSpecTest : StringSpec({
    val calculator = Calculator()

    "2 + 3 should equal 5" {
        calculator.add(2, 3) shouldBe 5
    }

    "5 - 3 should equal 2" {
        calculator.subtract(5, 3) shouldBe 2
    }

    "2 * 3 should equal 6" {
        calculator.multiply(2, 3) shouldBe 6
    }

    "6 / 3 should equal 2" {
        calculator.divide(6, 3) shouldBe 2
    }

    "Dividing by zero should throw an exception" {
        val exception = shouldThrow<IllegalArgumentException> {
            calculator.divide(1, 0)
        }
        exception.message shouldBe "Cannot divide by zero"
    }
})

/**
 * Calculator class tests (DescribeSpec)
 */
class CalculatorDescribeSpecTest : DescribeSpec({
    val calculator = Calculator()

    describe("Multiplication tests") {
        it("Multiplying positive numbers") {
            calculator.multiply(2, 3) shouldBe 6
        }

        it("Multiplying negative numbers") {
            calculator.multiply(-2, -3) shouldBe 6
        }

        it("Deliberately failing test") {
            calculator.multiply(2, 3) shouldBe 7 // This test should fail
        }
    }
})

/**
 * Calculator class tests (BehaviorSpec)
 */
class CalculatorBehaviorSpecTest : BehaviorSpec({
    val calculator = Calculator()

    given("Division functionality") {
        `when`("Dividing by a positive number") {
            then("Returns the correct result") {
                calculator.divide(6, 3) shouldBe 2
            }
        }

        `when`("Dividing by zero") {
            then("Throws an exception") {
                val exception = shouldThrow<IllegalArgumentException> {
                    calculator.divide(1, 0)
                }
                exception.message shouldBe "Cannot divide by zero"
            }
        }
    }
})

/**
 * Calculator class tests (ShouldSpec)
 */
class CalculatorShouldSpecTest : ShouldSpec({
    val calculator = Calculator()

    context("Calculator") {
        should("Add two positive numbers correctly") {
            calculator.add(2, 3) shouldBe 5
        }

        should("Add two negative numbers correctly") {
            calculator.add(-2, -3) shouldBe -5
        }

        should("Subtract two numbers correctly") {
            calculator.subtract(5, 3) shouldBe 2
        }

        should("Multiply two numbers correctly") {
            calculator.multiply(2, 3) shouldBe 6
        }

        should("Divide two numbers correctly") {
            calculator.divide(6, 3) shouldBe 2
        }

        should("Throw an exception when dividing by zero") {
            val exception = shouldThrow<IllegalArgumentException> {
                calculator.divide(1, 0)
            }
            exception.message shouldBe "Cannot divide by zero"
        }
    }
})