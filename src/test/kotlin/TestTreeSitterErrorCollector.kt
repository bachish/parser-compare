import measure.ParseError
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import parsers.AnalyzerType
import parsers.IRecoveryAnalyzer
import parsers.ParserFactory

class TestTreeSitterErrorCollector : IErrorCollectorTest{

    override fun getParser(): IRecoveryAnalyzer<*> = ParserFactory.create(AnalyzerType.TreeSitterAnalyzer)

    @Test
    fun testJava8ErrorCollector() {
        collectJavaError(identifierExpected, ParseError.SEMICOLON_EXPECTED)
        collectJavaError(missingOpenBracket, ParseError.OPEN_BRACKET_EXPECTED)
        collectJavaError(notAStatement, ParseError.NOT_A_STATEMENT)
    }


    @Disabled
    fun testOne() {
        // some strange error
        collectJavaError(missingArrow, ParseError.ARROW_EXPECTED)

        //15 different tokens expected!
        collectJavaError(notAStatement, ParseError.NOT_A_STATEMENT)
    }


}