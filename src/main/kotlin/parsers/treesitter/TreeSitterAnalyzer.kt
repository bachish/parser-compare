package parsers.treesitter

import jflex.JavaScanner
import jflex.JavaToken
import jflex.TreeSitterLexer
import measure.ErrorInfo
import measure.ParseError
import org.treesitter.TSLanguage
import org.treesitter.TSNode
import org.treesitter.TSParser
import org.treesitter.TreeSitterJava
import parsers.IRecoveryAnalyzer
import java.io.StringReader
import kotlin.system.measureNanoTime


class TreeSitterAnalyzer() : IRecoveryAnalyzer<Int> {
    // Токены от лексера (JFlex Scanner)
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
        val parser = TSParser()
        val javaLang: TSLanguage = TreeSitterJava()
        parser.setLanguage(javaLang)
        val tree = parser.parseString(null, code)
        val rootNode = tree.getRootNode()
        val errorNodes = getErrorNodes(rootNode)
        return errorNodes.map { getErrorInfo(it) }
    }

    private fun getErrorInfo(errorNode: TSNode): ErrorInfo {
        if(errorNode.isMissing && errorNode.type == ";") {
            return ErrorInfo(ParseError.SEMICOLON_EXPECTED)
        }
        return ErrorInfo(ParseError.UNKNOWN)

    }

    private fun getErrorNodes(node: TSNode): List<TSNode> {
        val res = ArrayList<TSNode>()
        for(i in 0..< node.childCount) {
            val child = node.getChild(i)
            if(child.isError || child.isMissing) {
                res.add(child)
            }
            if(child.hasError()) {
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
    var parser:TSParser = TSParser()
    var sink:Int = 0

    init {
        parser.setLanguage(TreeSitterJava())
    }

    override fun measureParse(code: String): Long {
        return measureNanoTime {
            val tree = parser.parseString(null, code)
            sink += tree.hashCode() % 10
        }
    }
}
