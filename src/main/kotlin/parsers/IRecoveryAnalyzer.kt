package parsers

import measure.ErrorInfo
import parsers.antlr.AntlrJava8Analyzer
import parsers.antlr.AntlrJavaAnalyzer
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.max
import kotlin.math.min

// Интерфейс для анализа кода (T - тип токенов)
interface IRecoveryAnalyzer<TokenType> {
    fun getLexerTokens(code: String): List<TokenType>
    fun getParserTokens(code: String): List<TokenType>
    fun getErrors(code: String): List<ErrorInfo>
    fun calculateSimilarity(code: String): Double {
        val lexerTokens = getLexerTokens(code)
        val parserTokens = getParserTokens(code)
        val distance = LevenshteinUtils.levenshteinLine(lexerTokens, parserTokens)
        val maxLength = max(lexerTokens.size, parserTokens.size)
        return if (maxLength > 0) 1 - (distance.toDouble() / maxLength) else 1.0
    }

    fun calculateSimilarity(file: File): Double {
        val code = Files.readString(Paths.get(file.absolutePath))
        try {
            return calculateSimilarity(code)
        } catch (e: Throwable) {
            println("\nCan't process file \n${file.absolutePath}")
            println(e.message)
            throw e
        }
    }

    //    fun hollowParse(code: String) {
//        getLexerTokens(code)
//        getParserTokens(code)
//    }
//    fun hollowParse(file: File) = hollowParse(file.readText())
    // Возвращает время парсинга в наносекундах, по умолчанию 0
    fun measureParse(file: File) = measureParse(file.readText())
    fun measureParse(code: String): Long = 0L

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




fun main() {
    val javaAnalyzer = AntlrJavaAnalyzer()
    val java8Analyzer = AntlrJava8Analyzer()
    val code = "class Test { void method(){ int x; x; } }" // not.stmt

    println("Java Similarity: ${javaAnalyzer.calculateSimilarity(code)}")
    println("Java8 Similarity: ${java8Analyzer.calculateSimilarity(code)}")
}