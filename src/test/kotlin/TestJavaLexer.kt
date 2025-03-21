import analyzer.treesitter.TreeSitterAnalyzer
import org.junit.jupiter.api.Test

class TestJavaLexer {
    @Test
    fun test() {
        val analyzer = TreeSitterAnalyzer()
        analyzer.getLexerTokens("""
                { 
                    out.printf ("f is the pringf/format symbol for float/real #s\n\n");
                    
                    double decOne= 9.23423, decTwo = 7.34243, decThree = 34.324532;
                    out.printf ("%.2--%.2f--%.3f\", decOne, decTwo, decThree);
                    double dec = 5.3423;
                    out.println (String.format ("%.3f", dec));
                    out.println (String.format "%12.3f", dec));
                    out.println (String.format "%12.3f", dec));
                    out.println (String.format "%-7.3f", dec));
                }
            }
        """.trimIndent())
    }
}