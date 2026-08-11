package com.example.matharium.fourier.engine

import kotlin.math.*

/**
 * A simple recursive descent parser for mathematical expressions.
 * Compiles expressions into a tree for high-performance evaluation.
 */
object FourierExpressionEvaluator {

    interface Node {
        fun eval(x: Double): Double
    }

    class Constant(val value: Double) : Node {
        override fun eval(x: Double) = value
    }

    class Variable : Node {
        override fun eval(x: Double) = x
    }

    class Unary(val op: Char, val child: Node) : Node {
        override fun eval(x: Double) = when (op) {
            '+' -> child.eval(x)
            '-' -> -child.eval(x)
            else -> Double.NaN
        }
    }

    class Binary(val op: Char, val left: Node, val right: Node) : Node {
        override fun eval(x: Double): Double {
            val a = left.eval(x)
            val b = right.eval(x)
            return when (op) {
                '+' -> a + b
                '-' -> a - b
                '*' -> a * b
                '/' -> a / b
                '%' -> a % b
                '^' -> a.pow(b)
                else -> Double.NaN
            }
        }
    }

    class Function(val name: String, val child: Node) : Node {
        override fun eval(x: Double): Double {
            val v = child.eval(x)
            return when (name) {
                "sqrt" -> sqrt(v)
                "sin" -> sin(v)
                "cos" -> cos(v)
                "tan" -> tan(v)
                "abs" -> abs(v)
                "exp" -> exp(v)
                "log", "ln" -> ln(v)
                "floor" -> floor(v)
                "ceil" -> ceil(v)
                else -> Double.NaN
            }
        }
    }

    private val cache = java.util.concurrent.ConcurrentHashMap<String, Node>()

    private object ErrorNode : Node {
        override fun eval(x: Double) = Double.NaN
    }

    fun compile(expression: String): Node? {
        val clean = expression.lowercase(java.util.Locale.US).replace(" ", "")
        if (clean.isBlank()) return null
        
        val cached = cache[clean]
        if (cached != null) return if (cached === ErrorNode) null else cached
        
        return try {
            val node = Parser(clean).parse()
            cache[clean] = node
            node
        } catch (e: Exception) {
            cache[clean] = ErrorNode
            null
        }
    }

    fun evaluate(expression: String, x: Double): Double {
        val node = compile(expression) ?: return Double.NaN
        return node.eval(x)
    }

    private val functions = setOf("sin", "cos", "tan", "sqrt", "abs", "log", "ln", "exp", "floor", "ceil")

    private class Parser(val input: String) {
        var pos = -1
        var ch = 0

        fun nextChar() {
            ch = if (++pos < input.length) input[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Node {
            if (input.isBlank()) throw Exception("Empty expression")
            nextChar()
            val node = parseExpression()
            if (pos < input.length) throw Exception("Unexpected character: '${input[pos]}'")
            return node
        }

        fun parseExpression(): Node {
            var node = parseTerm()
            while (true) {
                if (eat('+'.code)) node = Binary('+', node, parseTerm())
                else if (eat('-'.code)) node = Binary('-', node, parseTerm())
                else return node
            }
        }

        fun parseTerm(): Node {
            var node = parseFactor()
            while (true) {
                if (eat('*'.code)) node = Binary('*', node, parseFactor())
                else if (eat('/'.code)) node = Binary('/', node, parseFactor())
                else if (eat('%'.code)) node = Binary('%', node, parseFactor())
                else if (ch == '('.code || (ch >= '0'.code && ch <= '9'.code) || ch == '.'.code || (ch >= 'a'.code && ch <= 'z'.code)) {
                    node = Binary('*', node, parseFactor()) // Implicit mult
                } else return node
            }
        }

        fun parseFactor(): Node {
            if (eat('+'.code)) return Unary('+', parseFactor())
            if (eat('-'.code)) return Unary('-', parseFactor())

            var node: Node
            val startPos = pos
            if (eat('('.code)) {
                node = parseExpression()
                if (!eat(')'.code)) {
                    throw Exception("Expected ')'")
                }
            } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) {
                while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                node = Constant(input.substring(startPos, pos).toDouble())
            } else if (ch >= 'a'.code && ch <= 'z'.code) {
                while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                val word = input.substring(startPos, pos)
                if (word == "x" || word == "t") {
                    node = Variable()
                } else if (word == "pi") {
                    node = Constant(PI)
                } else if (word == "e") {
                    node = Constant(E)
                } else {
                    if (functions.contains(word)) {
                        if (eat('('.code)) {
                            val argument = parseExpression()
                            if (!eat(')'.code)) {
                                throw Exception("Expected ')' after function argument")
                            }
                            node = Function(word, argument)
                        } else {
                            throw Exception("Function '$word' requires parentheses")
                        }
                    } else if (word.startsWith("x") || word.startsWith("t")) {
                        // Handle cases like "xsin(x)" where "x" is followed by a function
                        // Backtrack to just after the 'x' or 't'
                        pos = startPos + 1
                        ch = if (pos < input.length) input[pos].code else -1
                        node = Variable()
                    } else {
                        throw Exception("Unknown function or variable: '$word'")
                    }
                }
            } else throw Exception("Unexpected character: '${ch.toChar()}'")

            if (eat('^'.code)) node = Binary('^', node, parseFactor())

            return node
        }
    }
}
