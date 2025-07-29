import measure.ParseError
import parsers.IRecoveryAnalyzer
import kotlin.test.assertEquals

interface IErrorCollectorTest {
    fun getParser(): IRecoveryAnalyzer<*>
    fun collectJavaError(code: String, error: ParseError) {
        var parser = getParser()
        val errors = parser.getErrors(code)
        assertEquals(1, errors.size)
        assertEquals(error, errors.get(0).type)
    }
}