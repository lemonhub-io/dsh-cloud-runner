package com.example.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * A small, UI-independent calculator expression evaluator.
 *
 * Supported syntax:
 *   +, -, *, /, %, decimal numbers, unary signs and whitespace.
 *
 * Multiplication and division have a higher precedence than addition and
 * subtraction. Percentage is a postfix operator, so `50%` evaluates to 0.5.
 */
class CalculatorEngine {

    fun calculate(expression: String): String = format(evaluate(expression))

    fun evaluate(expression: String): BigDecimal {
        require(expression.isNotBlank()) { "Expression must not be empty" }
        val parser = Parser(normalize(expression))
        val value = parser.parseExpression()
        parser.requireEnd()
        return value
    }

    private fun normalize(expression: String): String =
        expression
            .replace('×', '*')
            .replace('÷', '/')
            .replace('−', '-')
            .replace('—', '-')

    private fun format(value: BigDecimal): String {
        if (value.signum() == 0) return "0"

        val stripped = value.stripTrailingZeros()
        val plain = stripped.toPlainString()
        if (plain.length <= 18) return plain

        val rounded = value
            .round(MathContext(12, RoundingMode.HALF_UP))
            .stripTrailingZeros()
        return rounded.toString()
    }

    private class Parser(private val source: String) {
        private var position = 0

        fun parseExpression(): BigDecimal {
            var value = parseTerm()
            while (true) {
                skipWhitespace()
                when {
                    consume('+') -> value = value.add(parseTerm(), MATH_CONTEXT)
                    consume('-') -> value = value.subtract(parseTerm(), MATH_CONTEXT)
                    else -> return value
                }
            }
        }

        fun requireEnd() {
            skipWhitespace()
            if (position != source.length) {
                throw IllegalArgumentException(
                    "Unexpected character '${source[position]}' at position $position"
                )
            }
        }

        private fun parseTerm(): BigDecimal {
            var value = parseFactor()
            while (true) {
                skipWhitespace()
                when {
                    consume('*') -> value = value.multiply(parseFactor(), MATH_CONTEXT)
                    consume('/') -> {
                        val divisor = parseFactor()
                        if (divisor.signum() == 0) {
                            throw ArithmeticException("Division by zero")
                        }
                        value = value.divide(divisor, MATH_CONTEXT)
                    }
                    else -> return value
                }
            }
        }

        private fun parseFactor(): BigDecimal {
            skipWhitespace()
            var negative = false
            while (position < source.length) {
                when (source[position]) {
                    '+' -> position++
                    '-' -> {
                        negative = !negative
                        position++
                    }
                    else -> break
                }
                skipWhitespace()
            }

            var value = parseNumber()
            skipWhitespace()
            while (position < source.length && source[position] == '%') {
                position++
                value = value.divide(ONE_HUNDRED, MATH_CONTEXT)
                skipWhitespace()
            }
            return if (negative) value.negate() else value
        }

        private fun parseNumber(): BigDecimal {
            skipWhitespace()
            val start = position
            var hasDigits = false

            while (position < source.length && source[position].isDigit()) {
                position++
                hasDigits = true
            }
            if (position < source.length && source[position] == '.') {
                position++
                while (position < source.length && source[position].isDigit()) {
                    position++
                    hasDigits = true
                }
            }

            require(hasDigits) { "Expected a number at position $start" }
            return BigDecimal(source.substring(start, position), MATH_CONTEXT)
        }

        private fun consume(expected: Char): Boolean {
            if (position < source.length && source[position] == expected) {
                position++
                return true
            }
            return false
        }

        private fun skipWhitespace() {
            while (position < source.length && source[position].isWhitespace()) {
                position++
            }
        }
    }

    private companion object {
        val MATH_CONTEXT: MathContext = MathContext.DECIMAL64
        val ONE_HUNDRED: BigDecimal = BigDecimal(100)
    }
}
