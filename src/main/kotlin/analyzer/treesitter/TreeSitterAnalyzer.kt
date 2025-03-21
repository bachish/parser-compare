
package analyzer.treesitter

import analyzer.IRecoveryAnalyzer
import jflex.JavaScanner
import jflex.JavaToken
import jflex.TreeSitterLexer
import org.treesitter.TSNode
import org.treesitter.TSParser
import org.treesitter.TreeSitterJava
import java.io.StringReader

class TreeSitterAnalyzer : IRecoveryAnalyzer<Int> {
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
}
// Пример использования
fun main() {
    val analyzer = TreeSitterAnalyzer()
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

    val code = "int main(){z;}"

    val lexerTokens = analyzer.getLexerTokens(code)
    val parserTokens = analyzer.getParserTokens(code)

    println("Lexer Tokens (JFlex): $lexerTokens")
    println("Parser Tokens (Tree-sitter): $parserTokens")
    println("Similarity: ${analyzer.calculateSimilarity(code)}")
}