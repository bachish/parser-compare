import parsers.fuzzing.IAntlrMutator
import parsers.fuzzing.MissedSemicolonMutator
import parsers.treesitter.TreeSitterAnalyzer
import old.JavacRunnerWithProgress
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

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

    @Test
    fun testUnicode(){
//        println('\u')
    }

    @Test
    fun testAntlrSimilarity(){
        var code = "class X {int ;}"
        code = "class X {int x = ;}"
        code = "class X {int x = a}"
        val antlr = MissedSemicolonMutator()
        val lt = antlr.getOriginalLexerTokens(code)
        val pt = antlr.getOriginalParserTokens(code)
        val s = antlr.calculateSimilarity(code)
    }

    @Test
    fun getJavacErrors(){
        var code = "class X {int ;}"
        code = "class X {int x = ;}"
        println(JavacRunnerWithProgress.getErrors(code))
    }

    @Test
    fun testSemicolonMutator3(){
        val sourceCode = """class X{i x = 12;}""".trimMargin()
        val expected = """class X{i x = 12}""".trimMargin()
        val mutator = MissedSemicolonMutator()
        assertEquals(expected, mutator.getMutedCode(sourceCode))
        assertEquals(IAntlrMutator.OracleResult.RECOVER_ERROR, mutator.getOracleResult(sourceCode))

    }

    @Test
    fun testSemicolonMutator(){
        val sourceCode = """class X{
            int x = 12;
            int y;
        }""".trimMargin()
        val expected = """class X{
            int x = 12
            int y;
        }""".trimMargin()
        val mutator = MissedSemicolonMutator()
        val mutedCode = mutator.getMutedCode(sourceCode)
        assertEquals(expected, mutator.getMutedCode(sourceCode))
        assertEquals(IAntlrMutator.OracleResult.RECOVER_ERROR, mutator.getOracleResult(sourceCode))

    }
    fun testSemicolonMutator2(){
        val sourceCode = """class X{
            int x = 12;;
            int y;
        }""".trimMargin()
        val expected = """class X{
            int x = 12;
            int y;
        }""".trimMargin()
        val mutator = MissedSemicolonMutator()
        assertEquals(expected, mutator.getMutedCode(sourceCode))
        assertEquals(IAntlrMutator.OracleResult.RECOVER_ERROR, mutator.getOracleResult(sourceCode))
    }

}