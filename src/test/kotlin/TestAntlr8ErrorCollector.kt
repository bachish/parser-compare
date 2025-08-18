import measure.MISSING_ARROW
import measure.MISSING_SEMICOLON
import org.junit.jupiter.api.Test
import parsers.AnalyzerType
import parsers.IRecoveryAnalyzer
import parsers.ParserFactory
import kotlin.test.assertEquals

class TestAntlr8ErrorCollector : IErrorCollectorTest {

    override fun getParser(): IRecoveryAnalyzer<*, *> = ParserFactory.create(AnalyzerType.AntlrJava8Analyzer)

    @Test
    fun testJAva8ErrorCollector() {
        collectJavaError(missingSemicolon, MISSING_SEMICOLON)
        collectJavaError(missingArrow, MISSING_ARROW)
    }

    @Test
    fun testAntlrTed() {
        val analyzer = getParser()
        assertEquals(0.0, analyzer.getTreeEditDistance(correctCode, correctCode))
        assertEquals(1.0, analyzer.getTreeEditDistance("class Main{}", "class Foo{}"))
        assertEquals(4.0, analyzer.getTreeEditDistance("class Main {int x = 12}", "class Main {int x = 12};"))
    }
}