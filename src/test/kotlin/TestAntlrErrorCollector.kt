import measure.*
import org.junit.jupiter.api.Test
import parsers.AnalyzerType
import parsers.IRecoveryAnalyzer
import parsers.ParserFactory
import kotlin.test.assertEquals

class TestAntlrErrorCollector : IErrorCollectorTest{

    override fun getParser(): IRecoveryAnalyzer<*, *> = ParserFactory.create(AnalyzerType.AntlrJavaAnalyzer)

    @Test
    fun testJAva8ErrorCollector() {
        collectJavaError(missingSemicolon, MISSING_SEMICOLON)
        collectJavaError(missingArrow, MISSING_ARROW)
    }

    @Test
    fun testCantDetect(){
        assertNoError(notAStatement)

    }
    @Test
    fun testAntlrTed(){
        val analyzer = getParser()
        assertEquals(0.0, analyzer.getTreeEditDistance(correctCode, correctCode))
        assertEquals(1.0, analyzer.getTreeEditDistance("class Main{}", "class Foo{}"))
        assertEquals(4.0, analyzer.getTreeEditDistance("class Main {int x = 12}", "class Main {int x = 12};"))
    }
}