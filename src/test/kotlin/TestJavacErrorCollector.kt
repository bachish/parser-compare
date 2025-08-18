import measure.MISSING_ARROW
import measure.MISSING_OPEN_BRACKET
import measure.MISSING_SEMICOLON
import org.junit.jupiter.api.Test
import parsers.AnalyzerType
import parsers.ParserFactory
import kotlin.test.assertEquals

class TestJavacErrorCollector : IErrorCollectorTest {


    override fun getParser() = ParserFactory.create(AnalyzerType.JavacAnalyzer)

    @Test
    fun testCollectableError() {
        collectJavaError(missingSemicolon, MISSING_SEMICOLON)
        collectJavaError(missingArrow, MISSING_ARROW)
        collectJavaError(missingOpenBracket, MISSING_OPEN_BRACKET)
    }

    @Test
    fun testTed(){
        val analyzer = getParser()
        assertEquals(0.0, analyzer.getTreeEditDistance(correctCode, correctCode))
      //  assertEquals(1.0, analyzer.getTreeEditDistance("class Main{}", "class Foo{}"))
      //  assertEquals(2.0, analyzer.getTreeEditDistance("class Main {int x = 12}", "class Main {int x = 12};"))
    }

}