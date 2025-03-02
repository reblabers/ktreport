package com.example

/**
 * Simple calculator class
 */
class Calculator {
    
    /**
     * Adds two numbers
     * @param a First number
     * @param b Second number
     * @return Sum
     */
    fun add(a: Int, b: Int): Int {
        return a + b
    }
    
    /**
     * Subtracts two numbers
     * @param a First number
     * @param b Second number
     * @return Difference
     */
    fun subtract(a: Int, b: Int): Int {
        return a - b
    }
    
    /**
     * Multiplies two numbers
     * @param a First number
     * @param b Second number
     * @return Product
     */
    fun multiply(a: Int, b: Int): Int {
        return a * b
    }
    
    /**
     * Divides two numbers
     * @param a First number
     * @param b Second number
     * @return Quotient
     * @throws IllegalArgumentException When dividing by zero
     */
    fun divide(a: Int, b: Int): Int {
        require(b != 0) { "Cannot divide by zero" }
        return a / b
    }
}