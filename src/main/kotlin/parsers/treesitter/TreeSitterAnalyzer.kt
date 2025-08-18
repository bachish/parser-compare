package parsers.treesitter

import jflex.JavaScanner
import jflex.JavaToken
import jflex.TreeSitterLexer
import measure.ErrorInfo
import measure.MISSING_SEMICOLON
import measure.UNKNOWN_ERROR
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph
import org.treesitter.TSLanguage
import org.treesitter.TSNode
import org.treesitter.TSParser
import org.treesitter.TreeSitterJava
import parsers.IRecoveryAnalyzer
import java.io.StringReader
import kotlin.system.measureNanoTime


class TreeSitterAnalyzer : IRecoveryAnalyzer<Int, TSNode> {
    var source: String = ""
    override fun getLexerTokens(code: String): List<Int> {
        val scanner = JavaScanner(StringReader(code))
        val tokens = mutableListOf<Int>()
        var token: JavaToken
        while (scanner.yylex().also { token = it } != JavaToken.EOF) {
            tokens.add(token.ordinal) // Извлекаем тип токена как Int (ordinal из enum JavaToken)
        }
        return tokens
    }

    // Токены от парсера (TreeSitter через TreeSitterLexer)
    override fun getParserTokens(code: String): List<Int> {
        val leaves = getTreeSitterLeaves(code) // Получаем листья дерева разбора TreeSitter
        val lexer = TreeSitterLexer(StringReader(leaves))
        val tokens = mutableListOf<Int>()
        var token: JavaToken?
        while (lexer.nextToken().also { token = it } != JavaToken.EOF) {
            tokens.add(token!!.ordinal) // Извлекаем тип токена как Int (ordinal из enum JavaToken)
        }
        return tokens
    }

    override fun getErrors(code: String): List<ErrorInfo> {
        val rootNode = getParseTree(code)
        val errorNodes = getErrorNodes(rootNode)
        return errorNodes.map { getErrorInfo(it) }
    }

    private fun getErrorInfo(errorNode: TSNode): ErrorInfo {
        if (errorNode.isMissing) {
            val error = when (errorNode.type) {
                ";" -> MISSING_SEMICOLON
                else -> throw IllegalStateException("new missing node type!")
            }
            return ErrorInfo(error)
        }
        return ErrorInfo(UNKNOWN_ERROR)

    }

    private fun getErrorNodes(node: TSNode): List<TSNode> {
        val res = ArrayList<TSNode>()
        for (i in 0..<node.childCount) {
            val child = node.getChild(i)
            if (child.isError || child.isMissing) {
                res.add(child)
            }
            if (child.hasError()) {
                res.addAll(getErrorNodes(child))
            }
        }
        return res
    }

    // Вспомогательная функция для получения листьев дерева TreeSitter
    private fun getTreeSitterLeaves(code: String): String {
        val parser = TSParser()
        parser.setLanguage(TreeSitterJava())
        val tree = parser.parseString(null, code)
        val leaves = mutableListOf<String>()


        fun traverse(node: TSNode) {
            if (node.childCount == 0) {
                if (node.parent.isError) {
                    leaves.add("error")
                } else {
                    leaves.add(node.type)
                }
            }

            for (i in 0 until node.childCount) {
                traverse(node.getChild(i)!!)
            }
        }

        traverse(tree.rootNode)
        return leaves.joinToString(" ")
    }

    override fun getParseTree(code: String): TSNode {
        source = code
        val parser = TSParser()
        val javaLang: TSLanguage = TreeSitterJava()
        parser.setLanguage(javaLang)
        val tree = parser.parseString(null, code)
        return tree.rootNode
    }


    private var sink: Int = 0


    override fun measureParse(code: String): Long {
        return measureNanoTime {
            val tree = getParseTree(code)
            sink += tree.hashCode() % 10
        }
    }

    override fun equals(node1: TSNode, node2: TSNode): Boolean {
        if (node1 === node2) return true
        if (node1.type != node2.type) return false
        if (node1.grammarType != node2.grammarType) return false
        if (node1.isError != node2.isExtra) return false
        if (node1.childCount != node2.childCount) return false
        if (isJavaIdentifierOrLiteral(node1.type)) {
            val text1 = getNodeText(node1, source)
            val text2 = getNodeText(node2, source)
            if (text1 != text2) return false
        }
        // If node is identifier or literal, compare text too


        return true
    }

    private fun isJavaIdentifierOrLiteral(nodeType: String): Boolean {
        return nodeType == "identifier" || nodeType == "decimal_integer_literal" || nodeType == "string_literal" || nodeType == "character_literal" || nodeType == "floating_point_literal"
    }

    private fun getNodeText(node: TSNode, source: String): String {
        val bytes = source.toByteArray(Charsets.UTF_8)
        val start = node.startByte
        val end = node.endByte
        return bytes.copyOfRange(start, end).toString(Charsets.UTF_8)
    }

    override fun getGraphFromTree(code: String): Pair<Graph<TSNode, DefaultEdge>, TSNode> {
        val graph: Graph<TSNode, DefaultEdge> = SimpleGraph(
            DefaultEdge::class.java
        )
        val rootNode = getParseTree(code)
        traverse(rootNode, null, graph)
        return Pair(graph, rootNode)
    }


    private fun traverse(
        node: TSNode, parent: TSNode?, graph: Graph<TSNode, DefaultEdge>
    ) {
        graph.addVertex(node)
        if (parent != null) {
            graph.addEdge(parent, node)
        }

        for (i in 0..<node.childCount) {
            val child = node.getChild(i)
            if (child is TSNode) {
                traverse(child, node, graph)
            }
        }
    }

}
