package com.example.matharium.fourier

import kotlin.math.*

/**
 * A simple recursive descent parser for mathematical expressions.
 * Supports: +, -, *, /, ^, %, sin, cos, tan, abs, sqrt, exp, log, floor, ceil, pi, e, x
 */
object FourierExpressionEvaluator {

    fun evaluate(expression: String, x: Double): Double {
        if (expression.isBlank()) return Double.NaN
        val lowerExpr = expression.lowercase(java.util.Locale.US).replace(" ", "")
        return try {
            object : Any() {
                var pos = -1
                var ch = 0

                fun nextChar() {
                    ch = if (++pos < lowerExpr.length) lowerExpr[pos].code else -1
                }

                fun eat(charToEat: Int): Boolean {
                    if (ch == charToEat) {
                        nextChar()
                        return true
                    }
                    return false
                }

                fun parse(): Double {
                    nextChar()
                    val xVal = parseExpression()
                    if (pos < lowerExpr.length) return Double.NaN
                    return if (xVal.isFinite()) xVal else 0.0
                }

                fun parseExpression(): Double {
                    var xVal = parseTerm()
                    while (true) {
                        if (eat('+'.code)) xVal += parseTerm() // addition
                        else if (eat('-'.code)) xVal -= parseTerm() // subtraction
                        else return xVal
                    }
                }

                fun parseTerm(): Double {
                    var xVal = parseFactor()
                    while (true) {
                        if (eat('*'.code)) xVal *= parseFactor() // explicit multiplication
                        else if (eat('/'.code)) xVal /= parseFactor() // division
                        else if (eat('%'.code)) xVal %= parseFactor() // modulo
                        // Implicit multiplication: if the next char is the start of a factor
                        else if (ch == '('.code || (ch >= '0'.code && ch <= '9'.code) || ch == '.'.code || (ch >= 'a'.code && ch <= 'z'.code)) {
                            xVal *= parseFactor()
                        }
                        else return xVal
                    }
                }

                fun parseFactor(): Double {
                    if (eat('+'.code)) return parseFactor() // unary plus
                    if (eat('-'.code)) return -parseFactor() // unary minus

                    var xVal: Double
                    val startPos = pos
                    if (eat('('.code)) { // parentheses
                        xVal = parseExpression()
                        eat(')'.code)
                    } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) { // numbers
                        while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                        xVal = lowerExpr.substring(startPos, pos).toDouble()
                    } else if (ch >= 'a'.code && ch <= 'z'.code) { // functions
                        while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                        val func = lowerExpr.substring(startPos, pos)
                        if (func == "x" || func == "t") {
                            xVal = x
                        } else if (func == "pi") {
                            xVal = PI
                        } else if (func == "e") {
                            xVal = E
                        } else {
                            if (eat('('.code)) {
                                xVal = parseExpression()
                                eat(')'.code)
                                xVal = when (func) {
                                    "sqrt" -> sqrt(xVal)
                                    "sin" -> sin(xVal)
                                    "cos" -> cos(xVal)
                                    "tan" -> tan(xVal)
                                    "abs" -> abs(xVal)
                                    "exp" -> exp(xVal)
                                    "log", "ln" -> ln(xVal)
                                    "floor" -> floor(xVal)
                                    "ceil" -> ceil(xVal)
                                    else -> Double.NaN
                                }
                            } else {
                                xVal = Double.NaN
                            }
                        }
                    } else {
                        return Double.NaN
                    }

                    if (eat('^'.code)) xVal = xVal.pow(parseFactor()) // exponentiation

                    return xVal
                }
            }.parse()
        } catch (e: Exception) {
            Double.NaN
        }
    }
}
