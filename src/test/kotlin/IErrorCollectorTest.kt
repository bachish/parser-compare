import measure.ParseError
import org.junit.jupiter.api.Assertions.assertNotEquals
import parsers.IRecoveryAnalyzer
import kotlin.test.assertEquals

interface IErrorCollectorTest {
    fun getParser(): IRecoveryAnalyzer<*, *>
    fun collectJavaError(code: String, error: ParseError) {
        var parser = getParser()
        val errors = parser.getErrors(code)
        assertEquals(1, errors.size)
        assertEquals(error, errors[0].type)
    }

    fun assertNoError(code: String) {
        var parser = getParser()
        val errors = parser.getErrors(code)
        assertEquals(0, errors.size)
    }

    fun assertNotEqualsTrees(code1: String, code2: String) {
        var analyzer = getParser()
        val diff = analyzer.getTreeEditDistance(code1, code2)
        assertNotEquals(0.0, diff)
    }
}