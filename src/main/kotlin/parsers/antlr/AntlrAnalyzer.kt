package parsers.antlr

import antlr.java.JavaLexer
import measure.ErrorInfo
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.RuleNode
import org.antlr.v4.runtime.tree.TerminalNode
import parsers.CollectedErrorListener
import parsers.IRecoveryAnalyzer
import kotlin.system.measureNanoTime

// Реализация для Java с использованием ANTLR (токены как пара текст-тип)
abstract class AntlrAnalyzer<ParserType : Parser> : IRecoveryAnalyzer<Int> {
    protected var strategy: LoggingErrorStrategy? = null
    abstract fun getLexer(code: CodePointCharStream): Lexer
    abstract fun getParser(tokens: CommonTokenStream): ParserType
    abstract fun getExcludedTokens(): Set<Int>

    override fun getErrors(code: String): List<ErrorInfo> {
        val parserResult = getParseTreeWithErrors(code)
        val visitor = Visitor()
        visitor.visit(parserResult.tree)
        return parserResult.listener!!.syntaxErrors
    }

    fun getParseTreeWithErrors(code: String): AntlrParserResult {
        val lexer = getLexer(CharStreams.fromString(code))
        lexer.removeErrorListeners()
        val tokenStream = CommonTokenStream(lexer)
        val parser = getParser(tokenStream)
        parser.removeErrorListeners()
        val errorListener = getErrorListener()
        parser.addErrorListener(errorListener)
        strategy = LoggingErrorStrategy()
        parser.errorHandler = strategy
        val tree = getCompilationUnit(parser)
        return AntlrParserResult(tree, parser, errorListener)
    }

    abstract fun getCompilationUnit(parser: ParserType): ParserRuleContext

    fun getParseTree(code: String): AntlrParserResult {
        val lexer = getLexer(CharStreams.fromString(code))
        lexer.removeErrorListeners()
        val tokenStream = CommonTokenStream(lexer)
        val parser = getParser(tokenStream)
        parser.removeErrorListeners()
        val tree = getCompilationUnit(parser)
        return AntlrParserResult(tree, parser, null)
    }

    abstract fun getErrorListener(): CollectedErrorListener

    override fun getLexerTokens(code: String): List<Int> {
        val lexer = getLexer(CharStreams.fromString(code))
        lexer.removeErrorListeners()
        val tokenStream = CommonTokenStream(lexer)
        tokenStream.fill()
        val excludedTypes = getExcludedTokens()
        return tokenStream.tokens.filter { it.type !in excludedTypes }.map { it.type }
    }

    fun buildParser(code: String): ParserType {
        val lexer = JavaLexer(CharStreams.fromString(code))
        lexer.removeErrorListeners()
        val tokenStream = CommonTokenStream(lexer)
        val parser = getParser(tokenStream)
        parser.removeErrorListeners()
        parser.addErrorListener(getErrorListener())
        strategy = LoggingErrorStrategy()
        parser.errorHandler = strategy
        return parser
    }

    data class AntlrParserResult(
        val tree: ParserRuleContext, val parser: Parser, val listener: CollectedErrorListener? = null
    )

    override fun measureParse(code: String): Long {
        val lexer = JavaLexer(CharStreams.fromString(code))
        lexer.removeErrorListeners()
        return measureNanoTime {
            getParseTree(code)
        }
    }

    override fun getParserTokens(code: String): List<Int> {
        val parseTree = getParseTree(code).tree
        val visitor = Visitor()
        visitor.visit(parseTree)

        val excludedTypes = getExcludedTokens()
        return visitor.collectedTokens
            //.filter { it !in strategy!!.extraTokens }
            .filter { it.type !in excludedTypes }.map { it.type }
    }

    class LoggingErrorStrategy : DefaultErrorStrategy() {
        val extraTokens = mutableListOf<Token>()

        override fun reportUnwantedToken(recognizer: Parser) {
            // Получение текущего "лишнего" токена
            val unwantedToken = recognizer.currentToken
            extraTokens.add(unwantedToken)
            super.reportUnwantedToken(recognizer)
        }
    }

    class Visitor : AbstractParseTreeVisitor<Int>() {
        val collectedTokens = mutableListOf<Token>()
        val errorTokens = mutableListOf<Token>()

        override fun visitTerminal(node: TerminalNode): Int {
            collectedTokens.add(node.symbol)
            return 0
        }

        override fun visitErrorNode(node: ErrorNode): Int {
            val errorToken = CommonToken(Token.INVALID_TYPE).apply {
                text = "<error token>"
                line = node.symbol.line
                charPositionInLine = node.symbol.charPositionInLine
                channel = Token.DEFAULT_CHANNEL
            }
            collectedTokens.add(errorToken)
            errorTokens.add(errorToken)
            return 0
        }

        override fun visitChildren(node: RuleNode): Int {
            if (node is ParserRuleContext && (node.children == null || node.children.isEmpty())) {
                val fakeToken = CommonToken(Token.INVALID_TYPE, "<missing some>")
                collectedTokens.add(fakeToken)
            }
            super.visitChildren(node)
            return 0
        }
    }

}