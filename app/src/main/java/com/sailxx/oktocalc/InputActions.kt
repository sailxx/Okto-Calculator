package com.sailxx.oktocalc

/** Delete a complete function token, or the last character for ordinary input. */
internal fun deleteLastInput(expression: String): String {
    val token = deletionTokens.firstOrNull { expression.endsWith(it) }
    return expression.dropLast(token?.length ?: 1)
}

private val deletionTokens = listOf(
    "sin⁻¹(", "cos⁻¹(", "tan⁻¹(", "^(−1)", "sin(", "cos(", "tan(",
    "log(", "abs(", "e^(", "10^(", "ln(", "√("
).sortedByDescending { it.length }

/** Toggle the trailing operand without changing the preceding calculation. */
internal fun toggleSign(expression: String): String {
    if (expression.isEmpty()) return "−"
    if (expression.endsWith(')')) {
        var depth = 0
        var start = expression.lastIndex
        for (i in expression.indices.reversed()) {
            if (expression[i] == ')') depth++
            if (expression[i] == '(') depth--
            if (depth == 0) { start = i; break }
        }
        val operand = expression.substring(start)
        return if (operand.startsWith("(−")) expression.take(start) + operand.substring(2, operand.length - 1)
        else expression.take(start) + "(−$operand)"
    }
    val number = Regex("(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:E[−+-]?\\d+)?$|[πe]$").find(expression)
        ?: return expression
    val start = number.range.first
    val prefix = expression.take(start)
    val unaryMinus = prefix.endsWith('−') && (prefix.length == 1 || prefix[prefix.length - 2] in "(+−×÷^")
    return if (unaryMinus) prefix.dropLast(1) + number.value else prefix + "−".let {
        if (prefix.isEmpty() || prefix.last() in "(+−×÷^") it + number.value else "(−${number.value})"
    }
}
