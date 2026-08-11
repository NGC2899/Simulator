package com.example.matharium.fourier.engine

import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs

object FourierExportLogic {

    data class TermData(
        val freq: Float,
        val amp: Float,
        val phase: Float // in radians
    )

    fun generateNormalSeries(terms: List<TermData>, is2D: Boolean): String {
        if (terms.isEmpty()) return "0"
        
        val sb = StringBuilder()
        if (is2D) sb.append("f(t) = (x(t), y(t))\n\n")
        
        // x(t) or f(t)
        if (is2D) sb.append("x(t) = ") else sb.append("f(t) = ")
        
        val filtered = terms.filter { abs(it.amp) > 0.001f }
        if (filtered.isEmpty()) {
            sb.append("0")
        } else {
            filtered.forEachIndexed { i, term ->
                if (i > 0 && term.amp >= 0) sb.append(" + ")
                else if (term.amp < 0) sb.append(" - ")

                val a = abs(term.amp)
                if (abs(a - 1.0f) > 0.001f) sb.append("%.2f".format(Locale.US, a))
                
                sb.append(if (is2D) "cos(" else "sin(")
                if (abs(term.freq - 1.0f) < 0.001f) {
                    sb.append("2πt")
                } else if (abs(term.freq) < 0.001f) {
                    sb.append("0")
                } else {
                    sb.append("%.2f · 2πt".format(Locale.US, term.freq))
                }

                val phaseDeg = term.phase * 180f / PI.toFloat()
                if (abs(phaseDeg) > 0.1f) {
                    if (phaseDeg > 0) sb.append(" + %.1f°".format(Locale.US, phaseDeg))
                    else sb.append(" - %.1f°".format(Locale.US, abs(phaseDeg)))
                }
                sb.append(")")
            }
        }

        if (is2D) {
            sb.append("\n\ny(t) = ")
            if (filtered.isEmpty()) {
                sb.append("0")
            } else {
                filtered.forEachIndexed { i, term ->
                    if (i > 0 && term.amp >= 0) sb.append(" + ")
                    else if (term.amp < 0) sb.append(" - ")

                    val a = abs(term.amp)
                    if (abs(a - 1.0f) > 0.001f) sb.append("%.2f".format(Locale.US, a))
                    
                    sb.append("sin(")
                    if (abs(term.freq - 1.0f) < 0.001f) {
                        sb.append("2πt")
                    } else if (abs(term.freq) < 0.001f) {
                        sb.append("0")
                    } else {
                        sb.append("%.2f · 2πt".format(Locale.US, term.freq))
                    }

                    val phaseDeg = term.phase * 180f / PI.toFloat()
                    if (abs(phaseDeg) > 0.1f) {
                        if (phaseDeg > 0) sb.append(" + %.1f°".format(Locale.US, phaseDeg))
                        else sb.append(" - %.1f°".format(Locale.US, abs(phaseDeg)))
                    }
                    sb.append(")")
                }
            }
        }

        return sb.toString()
    }

    fun generateComplexSeries(terms: List<TermData>): String {
        if (terms.isEmpty()) return "f(t) = 0"
        
        val sb = StringBuilder()
        sb.append("f(t) = ")
        
        val filtered = terms.filter { abs(it.amp) > 0.001f }
        if (filtered.isEmpty()) {
            sb.append("0")
        } else {
            filtered.forEachIndexed { i, term ->
                if (i > 0) sb.append(" + ")

                if (abs(term.amp - 1.0f) > 0.001f) sb.append("%.2f".format(Locale.US, term.amp))
                
                sb.append("e^{i(")
                if (abs(term.freq - 1.0f) < 0.001f) {
                    sb.append("2πt")
                } else if (abs(term.freq) < 0.001f) {
                    sb.append("0")
                } else {
                    sb.append("%.2f · 2πt".format(Locale.US, term.freq))
                }

                val phaseDeg = term.phase * 180f / PI.toFloat()
                if (abs(phaseDeg) > 0.1f) {
                    if (phaseDeg > 0) sb.append(" + %.1f°".format(Locale.US, phaseDeg))
                    else sb.append(" - %.1f°".format(Locale.US, abs(phaseDeg)))
                }
                sb.append(")}")
            }
        }
        
        return sb.toString()
    }
}
