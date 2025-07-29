package parsers

import measure.ErrorInfo
import measure.ParseError
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.misc.IntervalSet

abstract class CollectedErrorListener: BaseErrorListener() {
    abstract fun getSemi(): Int
    abstract fun getLBrace(): Int
    abstract fun getArrow(): Int
    val syntaxErrors: MutableList<ErrorInfo> = mutableListOf()
    override fun syntaxError(
        recognizer: Recognizer<*, *>?,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String?,
        e: RecognitionException?
    ) {
        super.syntaxError(recognizer, offendingSymbol, line, charPositionInLine, msg, e)
        var expectedTokens: IntervalSet = (recognizer as Parser).getExpectedTokens()
        if(e != null) {
            expectedTokens = e.expectedTokens
        }
        var errorType = ParseError.UNKNOWN
        if (expectedTokens.size() != 0) {
            if (expectedTokens.size() > 1) {
                errorType = ParseError.MORE_THAT_ONE_EXPECTED
            } else {
                val expectedToken = expectedTokens.get(0)
                errorType = when (expectedToken) {
                    getSemi() -> ParseError.SEMICOLON_EXPECTED
                    getLBrace() -> ParseError.OPEN_BRACKET_EXPECTED
                    getArrow() -> ParseError.ARROW_EXPECTED
                    else -> ParseError.UNKNOWN
                }
            }
        }
        syntaxErrors.add(ErrorInfo(errorType, msg, line, charPositionInLine))
    }
}