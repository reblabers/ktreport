package com.example

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Tests for the Calculator class
 */
class CalculatorTest {

    private val calculator = Calculator()

    @Nested
    @DisplayName("Addition Tests")
    inner class AdditionTests {
        @Test
        @DisplayName("Adding positive numbers")
        fun testAddPositiveNumbers() {
            Assertions.assertEquals(5, calculator.add(2, 3), "2 + 3 should equal 5")
        }

        @Test
        @DisplayName("Adding negative numbers")
        fun testAddNegativeNumbers() {
            Assertions.assertEquals(-5, calculator.add(-2, -3), "-2 + -3 should equal -5")
        }

        @ParameterizedTest(name = "{0} + {1} = {2}")
        @CsvSource(
            "0, 0, 0",
            "1, 1, 2",
            "-1, -2, -3",
            "10, -5, 5"
        )
        @DisplayName("Parameterized addition tests")
        fun testAddWithParameters(a: Int, b: Int, expected: Int) {
            Assertions.assertEquals(expected, calculator.add(a, b))
        }
    }

    @Nested
    @DisplayName("Subtraction Tests")
    inner class SubtractionTests {
        @Test
        @DisplayName("Subtracting positive numbers")
        fun testSubtractPositiveNumbers() {
            Assertions.assertEquals(2, calculator.subtract(5, 3), "5 - 3 should equal 2")
        }

        @Test
        @DisplayName("Subtracting negative numbers")
        fun testSubtractNegativeNumbers() {
            Assertions.assertEquals(1, calculator.subtract(-2, -3), "-2 - -3 should equal 1")
        }
    }

    @Nested
    @DisplayName("Multiplication Tests")
    inner class MultiplicationTests {
        @Test
        @DisplayName("Multiplying positive numbers")
        fun testMultiplyPositiveNumbers() {
            Assertions.assertEquals(6, calculator.multiply(2, 3), "2 * 3 should equal 6")
        }

        @Test
        @DisplayName("Multiplying negative numbers")
        fun testMultiplyNegativeNumbers() {
            Assertions.assertEquals(6, calculator.multiply(-2, -3), "-2 * -3 should equal 6")
        }

        @Test
        @DisplayName("Deliberately failing test")
        fun testMultiplyFailing() {
            Assertions.assertEquals(7, calculator.multiply(2, 3), "This test should fail")
        }
    }

    @Nested
    @DisplayName("Division Tests")
    inner class DivisionTests {
        @Test
        @DisplayName("Dividing positive numbers")
        fun testDividePositiveNumbers() {
            Assertions.assertEquals(2, calculator.divide(6, 3), "6 / 3 should equal 2")
        }

        @Test
        @DisplayName("Dividing by zero")
        fun testDivideByZero() {
            val exception = assertThrows<IllegalArgumentException> {
                calculator.divide(1, 0)
            }
            Assertions.assertEquals("Cannot divide by zero", exception.message)
        }
    }
}