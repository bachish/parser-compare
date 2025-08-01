
package parsers.treesitter

import parsers.IRecoveryAnalyzer
import jflex.JavaScanner
import jflex.JavaToken
import jflex.TreeSitterLexer
import measure.ErrorInfo
import org.treesitter.TSNode
import org.treesitter.TSParser
import org.treesitter.TreeSitterJava
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
        TODO("Not yet implemented")
    }

    // Вспомогательная функция для получения листьев дерева TreeSitter
    private fun getTreeSitterLeaves(code: String): String {
        val parser = TSParser()
        parser.setLanguage(TreeSitterJava())
        val tree = parser.parseString(null, code)
        val leaves = mutableListOf<String>()


//        // На файле с xml разметкой для логитека рекурсия падает со стак овервлоу((((
//        // поэтому переделываем без рекурсии
//        fun traverse(node: TSNode) {
//            val stack = ArrayDeque<TSNode>()
//            stack.addFirst(node)
//
//            while (stack.isNotEmpty()) {
//                val current = stack.removeFirst()
//
//                if (current.childCount == 0) {
//                    if (current.parent.isError) {
//                        leaves.add("error")
//                    } else {
//                        leaves.add(current.type)
//                    }
//                }
//
//                // Добавляем дочерние узлы в обратном порядке,
//                // чтобы обрабатывать их в прямом порядке
//                for (i in current.childCount - 1 downTo 0) {
//                    stack.addFirst(current.getChild(i)!!)
//                }
//            }
//        }
//

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
// Пример использования
fun main() {
    val analyzer = TreeSitterAnalyzer()
//    val analyzer = AntlrJavaAnalyzer()
//    val code = """
//        @interface MyAnnotation {}
//        public class Test {
//            public static void main(String[] args) {
//                System.out.println("Hello World!");
//            }
//
//    """.trimIndent()

    // not a statement?
    val missingId = """
int main(){
    a+ b +; }}"""

    val errorDeleted = """
int main(){}}}
   """

    val code = """
public class Test {
    public static void main(String[] args) {{{{{
        System.out.println("Hello World!");
    }
}
"""

    val lexerTokens = analyzer.getLexerTokens(code)
    val parserTokens = analyzer.getParserTokens(code)

    println("code: $code")
    println("Lexer Tokens (JFlex): $lexerTokens")
    println("Parser Tokens (Tree-sitter): $parserTokens")
    println("Similarity: ${analyzer.calculateSimilarity(code)}")
}