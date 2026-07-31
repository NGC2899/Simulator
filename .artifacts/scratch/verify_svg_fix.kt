
import java.util.regex.Pattern

// Mock Offset for testing
data class Offset(val x: Float, val y: Float)

fun main() {
    val svgContent = """
        <svg viewBox="0 0 100 100">
            <metadata>Some metadata that used to crash us</metadata>
            <path d="M 10,10 L 90,90" />
            <circle cx="50" cy="50" r="40" />
            <path d='M 0 0 L 10 0, 10 10, 0 10 Z' />
        </svg>
    """.trimIndent()

    println("Testing robust SVG extraction...")
    val points = extractPointsFromSVG(svgContent)
    println("Extracted points count: ${points.size}")
    if (points.isNotEmpty()) {
        println("First few points: ${points.take(5)}")
    } else {
        println("FAILED: No points extracted!")
    }

    val svgScientific = """<path d="M0,0 L1.2e-1,1.2e-1" />"""
    println("\nTesting scientific notation: $svgScientific")
    val sciPoints = extractPointsFromSVG(svgScientific)
    println("Points: $sciPoints")
}

fun extractPointsFromSVG(svgContent: String): List<Offset> {
    val rawPoints = mutableListOf<Offset>()
    try {
        // The implementation from FourierLogic.kt (relaxed)
        val dPattern = "d=(?:\"|')([^\"']+)(?:\"|')".toRegex()
        val dMatches = dPattern.findAll(svgContent)
        
        for (dMatch in dMatches) {
            val d = dMatch.groupValues[1]
            val tokens = mutableListOf<String>()
            val tokenRegex = "([a-df-z])|(-?\\d*\\.?\\d+(?:e[-+]?\\d+)?)"
            val matcher = Pattern.compile(tokenRegex, Pattern.CASE_INSENSITIVE).matcher(d)
            while (matcher.find()) {
                tokens.add(matcher.group())
            }
            
            var currentX = 0f
            var currentY = 0f
            var startX = 0f
            var startY = 0f
            var i = 0
            while (i < tokens.size) {
                val token = tokens[i]
                if (token[0].isLetter()) {
                    val command = token[0]
                    i++
                    
                    when (command.lowercaseChar()) {
                        'm' -> {
                            if (i + 1 < tokens.size) {
                                val x = tokens[i].toFloatOrNull() ?: 0f
                                val y = tokens[i+1].toFloatOrNull() ?: 0f
                                if (command.isLowerCase()) {
                                    currentX += x; currentY += y
                                } else {
                                    currentX = x; currentY = y
                                }
                                startX = currentX; startY = currentY
                                rawPoints.add(Offset(currentX, currentY))
                                i += 2
                                while (i + 1 < tokens.size && !tokens[i][0].isLetter()) {
                                    val nextX = tokens[i].toFloatOrNull() ?: 0f
                                    val nextY = tokens[i+1].toFloatOrNull() ?: 0f
                                    currentX = if (command.isLowerCase()) currentX + nextX else nextX
                                    currentY = if (command.isLowerCase()) currentY + nextY else nextY
                                    rawPoints.add(Offset(currentX, currentY))
                                    i += 2
                                }
                            }
                        }
                        'l' -> {
                            while (i + 1 < tokens.size && !tokens[i][0].isLetter()) {
                                val x = tokens[i].toFloatOrNull() ?: 0f
                                val y = tokens[i+1].toFloatOrNull() ?: 0f
                                if (command.isLowerCase()) {
                                    currentX += x; currentY += y
                                } else {
                                    currentX = x; currentY = y
                                }
                                rawPoints.add(Offset(currentX, currentY))
                                i += 2
                            }
                        }
                        'z' -> {
                            currentX = startX; currentY = startY
                            rawPoints.add(Offset(currentX, currentY))
                        }
                        // Simplification for test: only M, L, Z
                    }
                } else i++
            }
        }
    } catch (e: Exception) {
        println("Error during extraction: ${e.message}")
        e.printStackTrace()
    }
    return rawPoints
}
