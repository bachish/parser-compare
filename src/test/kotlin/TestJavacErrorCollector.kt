import measure.ParseError
import org.junit.jupiter.api.Test
import parsers.AnalyzerType
import parsers.IRecoveryAnalyzer
import parsers.ParserFactory

class TestJavacErrorCollector : IErrorCollectorTest {


    override fun getParser(): IRecoveryAnalyzer<*> = ParserFactory.create(AnalyzerType.JavacAnalyzer)

    @Test
    fun testCollectableError() {
        collectJavaError(missingSemicolon, ParseError.SEMICOLON_EXPECTED)
        collectJavaError(missingArrow, ParseError.ARROW_EXPECTED)
        collectJavaError(missingOpenBracket, ParseError.OPEN_BRACKET_EXPECTED)
        collectJavaError(notAStatement, ParseError.NOT_A_STATEMENT)
    }

}