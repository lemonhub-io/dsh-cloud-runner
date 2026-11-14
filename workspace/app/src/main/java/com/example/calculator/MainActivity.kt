package com.example.calculator

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.TextView
import kotlin.math.roundToInt

class MainActivity : Activity() {

    private lateinit var expressionView: TextView
    private lateinit var resultView: TextView

    private val engine = CalculatorEngine()
    private var expression = ""
    private var justEvaluated = false
    private var hadError = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpEdgeToEdge()

        expressionView = findViewById(R.id.expression)
        resultView = findViewById(R.id.result)

        bindButton(R.id.key_clear) { clearAll() }
        bindButton(R.id.key_backspace) { backspace() }
        bindButton(R.id.key_percent) { appendPercent() }
        bindButton(R.id.key_divide) { appendOperator('÷') }
        bindButton(R.id.key_multiply) { appendOperator('×') }
        bindButton(R.id.key_minus) { appendOperator('−') }
        bindButton(R.id.key_plus) { appendOperator('+') }
        bindButton(R.id.key_equals) { evaluate() }
        bindButton(R.id.key_sign) { toggleSign() }
        bindButton(R.id.key_decimal) { appendDecimal() }

        val digits = mapOf(
            R.id.key_0 to "0",
            R.id.key_1 to "1",
            R.id.key_2 to "2",
            R.id.key_3 to "3",
            R.id.key_4 to "4",
            R.id.key_5 to "5",
            R.id.key_6 to "6",
            R.id.key_7 to "7",
            R.id.key_8 to "8",
            R.id.key_9 to "9"
        )
        digits.forEach { (id, digit) ->
            bindButton(id) { appendDigit(digit) }
        }

