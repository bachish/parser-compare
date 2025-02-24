import org.treesitter.TSParser
import org.treesitter.TreeSitterJava


fun main() {
    val parser = TSParser()
    parser.setLanguage(TreeSitterJava())

    val code = """
        public class Test {
            public static void main(String[] args) 
                System.out.println("Hello World!");
            }
        }
    """.trimIndent()

    val tree = parser.parseString(null, code)
    val dotString = captureDotGraph(tree)
    renderDotGraph(dotString)
}
