package com.example

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import io.kotest.matchers.shouldBe

/**
 * Property-based tests for Calculator class
 */
class CalculatorPropertyTest : StringSpec({
    val calculator = Calculator()

    "Addition commutative property: a + b = b + a" {
        checkAll<Int, Int> { a, b ->
            calculator.add(a, b) shouldBe calculator.add(b, a)
        }
    }

    "Addition and subtraction relationship: (a + b) - b = a" {
        checkAll(Arb.int(-100..100), Arb.int(-100..100)) { a, b ->
            calculator.subtract(calculator.add(a, b), b) shouldBe a
        }
    }

    "Multiplication commutative property: a * b = b * a" {
        checkAll<Int, Int> { a, b ->
            calculator.multiply(a, b) shouldBe calculator.multiply(b, a)
        }
    }

    "Multiplication distributive property: a * (b + c) = a * b + a * c" {
        checkAll(
            Arb.int(-10..10),
            Arb.int(-10..10),
            Arb.int(-10..10)
        ) { a, b, c ->
            val left = calculator.multiply(a, calculator.add(b, c))
            val right = calculator.add(calculator.multiply(a, b), calculator.multiply(a, c))
            left shouldBe right
        }
    }
})