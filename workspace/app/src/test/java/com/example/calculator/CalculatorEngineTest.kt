package com.example.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CalculatorEngineTest {

    private val engine = CalculatorEngine()

    @Test
    fun additionAndSubtraction() {
        assertEquals("9", engine.calculate("4 + 5"))
        assertEquals("-1", engine.calculate("4 − 5"))
    }

    @Test
    fun multiplicationAndDivisionTakePrecedence() {
        assertEquals("14", engine.calculate("2 + 3 × 4"))
        assertEquals("2.5", engine.calculate("10 ÷ 4"))
        assertEquals("7", engine.calculate("2 × 3 + 4 ÷ 4"))
    }

    @Test
    fun decimalCalculationsArePrecise() {
        assertEquals("0.3", engine.calculate("0.1 + 0.2"))
        assertEquals("3.75", engine.calculate("1.5 + 2.25"))
        assertEquals("0.5", engine.calculate(".5"))
    }

    @Test
    fun percentIsAPostfixOperator() {
        assertEquals("0.1", engine.calculate("10%"))
        assertEquals("20", engine.calculate("200 × 10%"))
    }

    @Test
    fun unarySignsAreSupported() {
        assertEquals("-3", engine.calculate("−5 + 2"))
        assertEquals("8", engine.calculate("5 − −3"))
        assertEquals("3", engine.calculate("−5 + 2 × 4"))
    }

    @Test
    fun divisionByZeroThrows() {
        assertThrows(ArithmeticException::class.java) {
            engine.calculate("8 ÷ 0")
        }
    }

    @Test
    fun invalidExpressionsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            engine.calculate("2 +")
        }
        assertThrows(IllegalArgumentException::class.java) {
            engine.calculate("1.2.3")
        }
    }

    @Test
    fun largeResultsAreTrimmedButRemainReadable() {
        assertEquals("100000000000000", engine.calculate("100000000 × 1000000"))
        assertEquals("99999999999999.9", engine.calculate("999999999999999 ÷ 10"))
    }
}
