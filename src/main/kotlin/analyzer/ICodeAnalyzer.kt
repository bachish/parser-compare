package analyzer

import analyzer.antlr.AntlrJava8Analyzer
import analyzer.antlr.AntlrJavaAnalyzer
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.max
import kotlin.math.min

// Интерфейс для анализа кода (T - тип токенов)
interface ICodeAnalyzer<T> {
    fun getLexerTokens(code: String): List<T>
    fun getParserTokens(code: String): List<T>

    fun calculateSimilarity(code: String): Double {
        val lexerTokens = getLexerTokens(code)
        val parserTokens = getParserTokens(code)
        val distance = LevenshteinUtils.levenshteinLine(lexerTokens, parserTokens)
        val maxLength = max(lexerTokens.size, parserTokens.size)
        return if (maxLength > 0) 1 - (distance.toDouble() / maxLength) else 1.0
    }

    fun calculateSimilarity(file: File): Double {
        val code = Files.readString(Paths.get(file.absolutePath))
        return calculateSimilarity(code)
    }

    fun hollowParse(code: String) {
        getLexerTokens(code)
        getParserTokens(code)
    }
}

// Утилита для вычисления расстояния Левенштейна
object LevenshteinUtils {
    fun <T> levenshteinLine(lhs: List<T>, rhs: List<T>): Int {
        if (lhs == rhs) return 0
        if (lhs.isEmpty()) return rhs.size
        if (rhs.isEmpty()) return lhs.size

        val lhsLength = lhs.size + 1
        val rhsLength = rhs.size + 1

        var cost = Array(lhsLength) { it }
        var newCost = Array(lhsLength) { 0 }

        for (i in 1 until rhsLength) {
            newCost[0] = i
            for (j in 1 until lhsLength) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1
                newCost[j] = min(min(costInsert, costDelete), costReplace)
            }
            val swap = cost
            cost = newCost
            newCost = swap
        }
        return cost[lhsLength - 1]
    }

    // todo("квадратичное добавить")
}


// Пример реализации для не-ANTLR парсера
class NewAnalyzer : ICodeAnalyzer<String> {
    override fun getLexerTokens(code: String): List<String> {
        return code.split(" ").filter { it.isNotBlank() }
    }

    override fun getParserTokens(code: String): List<String> {
        return code.split(" ").filter { it.isNotBlank() }
    }
}

fun main() {
    val javaAnalyzer = AntlrJavaAnalyzer()
    val java8Analyzer = AntlrJava8Analyzer()
    val newAnalyzer = NewAnalyzer()
    val code = "class Test { void method(){ int x; x; } }" // not.stmt

    println("Java Similarity: ${javaAnalyzer.calculateSimilarity(code)}")
    println("Java8 Similarity: ${java8Analyzer.calculateSimilarity(code)}")
    println("New Similarity: ${newAnalyzer.calculateSimilarity(code)}")
}