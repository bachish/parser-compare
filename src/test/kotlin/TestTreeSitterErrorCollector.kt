import measure.MISSING_SEMICOLON
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
    }


    @Test
    fun testBadDetection() {
        collectJavaError(identifierExpected, UNKNOWN_ERROR)
        collectJavaError(missingOpenBracket, UNKNOWN_ERROR)
        collectJavaError(missingArrow, UNKNOWN_ERROR)
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