import measure.MISSING_SEMICOLON
import measure.ParseError
import measure.ParseErrorType
import measure.UNKNOWN_ERROR
import org.junit.jupiter.api.Test
import parsers.AnalyzerType
import parsers.ParserFactory
import kotlin.test.assertEquals

class TestTreeSitterErrorCollector : IErrorCollectorTest{

    override fun getParser() = ParserFactory.create(AnalyzerType.TreeSitterAnalyzer)

    @Test
    fun testJava8ErrorCollector() {
        collectJavaError(missingSemicolon, MISSING_SEMICOLON)
        collectJavaError(additionalBracket, ParseError(ParseErrorType.REMOVED_TOKEN, "}"))
        collectJavaError(missingOpenBrace, ParseError(ParseErrorType.REMOVED_TOKEN, ")"))
    }


    @Test
    fun testBadDetection() {
        collectJavaError(identifierExpected, UNKNOWN_ERROR)
        collectJavaError(missingOpenBracket, UNKNOWN_ERROR)
        collectJavaError(missingArrow, UNKNOWN_ERROR)
        collectJavaError(missedRParInMethodCall, UNKNOWN_ERROR)
    }

    @Test
    fun testMissingError(){
        assertNoError(notAStatement)
    }

    @Test
    fun testCascadeError() {
        findCascadeErrors(complicatedMissingRPar)
    }

    @Test
    fun testTed(){
        val analyzer = getParser()
        assertEquals(0.0, analyzer.getTreeEditDistance(correctCode, correctCode))
        assertEquals(1.0, analyzer.getTreeEditDistance("class Main{}", "class Foo{}"))
        assertEquals(2.0, analyzer.getTreeEditDistance("class Main {int x = 12}", "class Main {int x = 12};"))
    }

    @Test
    fun debugTest() {

    }
    val code = """
   
""".trimIndent()
}