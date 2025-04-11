
package analyzer.treesitter

import analyzer.IRecoveryAnalyzer
import jflex.JavaScanner
import jflex.JavaToken
import jflex.TreeSitterLexer
import old.captureDotGraph
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

    // Вспомогательная функция для получения листьев дерева TreeSitter
    private fun getTreeSitterLeaves(code: String): String {
        val parser = TSParser()
        parser.setLanguage(TreeSitterJava())
        val tree = parser.parseString(null, code)
        val leaves = mutableListOf<String>()


//        // На файле с xml разметкой для логитека рекурсия падает со стак овервлоу((((
//        // поэтому переделываем без рекурсии
    fun traverseStack(node: TSNode) {
        val stack = ArrayDeque<TSNode>()
        stack.addFirst(node)

        while (stack.isNotEmpty()) {
            val current = stack.removeFirst()

            if (current.childCount == 0) {
                // Терминальная нода: проверяем родителя, как было раньше
                if (current.parent.isError) {
                    leaves.add("error")
                } else {
                    leaves.add(current.type)
                }
            } else if (current.isError) {
                // Если это error-нода, проверяем, есть ли прямые терминальные потомки
                var hasTerminalChildren = false
                for (i in 0 until current.childCount) {
                    if (current.getChild(i)!!.childCount == 0) {
                        hasTerminalChildren = true
                        break
                    }
                }
                // Если у error-ноды нет прямых терминальных потомков, добавляем "error"
                if (!hasTerminalChildren) {
                    leaves.add("error")
                }
            }

            // Добавляем дочерние узлы в обратном порядке,
            // чтобы обрабатывать их в прямом порядке
            for (i in current.childCount - 1 downTo 0) {
                stack.addFirst(current.getChild(i)!!)
            }
        }
    }
//

        fun traverse(node: TSNode) {
            if (node.childCount == 0) {
                // Терминальная нода: проверяем родителя, как было раньше
                if (node.parent.isError) {
                    leaves.add("error")
                } else {
                    leaves.add(node.type)
                }
            } else if (node.isError) {
                // Если это error-нода, проверяем, есть ли прямые терминальные потомки
                var hasTerminalChildren = false
                for (i in 0 until node.childCount) {
                    if (node.getChild(i)!!.childCount == 0) {
                        hasTerminalChildren = true
                        break
                    }
                }
                // Если у error-ноды нет прямых терминальных потомков, добавляем "error"
                if (!hasTerminalChildren) {
                    leaves.add("error")
                }
            }

            // Рекурсивно обходим детей
            for (i in 0 until node.childCount) {
                traverse(node.getChild(i)!!)
            }
        }

        try {
            traverse(tree.rootNode) // Пробуем рекурсию
        } catch (e: StackOverflowError) {
            println("StackOverflowError occurred, switching to stack-based traversal")
            leaves.clear() // Очищаем листья
            traverseStack(tree.rootNode) // Переключаемся на итеративный подход
        }

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

    val score_0_class_expected = """
        class phs
        {
           rrrrrrrrrrrr (rrr[ ]) 
         {
             hyfhhhhh(h[]);
            }
        }
    """.trimIndent()

    val oldScore1Semi = """
        class HAPPY
        { Chatbot c;
            HAPPY(Chatbot cneu)
            { c= cneu
            }
        }
    """.trimIndent()

    val code  = oldScore1Semi

//    val code = """
//public class Test {
//    public static void main(String[] args) {{{{{
//        System.out.println("Hello World!");
//    }
//}
//"""

    val lexerTokens = analyzer.getLexerTokens(code)
    val parserTokens = analyzer.getParserTokens(code)


    println("code: $code")
    println("Lexer Tokens (JFlex):\n$lexerTokens")
    println("Parser Tokens (Tree-sitter):\n$parserTokens")
    println("Similarity: ${analyzer.calculateSimilarity(code)}")
}