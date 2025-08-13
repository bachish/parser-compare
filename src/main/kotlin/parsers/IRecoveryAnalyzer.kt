package parsers

import measure.ErrorInfo
import org.jgrapht.Graph
import org.jgrapht.alg.similarity.ZhangShashaTreeEditDistance
import org.jgrapht.graph.DefaultEdge
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.max
import kotlin.math.min

/**
 * TokenType -- type of tokens (leafs types)
 * NodeType -- type of AST nodes
 */
interface IRecoveryAnalyzer<TokenType, NodeType> {
    fun getLexerTokens(code: String): List<TokenType>
    fun getParserTokens(code: String): List<TokenType>
    fun getErrors(code: String): List<ErrorInfo>
    fun getParseTree(code: String): NodeType
    fun calculateSimilarity(code: String): Double {
        val lexerTokens = getLexerTokens(code)
        val parserTokens = getParserTokens(code)
        val distance = LevenshteinUtils.levenshteinLine(lexerTokens, parserTokens)
        val maxLength = max(lexerTokens.size, parserTokens.size)
        return if (maxLength > 0) 1 - (distance.toDouble() / maxLength) else 1.0
    }

    fun calculateSimilarity(correctCode: String, brockenCode: String): Double {
        val lexerTokens = getParserTokens(correctCode)
        val parserTokens = getParserTokens(brockenCode)
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

    fun measureParse(file: File) = measureParse(file.readText())
    fun measureParse(code: String): Long = 0L


    fun getTreeEditDistance(code1: String, code2: String): Double {
        val insertCost = 1.0
        val removeCost = 1.0
        val replaceCost = 1.0
        val (graph1, root1) = getGraphFromTree(code1)
        val (graph2, root2) = getGraphFromTree(code2)
        val ted = ZhangShashaTreeEditDistance(
            graph1, root1,
            graph2, root2,
            { _ -> insertCost },
            { _ -> removeCost },
            { n1: NodeType, n2: NodeType ->
                if (equals(n1, n2)) 0.0
                else replaceCost
            }
        )
        return ted.distance
    }

    fun equals(node1: NodeType, node2: NodeType) : Boolean

    fun getGraphFromTree(code: String): Pair<Graph<NodeType, DefaultEdge>, NodeType>

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