import measure.ParseError
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import parsers.AnalyzerType
import parsers.IRecoveryAnalyzer
import parsers.ParserFactory
import kotlin.test.assertEquals

class TestAntlrErrorCollector : IErrorCollectorTest{

    override fun getParser(): IRecoveryAnalyzer<*> = ParserFactory.create(AnalyzerType.AntlrJavaAnalyzer)

    @Test
    fun testJAva8ErrorCollector() {
        collectJavaError(missingSemicolon, ParseError.SEMICOLON_EXPECTED)
        collectJavaError(missingArrow, ParseError.ARROW_EXPECTED)
    }

    @Disabled
    fun testOne() {
        // find more than one expected
        collectJavaError(missingOpenBracket, ParseError.OPEN_BRACKET_EXPECTED)
        //15 different tokens expected!
        collectJavaError(notAStatement, ParseError.NOT_A_STATEMENT)
    }

}