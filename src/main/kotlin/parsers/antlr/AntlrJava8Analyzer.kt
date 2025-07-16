package parsers.antlr

import antlr.java.JavaLexer
import antlr.java8.Java8Lexer
import antlr.java8.Java8Parser
import measure.ErrorInfo
import measure.ParseError
import org.antlr.v4.runtime.*

// Реализация для Java8 с использованием ANTLR
class AntlrJava8Analyzer : AntlrAnalyzer() {
    override fun getLexer(code: CodePointCharStream): Lexer {
        return Java8Lexer(code)
    }

    override fun <ParserType : Parser> getParser(tokenStream: CommonTokenStream): ParserType {
        return Java8Parser(tokenStream) as ParserType
    }

    override fun getExcludedTokens(): Set<Int> {
        return setOf(Java8Lexer.WS, Java8Lexer.COMMENT, Java8Lexer.LINE_COMMENT, Java8Lexer.EOF)
    }

    override fun getParseTree(code: String): AntlrParserResult {
        val lexer = JavaLexer(CharStreams.fromString(code))
        lexer.removeErrorListeners()
        val tokenStream = CommonTokenStream(lexer)
        val parser = getParser(tokenStream) as Java8Parser
        parser.removeErrorListeners()
        val errorListener = AntlrJava8Analyzer.ErrorListener()
        parser.addErrorListener(errorListener)
        strategy = LoggingErrorStrategy()
        parser.errorHandler = strategy
        val tree = parser.compilationUnit()
        return AntlrParserResult(tree, parser, errorListener)
    }

    override fun getErrors(code: String): List<ErrorInfo> {
        var parserResult = getParseTree(code)
        val visitor = Visitor()
        visitor.visit(parserResult.tree)
        return parserResult.listener!!.syntaxErrors
    }

    class ErrorListener : BaseErrorListener() {
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
            var errorType = ParseError.UNKNOWN
            if (e != null) {
                var expectedTokens = e.expectedTokens
                if (expectedTokens.size() > 1) {
                    errorType = ParseError.MORE_THAT_ONE_EXPECTED
                } else {
                    val expectedToken = expectedTokens.get(0)
                    when (expectedToken) {
                        Java8Lexer.SEMI -> ParseError.SEMICOLON_EXPECTED
                        Java8Lexer.LBRACE -> ParseError.OPEN_BRACKET_EXPECTED
                        Java8Lexer.ARROW -> ParseError.ARROW_EXPECTED
                    }
                }
            }
            syntaxErrors.add(ErrorInfo(errorType, msg, line, charPositionInLine))
        }
    }

}
