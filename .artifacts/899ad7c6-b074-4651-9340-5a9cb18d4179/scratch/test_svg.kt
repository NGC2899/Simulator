
import java.util.regex.Pattern

// Mock Offset for testing
data class Offset(val x: Float, val y: Float)

fun main() {
    val svgContent = """
        <svg viewBox="0 0 100 100">
            <path d="M 10,10 L 90,90" />
            <circle cx="50" cy="50" r="40" />
        </svg>
    """.trimIndent()

    try {
        println("Testing SVG with circle tag...")
        extractPointsFromSVG(svgContent)
        println("Success (Unexpected, should have failed)")
    } catch (e: Exception) {
        println("Caught expected exception: ${e.message}")
    }

    val svgCommas = """<path d="M10,20L30,40" />"""
    println("\nTesting SVG with commas: $svgCommas")
    val points = extractPointsFromSVG(svgCommas)
    println("Points: $points")
}

fun extractPointsFromSVG(svgContent: String): List<Offset> {
    val rawPoints = mutableListOf<Offset>()
    val tagPattern = "<([a-zA-Z0-9]+)".toRegex()
    val matches = tagPattern.findAll(svgContent)
    for (match in matches) {
        val tagName = match.groupValues[1].lowercase()
        // Simulate the strict check
        if (tagName != "svg" && tagName != "path" && tagName != "g" && tagName != "defs" && tagName != "style") {
            throw IllegalArgumentException("SVG contains unsupported element: ${tagName}. Only paths are allowed.")
        }
    }

    val dPattern = "d=\"([^\"]+)\"".toRegex()
    val dMatches = dPattern.findAll(svgContent)
    
    for (dMatch in dMatches) {
        val d = dMatch.groupValues[1]
        val tokens = mutableListOf<String>()
        val tokenRegex = "([a-df-z])|(-?\\d*\\.?\\d+(?:e[-+]?\\d+)?)"
        val matcher = Pattern.compile(tokenRegex, Pattern.CASE_INSENSITIVE).matcher(d)
        while (matcher.find()) {
            tokens.add(matcher.group())
        }
        println("Tokens found: ${tokens}")
    }
    return emptyList()
}
