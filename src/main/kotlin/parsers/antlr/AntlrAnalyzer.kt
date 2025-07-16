package parsers.antlr

import antlr.java.JavaLexer
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.RuleNode
import org.antlr.v4.runtime.tree.TerminalNode
import parsers.IRecoveryAnalyzer
import kotlin.system.measureNanoTime

// Реализация для Java с использованием ANTLR (токены как пара текст-тип)
abstract class AntlrAnalyzer : IRecoveryAnalyzer<Int> {
    protected var strategy: LoggingErrorStrategy? = null
    abstract fun getLexer(code: CodePointCharStream): Lexer
    abstract fun <ParserType : Parser> getParser(tokens: CommonTokenStream): ParserType
    abstract fun getExcludedTokens(): Set<Int>
    abstract fun getParseTree(code: String): AntlrParserResult

    override fun getLexerTokens(code: String): List<Int> {
        val lexer = getLexer(CharStreams.fromString(code))
        lexer.removeErrorListeners()
        val tokenStream = CommonTokenStream(lexer)
        tokenStream.fill()
        val excludedTypes = getExcludedTokens()
        return tokenStream.tokens.filter { it.type !in excludedTypes }.map { it.type }
    }

    fun <ParserType : Parser> buildParser(code: String): ParserType {
        val lexer = JavaLexer(CharStreams.fromString(code))
        lexer.removeErrorListeners()
        val tokenStream = CommonTokenStream(lexer)
        val parser = getParser(tokenStream) as ParserType
        parser.removeErrorListeners()
        parser.addErrorListener(AntlrJava8Analyzer.ErrorListener())
        strategy = LoggingErrorStrategy()
        parser.errorHandler = strategy
        return parser
    }

    data class AntlrParserResult(val tree: ParserRuleContext,
                                 val parser: Parser, val listener: AntlrJava8Analyzer.ErrorListener? = null)

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