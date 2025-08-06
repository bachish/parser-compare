import measure.ParseError
import org.junit.jupiter.api.Test
import parsers.AnalyzerType
import parsers.IRecoveryAnalyzer
import parsers.ParserFactory
import kotlin.test.assertEquals

class TestJavacErrorCollector : IErrorCollectorTest {


    override fun getParser() = ParserFactory.create(AnalyzerType.JavacAnalyzer)

    @Test
    fun testCollectableError() {
        collectJavaError(missingSemicolon, ParseError.SEMICOLON_EXPECTED)
        collectJavaError(missingArrow, ParseError.ARROW_EXPECTED)
        collectJavaError(missingOpenBracket, ParseError.OPEN_BRACKET_EXPECTED)
        collectJavaError(notAStatement, ParseError.NOT_A_STATEMENT)
    }

    @Test
    fun testTed(){
        val analyzer = getParser()
        assertEquals(0.0, analyzer.getTreeEditDistance(correctCode, correctCode))
      //  assertEquals(1.0, analyzer.getTreeEditDistance("class Main{}", "class Foo{}"))
      //  assertEquals(2.0, analyzer.getTreeEditDistance("class Main {int x = 12}", "class Main {int x = 12};"))
    }

}