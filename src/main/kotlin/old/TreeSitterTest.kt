package old
import org.treesitter.TSParser
import org.treesitter.TreeSitterJava
import java.io.File

// not a statement?
val missingId = """public class Simple {
int main(){
    a+ b +; }}"""

val errorDeleted = """public class Simple {
int main(){}}}
   """

val notStmt = "int main(){z;}"

fun main() {
    val parser = TSParser()
    parser.setLanguage(TreeSitterJava())

////    val code = """int main(){z;}""".trimIndent()
    val code = """
        class phs
        {
           rrrrrrrrrrrr (rrr[ ]) 
         {
             hyfhhhhh(h[]);
            }
        }
        
        
    """.trimIndent()
//    val filePath = "C:\\data\\java_src_files\\8821304_307656826" // Укажите путь к файлу
//    val code = File(filePath).readText()

    val tree = parser.parseString(null, code)
    val dotString = captureDotGraph(tree)
    renderDotGraph(dotString)
}
//
//import jflexLexer.Scanner
//import jflexLexer.JavaToken
//import jflexLexer.TreeSitterLexer
//import org.treesitter.TSNode
//import org.treesitter.TSParser
//import org.treesitter.TreeSitterJava
//import java.io.StringReader
//
//
//fun main() {
//    val code = """
//        @interface MyAnnotation {}
//        public class Test {
//            public static void main(String[] args) {
//                System.out.println("Hello World!"
//            }
//        }
//    """.trimIndent()
//
//    val leaves = getTreeSitterLeaves(code)
//    val lexer = TreeSitterLexer(StringReader(leaves))
//    var token1: JavaToken?
//    while (lexer.nextToken().also { token1 = it } != JavaToken.EOF) {
//        println(token1)
//    }
//
//    val input = "int x = 42;"
//
//
//    // Создаем Scanner из строки
//    val scanner: Scanner = Scanner(StringReader(input))
//
//
//    // Читаем токены до конца файла (EOF)
//    var token2: JavaToken
//    while ((scanner.yylex().also { token2 = it }) != JavaToken.EOF) {
//        println("Token: $token2")
//    }
//
//}
//
//fun getTreeSitterLeaves(code: String): String {
//    val parser = TSParser()
//    parser.setLanguage(TreeSitterJava())
//    val tree = parser.parseString(null, code)
//    val leaves = mutableListOf<String>()
//    fun traverse(node: TSNode) {
//        if (node.childCount == 0) {
//            leaves.add(node.type)
//        }
//        for (i in 0 until node.childCount) {
//            traverse(node.getChild(i)!!)
//        }
//    }
//    traverse(tree.rootNode)
//    return leaves.joinToString(" ")
//}