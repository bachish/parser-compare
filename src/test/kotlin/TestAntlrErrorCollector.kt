import antlr.java.JavaLexer
import measure.*
import org.junit.jupiter.api.Test
import parsers.AnalyzerType
import parsers.IRecoveryAnalyzer
import parsers.ParserFactory
import parsers.antlr.AntlrJavaAnalyzer
import kotlin.test.assertEquals

class TestAntlrErrorCollector : IErrorCollectorTest{

    override fun getParser(): IRecoveryAnalyzer<*, *> = ParserFactory.create(AnalyzerType.AntlrJavaAnalyzer)

    @Test
    fun testJAva8ErrorCollector() {
        collectJavaError(missingSemicolon, MISSING_SEMICOLON)
        collectJavaError(missingArrow, MISSING_ARROW)
        collectJavaError(additionalBracket, ParseError(ParseErrorType.REMOVED_TOKEN, "VARIANT_NUMBER_IS_45"))
        collectJavaError(missingOpenBrace, ParseError(ParseErrorType.REMOVED_TOKEN, ")"))
    }

    @Test
    fun testCantDetect(){
        assertNoError(notAStatement)
    }

    @Test
    fun codeToTokenTest(){
        val errorListener = AntlrJavaAnalyzer.JavaErrorListener()
        assertEquals(";", errorListener.getTokenView(JavaLexer.SEMI))
    }

    @Test
    fun testAntlrTed(){
        val analyzer = getParser()
        assertEquals(0.0, analyzer.getTreeEditDistance(correctCode, correctCode))
        assertEquals(1.0, analyzer.getTreeEditDistance("class Main{}", "class Foo{}"))
        assertEquals(4.0, analyzer.getTreeEditDistance("class Main {int x = 12}", "class Main {int x = 12};"))
    }

    @Test
    fun cascadeErrorsTest(){
        findCascadeErrors(complicatedMissingRPar)
        findCascadeErrors(missedRParInMethodCall)
    }

    val code = """
       
    """.trimIndent()
}