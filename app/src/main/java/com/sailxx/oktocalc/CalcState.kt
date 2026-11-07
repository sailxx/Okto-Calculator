package com.sailxx.oktocalc

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class HistoryItem(val expr: String, val result: String)

/** Состояние калькулятора: выражение, предпросмотр результата и история. */
class CalcState(context: Context) {
    private val prefs = context.getSharedPreferences("history", Context.MODE_PRIVATE)

    var expr by mutableStateOf("")
        private set
    var prevExpr by mutableStateOf("")
        private set
    var evaluated by mutableStateOf(false)
        private set
    var error by mutableStateOf<CalcError.Kind?>(null)
        private set
    var inv by mutableStateOf(false)
        private set
    var deg by mutableStateOf(true)

    var keepHistory = true
    val history = mutableStateListOf<HistoryItem>().apply { addAll(loadHistory()) }

    /** Результат «на лету»: показывается серым под выражением, пока оно не равно простому числу. */
    val preview: String?
        get() {
            if (evaluated || expr.isEmpty()) return null
            val s = expr.trimEnd('+', '−', '×', '÷', '^', '.', '(')
            if (s.isEmpty()) return null
            val v = try { Engine.eval(s, deg) } catch (e: CalcError) { return null }
            val f = fmt(v)
            return if (f == s) null else f
        }

    private fun fmt(v: Double) = Engine.format(v).replace('-', '−')

    private fun operandEnd() = expr.isNotEmpty() && (expr.last().isDigit() || expr.last() in ")πe%!")
    private fun startNew() { if (evaluated) { expr = ""; evaluated = false } }

    fun press(k: String) {
        error = null
        when (k) {
            in DIGITS -> digit(k)
            "." -> dot()
            "+", "−", "×", "÷", "^" -> op(k)
            "%", "!" -> if (operandEnd()) { evaluated = false; expr += k }
            "(" -> { startNew(); expr += "(" }
            ")" -> if (canClose()) { evaluated = false; expr += ")" }
            "()" -> if (canClose()) { evaluated = false; expr += ")" } else { startNew(); expr += "(" }
            "π", "e" -> { startNew(); expr += k }
            "sin", "cos", "tan" -> { startNew(); expr += if (inv) "$k⁻¹(" else "$k(" }
            "ln" -> { startNew(); expr += if (inv) "e^(" else "ln(" }
            "log" -> { startNew(); expr += if (inv) "10^(" else "log(" }
            "√" -> { startNew(); expr += "√(" }
            "abs" -> { startNew(); expr += "abs(" }
            "x²" -> if (operandEnd()) { evaluated = false; expr += if (inv) "^3" else "^2" }
            "x⁻¹" -> if (operandEnd()) { evaluated = false; expr += "^(−1)" }
            "inv" -> inv = !inv
            "=" -> equals()
            "±" -> { expr = toggleSign(expr); evaluated = false }
            "DEL" -> delete()
            "AC" -> clear()
        }
    }

    private fun canClose(): Boolean = operandEnd() && expr.count { it == '(' } > expr.count { it == ')' }

    private fun digit(k: String) {
        startNew()
        val n = expr.takeLastWhile { it.isDigit() || it == '.' }
        if (n.length >= 15) return
        expr = if (n == "0") expr.dropLast(1) + k else expr + k
    }

    private fun dot() {
        if (evaluated) { expr = "0."; evaluated = false; return }
        val n = expr.takeLastWhile { it.isDigit() || it == '.' }
        if (n.contains('.')) return
        expr += if (n.isEmpty()) "0." else "."
    }

    private fun op(k: String) {
        evaluated = false
        if (expr.isEmpty()) { if (k == "−") expr = "−"; return }
        val last = expr.last()
        when {
            last in "+−×÷^" -> when {
                expr.length == 1 -> {}
                k == "−" && last in "×÷^" -> expr += k
                else -> expr = expr.dropLast(1) + k
            }
            last == '(' -> if (k == "−") expr += k
            last == '.' -> expr = expr.dropLast(1) + k
            else -> expr += k
        }
    }

    private fun equals() {
        if (expr.isEmpty() || evaluated) return
        try {
            val f = fmt(Engine.eval(expr, deg))
            if (keepHistory && f != expr) {
                history.add(0, HistoryItem(expr, f))
                while (history.size > 100) history.removeAt(history.lastIndex)
                saveHistory()
            }
            prevExpr = expr
            expr = f
            evaluated = true
        } catch (e: CalcError) {
            error = e.kind
        }
    }

    private fun delete() {
        evaluated = false
        expr = deleteLastInput(expr)
    }

    fun clear() { expr = ""; prevExpr = ""; evaluated = false }

    /** Подставить значение из истории. */
    fun insert(text: String) {
        startNew()
        expr = if (expr.isEmpty() || !operandEnd()) expr + text else "$expr×$text"
        evaluated = false
    }

    fun clearHistory() { history.clear(); saveHistory() }

    private fun saveHistory() {
        prefs.edit().putString("items", history.joinToString("\n") { it.expr + "\t" + it.result }).apply()
    }

    private fun loadHistory(): List<HistoryItem> =
        prefs.getString("items", "").orEmpty().lines().mapNotNull {
            val p = it.split('\t')
            if (p.size == 2) HistoryItem(p[0], p[1]) else null
        }

    companion object {
        private val DIGITS = ('0'..'9').map { it.toString() }.toSet()
        fun pretty(s: String, sep: String): String =
            Regex("(?<![\\d.])(\\d{4,})").replace(s) { m ->
                m.value.reversed().chunked(3).joinToString(sep).reversed()
            }
    }
}
