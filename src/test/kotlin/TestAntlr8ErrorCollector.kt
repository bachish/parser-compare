import measure.ParseError
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import parsers.AnalyzerType
import parsers.IRecoveryAnalyzer
import parsers.ParserFactory
import kotlin.test.assertEquals

class TestAntlr8ErrorCollector : IErrorCollectorTest {

    override fun getParser(): IRecoveryAnalyzer<*, *> = ParserFactory.create(AnalyzerType.AntlrJava8Analyzer)

    @Test
    fun testJAva8ErrorCollector() {
        collectJavaError(missingSemicolon, ParseError.SEMICOLON_EXPECTED)
        collectJavaError(missingArrow, ParseError.ARROW_EXPECTED)
    }

    @Test
    fun testBadRecovery() {
        // find more than one expected
        collectJavaError(missingOpenBracket, ParseError.MORE_THAT_ONE_EXPECTED)
        //15 different tokens expected!
        collectJavaError(notAStatement, ParseError.MORE_THAT_ONE_EXPECTED)
    }

    @Test
    fun testAntlrTed() {
        val analyzer = getParser()
        assertEquals(0.0, analyzer.getTreeEditDistance(correctCode, correctCode))
        assertEquals(1.0, analyzer.getTreeEditDistance("class Main{}", "class Foo{}"))
        assertEquals(4.0, analyzer.getTreeEditDistance("class Main {int x = 12}", "class Main {int x = 12};"))
    }
}