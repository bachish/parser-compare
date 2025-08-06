import measure.ParseError
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import parsers.AnalyzerType
import parsers.IRecoveryAnalyzer
import parsers.ParserFactory
import kotlin.test.assertEquals

class TestTreeSitterErrorCollector : IErrorCollectorTest{

    override fun getParser() = ParserFactory.create(AnalyzerType.TreeSitterAnalyzer)

    @Test
    fun testJava8ErrorCollector() {
        collectJavaError(missingSemicolon, ParseError.SEMICOLON_EXPECTED)
    }


    @Test
    fun testBadDetection() {
        collectJavaError(identifierExpected, ParseError.UNKNOWN)
        collectJavaError(missingOpenBracket, ParseError.UNKNOWN)
        collectJavaError(missingArrow, ParseError.UNKNOWN)
    }

    @Test
    fun testMissingError(){
        assertNoError(notAStatement)
    }


    @Test
    fun testTed(){
        val analyzer = getParser()
        assertEquals(0.0, analyzer.getTreeEditDistance(correctCode, correctCode))
        assertEquals(1.0, analyzer.getTreeEditDistance("class Main{}", "class Foo{}"))
        assertEquals(2.0, analyzer.getTreeEditDistance("class Main {int x = 12}", "class Main {int x = 12};"))
    }

}