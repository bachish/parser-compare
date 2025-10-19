package parsers.antlr

import measure.*
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.InputMismatchException
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.misc.IntervalSet

abstract class CollectedErrorListener: BaseErrorListener() {
    abstract fun getTokenView(antlrCode: Int): String
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
        val parseError: ParseError = when(e) {
            is InputMismatchException -> {
                ParseError(
                    ParseErrorType.CHANGED_TOKEN,
                    "from:${e.offendingToken.text}, to:${getTokenView(e.expectedTokens.get(0))}"
                )
            }

            else -> {
                var expectedTokens: IntervalSet = (recognizer as Parser).expectedTokens
                if (e != null) {
                    expectedTokens = e.expectedTokens
                }
                if (expectedTokens.size() != 0) {
                    if (expectedTokens.size() > 1) {
                        ParseError(ParseErrorType.REMOVED_TOKEN, ("VARIANT_NUMBER_IS_${expectedTokens.size()}"))
                    } else {
                        val expectedToken = expectedTokens.get(0)
                        ParseError(ParseErrorType.REMOVED_TOKEN, getTokenView(expectedToken))
                    }
                }
                else {
                    UNKNOWN_ERROR
                }
            }
        }
        val errorInfo = ErrorInfo(parseError,msg, line.toLong(), charPositionInLine.toLong() )
        syntaxErrors.add(errorInfo)

    }
}