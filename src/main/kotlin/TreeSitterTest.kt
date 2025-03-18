//import org.treesitter.TSParser
//import org.treesitter.TreeSitterJava
//
//
//fun main() {
//    val parser = TSParser()
//    parser.setLanguage(TreeSitterJava())
//
//    val code = """
//public class Simple {
//    public static void main(String[] args) {
//        int x = '2';
//        x; // Not a statement
//    }
//}
//    """.trimIndent()
//
//    val tree = parser.parseString(null, code)
//    val dotString = captureDotGraph(tree)
//    renderDotGraph(dotString)
//}

import jflexLexer.JavaToken
import jflexLexer.TreeSitterLexer
import java.io.StringReader
import org.treesitter.TSParser
import org.treesitter.TSNode
import org.treesitter.TreeSitterJava

fun main() {
    val code = """
        @interface MyAnnotation {}
        public class Test {
            public static void main(String[] args) {
                System.out.println("Hello World!"
            }
        }
    """.trimIndent()

    val leaves = getTreeSitterLeaves(code)
    val lexer = TreeSitterLexer(StringReader(leaves))
    var token: JavaToken?
    while (lexer.nextToken().also { token = it } != JavaToken.EOF) {
        println(token)
    }
}

fun getTreeSitterLeaves(code: String): String {
    val parser = TSParser()
    parser.setLanguage(TreeSitterJava())
    val tree = parser.parseString(null, code)
    val leaves = mutableListOf<String>()
    fun traverse(node: TSNode) {
        if (node.childCount == 0) {
            leaves.add(node.type)
        }
        for (i in 0 until node.childCount) {
            traverse(node.getChild(i)!!)
        }
    }
    traverse(tree.rootNode)
    return leaves.joinToString(" ")
}