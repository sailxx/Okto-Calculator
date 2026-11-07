package com.sailxx.oktocalc

import org.junit.Assert.assertEquals
import org.junit.Test

class InputActionsTest {
    @Test fun deleteDigitsOperatorsAndEmptyInput() {
        assertEquals("2", deleteLastInput("25"))
        assertEquals("", deleteLastInput("2"))
        assertEquals("", deleteLastInput(""))
        assertEquals("25", deleteLastInput("25+"))
        assertEquals("2.", deleteLastInput("2.5"))
        assertEquals("2", deleteLastInput("2."))
    }
    @Test fun deleteWholeFunctionTokens() {
        for (token in listOf("sin⁻¹(", "cos⁻¹(", "tan⁻¹(", "^(−1)", "sin(", "cos(", "tan(", "log(", "abs(", "e^(", "10^(", "ln(", "√(")) {
            assertEquals("2+", deleteLastInput("2+$token"))
        }
        assertEquals("sin(", deleteLastInput("sin(2"))
        assertEquals("sin(2", deleteLastInput("sin(2)"))
    }
    @Test fun toggleTrailingOperand() {
        assertEquals("−", toggleSign(""))
        assertEquals("−42", toggleSign("42"))
        assertEquals("42", toggleSign("−42"))
        assertEquals("2+−3", toggleSign("2+3"))
        assertEquals("2+3", toggleSign("2+−3"))
        assertEquals("−1E−8", toggleSign("1E−8"))
        assertEquals("(−(2+3))", toggleSign("(2+3)"))
        assertEquals("(2+3)", toggleSign("(−(2+3))"))
        assertEquals("2+", toggleSign("2+"))
    }
    @Test fun toggledExpressionsEvaluate() {
        assertEquals(-6.0, Engine.eval(toggleSign("2×3"), true), 0.0)
        assertEquals(-1.0, Engine.eval(toggleSign("2+3"), true), 0.0)
    }
}
