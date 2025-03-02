package com.example

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

/**
 * Calculator class tests (WordSpec)
 */
class CalculatorWordSpecTest : WordSpec({
    val calculator = Calculator()

    "A Calculator" should {
        "add two numbers correctly" {
            calculator.add(2, 3) shouldBe 5
        }

        "subtract two numbers correctly" {
            calculator.subtract(5, 3) shouldBe 2
        }

        "multiply two numbers correctly" {
            calculator.multiply(2, 3) shouldBe 6
        }

        "divide two numbers correctly" {
            calculator.divide(6, 3) shouldBe 2
        }
    }
    
    "Division by zero" should {
        "throw an IllegalArgumentException" {
            val exception = shouldThrow<IllegalArgumentException> {
                calculator.divide(1, 0)
            }
            exception.message shouldBe "Cannot divide by zero"
        }
    }
})

/**
 * Calculator class tests (FreeSpec)
 */
class CalculatorFreeSpecTest : FreeSpec({
    val calculator = Calculator()

    "Calculator Tests" - {
        "Addition" - {
            "2 + 3 = 5" {
                calculator.add(2, 3) shouldBe 5
            }
            "Adding negative numbers" {
                calculator.add(-2, -3) shouldBe -5
            }
        }

        "Subtraction" - {
            "5 - 3 = 2" {
                calculator.subtract(5, 3) shouldBe 2
            }
            "Subtracting negative numbers" {
                calculator.subtract(-2, -3) shouldBe 1
            }
        }

        "Multiplication" - {
            "2 * 3 = 6" {
                calculator.multiply(2, 3) shouldBe 6
            }
        }

        "Division" - {
            "6 / 3 = 2" {
                calculator.divide(6, 3) shouldBe 2
            }
            "Dividing by zero throws an exception" {
                val exception = shouldThrow<IllegalArgumentException> {
                    calculator.divide(1, 0)
                }
                exception.message shouldBe "Cannot divide by zero"
            }
        }
    }
})