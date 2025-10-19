import antlr.java8.Java8Lexer
import measure.*
import org.junit.jupiter.api.Test
import parsers.AnalyzerType
import parsers.IRecoveryAnalyzer
import parsers.ParserFactory
import parsers.antlr.AntlrJava8Analyzer
import kotlin.test.assertEquals

class TestAntlr8ErrorCollector : IErrorCollectorTest {

    override fun getParser(): IRecoveryAnalyzer<*, *> = ParserFactory.create(AnalyzerType.AntlrJava8Analyzer)

    @Test
    fun testJAva8ErrorCollector() {
        collectJavaError(missingSemicolon, MISSING_SEMICOLON)
        collectJavaError(missingArrow, MISSING_ARROW)
        collectJavaError(additionalBracket, ParseError(ParseErrorType.REMOVED_TOKEN, "VARIANT_NUMBER_IS_29"))
        collectJavaError(missingOpenBrace, ParseError(ParseErrorType.REMOVED_TOKEN, "VARIANT_NUMBER_IS_23"))
        collectJavaError(missedRParInMethodCall, ParseError(ParseErrorType.REMOVED_TOKEN, "VARIANT_NUMBER_IS_21"))
    }

    @Test
    fun codeToTokenTest(){
        val errorListener = AntlrJava8Analyzer.Java8ErrorListener()
        assertEquals(";", errorListener.getTokenView(Java8Lexer.SEMI))
        assertEquals(")", errorListener.getTokenView(Java8Lexer.RPAREN))
    }

    @Test
    fun testAntlrTed() {
        val analyzer = getParser()
        assertEquals(0.0, analyzer.getTreeEditDistance(correctCode, correctCode))
        assertEquals(1.0, analyzer.getTreeEditDistance("class Main{}", "class Foo{}"))
        assertEquals(4.0, analyzer.getTreeEditDistance("class Main {int x = 12}", "class Main {int x = 12};"))
    }

    @Test
    fun cascadeErrorsTest(){
        findCascadeErrors(complicatedMissingRPar)
    }

    @Test
    fun debugTest() {

    }

    val code =""
}