        if (savedInstanceState != null) {
            expression = savedInstanceState.getString(STATE_EXPRESSION).orEmpty()
            justEvaluated = savedInstanceState.getBoolean(STATE_JUST_EVALUATED)
            hadError = savedInstanceState.getBoolean(STATE_HAD_ERROR)
            expressionView.text = savedInstanceState.getString(STATE_EXPRESSION_TEXT).orEmpty()
            resultView.text = savedInstanceState.getString(STATE_RESULT_TEXT)
                ?: getString(R.string.result_hint)
        } else {
            resetDisplay()
        }
    }

    @Suppress("DEPRECATION")
    private fun setUpEdgeToEdge() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }

        val root = findViewById<View>(R.id.root)
        val density = resources.displayMetrics.density
        val baseLeft = (18 * density).roundToInt()
        val baseTop = (20 * density).roundToInt()
        val baseRight = (18 * density).roundToInt()
        val baseBottom = (14 * density).roundToInt()

        root.setOnApplyWindowInsetsListener { view, insets ->
            val left: Int
            val top: Int
            val right: Int
            val bottom: Int
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bars = insets.getInsets(
                    WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout()
                )
                left = bars.left
                top = bars.top
                right = bars.right
                bottom = bars.bottom
            } else {
                left = insets.systemWindowInsetLeft
                top = insets.systemWindowInsetTop
                right = insets.systemWindowInsetRight
                bottom = insets.systemWindowInsetBottom
            }
            view.setPadding(
                baseLeft + left,
                baseTop + top,
                baseRight + right,
                baseBottom + bottom
            )
            insets
        }
        root.requestApplyInsets()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_EXPRESSION, expression)
        outState.putBoolean(STATE_JUST_EVALUATED, justEvaluated)
        outState.putBoolean(STATE_HAD_ERROR, hadError)
        outState.putString(STATE_EXPRESSION_TEXT, expressionView.text.toString())
        outState.putString(STATE_RESULT_TEXT, resultView.text.toString())
        super.onSaveInstanceState(outState)
    }

    private fun bindButton(id: Int, action: () -> Unit) {
        findViewById<Button>(id).setOnClickListener { button ->
            button.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            action()
        }
    }

    private fun appendDigit(digit: String) {
        if (justEvaluated || hadError) startFreshExpression()
        if (expression.endsWith('%')) return

        val currentNumber = trailingNumber()
        if (digit == "0" && currentNumber == "0") return
        expression += digit
        renderPreview()
    }

    private fun appendDecimal() {
        if (justEvaluated || hadError) startFreshExpression()
        if (expression.endsWith('%')) return

        val currentNumber = trailingNumber()
        if (currentNumber.contains('.')) return
        expression += if (currentNumber.isEmpty()) "0." else "."
        renderPreview()
    }

    private fun appendOperator(operator: Char) {
        if (expression.isEmpty()) {
            // A leading minus is useful for entering a negative number.
            if (operator == '−') {
                expression = "−"
                justEvaluated = false
                hadError = false
                renderPreview()
            }
            return
        }

        justEvaluated = false
        hadError = false

        if (expression.last() == '.') {
            expression += "0"
        }

        when {
            expression.last().isDigit() || expression.last() == '%' -> expression += operator
            expression.last() in OPERATORS -> {
                if (expression.length == 1) {
                    // Do not turn a lone unary minus into a binary operator.
                    if (operator != '−') return
                } else {
                    expression = expression.dropLast(1) + operator
                }
            }
            else -> return
        }
        renderPreview()
    }

    private fun appendPercent() {
        if (hadError) return
        if (justEvaluated) {
            // Allow percentages directly on a calculated result.
            justEvaluated = false
        }
        if (expression.isEmpty() || expression.last() in OPERATORS ||
            expression.last() == '.' || expression.last() == '%'
        ) {
            return
        }
        expression += "%"
        renderPreview()
    }

    private fun toggleSign() {
        if (hadError) return
        if (justEvaluated) justEvaluated = false
        if (expression.isEmpty()) {
            expression = "−"
            renderPreview()
            return
        }
        if (expression.last() in OPERATORS || expression.last() == '%') return

        val match = TRAILING_NUMBER.find(expression) ?: return
        val start = match.range.first
        val hasUnaryMinus = start > 0 &&
            expression[start - 1] == '−' &&
            (start == 1 || expression[start - 2] in OPERATORS)

        expression = if (hasUnaryMinus) {
            expression.removeRange(start - 1, start)
        } else {
            expression.substring(0, start) + "−" + expression.substring(start)
        }
        renderPreview()
    }

    private fun backspace() {
        if (justEvaluated || hadError) {
            clearAll()
            return
        }
        if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
        }
        renderPreview()
    }

    private fun clearAll() {
        expression = ""
        justEvaluated = false
        hadError = false
        resetDisplay()
    }

    private fun evaluate() {
        if (expression.isEmpty() || expression.last() in OPERATORS || expression.last() == '.') {
            return
        }

        try {
            val displayedExpression = expression
            val result = toDisplay(engine.calculate(displayedExpression))
            expressionView.text = getString(R.string.expression_with_equals, displayedExpression)
            resultView.text = result
            expression = result
            justEvaluated = true
            hadError = false
        } catch (_: ArithmeticException) {
            showError()
        } catch (_: IllegalArgumentException) {
            showError()
        }
    }

    private fun renderPreview() {
        expressionView.text = expression
        if (expression.isEmpty()) {
            resultView.text = getString(R.string.result_hint)
            return
        }

        if (expression.last() in OPERATORS || expression.last() == '.' || expression.endsWith("%%")) {
            return
        }

        resultView.text = try {
            toDisplay(engine.calculate(expression))
        } catch (_: RuntimeException) {
            ""
        }
    }

    private fun showError() {
        resultView.text = getString(R.string.error_message)
        hadError = true
        justEvaluated = true
    }

    private fun startFreshExpression() {
        expression = ""
        justEvaluated = false
        hadError = false
        expressionView.text = ""
        resultView.text = getString(R.string.result_hint)
    }

    private fun resetDisplay() {
        expressionView.text = ""
        resultView.text = getString(R.string.result_hint)
    }

    /** Extracts the unsigned number currently being typed, if any. */
    private fun trailingNumber(): String {
        val match = TRAILING_NUMBER.find(expression) ?: return ""
        return match.value
    }

    private fun toDisplay(value: String): String = value.replace('-', '−')

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_0 -> true.also { appendDigit("0") }
            KeyEvent.KEYCODE_1 -> true.also { appendDigit("1") }
            KeyEvent.KEYCODE_2 -> true.also { appendDigit("2") }
            KeyEvent.KEYCODE_3 -> true.also { appendDigit("3") }
            KeyEvent.KEYCODE_4 -> true.also { appendDigit("4") }
            KeyEvent.KEYCODE_5 -> true.also { if (event.isShiftPressed) appendPercent() else appendDigit("5") }
            KeyEvent.KEYCODE_6 -> true.also { appendDigit("6") }
            KeyEvent.KEYCODE_7 -> true.also { appendDigit("7") }
            KeyEvent.KEYCODE_8 -> true.also { appendDigit("8") }
            KeyEvent.KEYCODE_9 -> true.also { appendDigit("9") }
            KeyEvent.KEYCODE_NUMPAD_0 -> true.also { appendDigit("0") }
            KeyEvent.KEYCODE_NUMPAD_1 -> true.also { appendDigit("1") }
            KeyEvent.KEYCODE_NUMPAD_2 -> true.also { appendDigit("2") }
            KeyEvent.KEYCODE_NUMPAD_3 -> true.also { appendDigit("3") }
            KeyEvent.KEYCODE_NUMPAD_4 -> true.also { appendDigit("4") }
            KeyEvent.KEYCODE_NUMPAD_5 -> true.also { appendDigit("5") }
            KeyEvent.KEYCODE_NUMPAD_6 -> true.also { appendDigit("6") }
            KeyEvent.KEYCODE_NUMPAD_7 -> true.also { appendDigit("7") }
            KeyEvent.KEYCODE_NUMPAD_8 -> true.also { appendDigit("8") }
            KeyEvent.KEYCODE_NUMPAD_9 -> true.also { appendDigit("9") }
            KeyEvent.KEYCODE_PERIOD, KeyEvent.KEYCODE_NUMPAD_DOT -> true.also { appendDecimal() }
            KeyEvent.KEYCODE_PLUS, KeyEvent.KEYCODE_NUMPAD_ADD -> true.also { appendOperator('+') }
            KeyEvent.KEYCODE_MINUS, KeyEvent.KEYCODE_NUMPAD_SUBTRACT -> true.also { appendOperator('−') }
            KeyEvent.KEYCODE_STAR, KeyEvent.KEYCODE_NUMPAD_MULTIPLY -> true.also { appendOperator('×') }
            KeyEvent.KEYCODE_SLASH, KeyEvent.KEYCODE_NUMPAD_DIVIDE -> true.also { appendOperator('÷') }
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER,
            KeyEvent.KEYCODE_EQUALS -> true.also { evaluate() }
            KeyEvent.KEYCODE_DEL -> true.also { backspace() }
            KeyEvent.KEYCODE_ESCAPE -> true.also { clearAll() }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    private companion object {
        val OPERATORS = setOf('+', '−', '×', '÷')
        val TRAILING_NUMBER = Regex("(?:\\d+(?:\\.\\d*)?|\\.\\d+)$")

        const val STATE_EXPRESSION = "expression"
        const val STATE_JUST_EVALUATED = "just_evaluated"
        const val STATE_HAD_ERROR = "had_error"
        const val STATE_EXPRESSION_TEXT = "expression_text"
        const val STATE_RESULT_TEXT = "result_text"
    }
}
