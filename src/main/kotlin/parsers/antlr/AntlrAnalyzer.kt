package parsers.antlr

import antlr.java.JavaLexer
import measure.ErrorInfo
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.*
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph
import parsers.IRecoveryAnalyzer
import kotlin.system.measureNanoTime

// Реализация для Java с использованием ANTLR (токены как пара текст-тип)
abstract class AntlrAnalyzer<ParserType : Parser> : IRecoveryAnalyzer<Int, ParseTree> {
    private var strategy: LoggingErrorStrategy? = null
    abstract fun getLexer(code: CodePointCharStream): Lexer
    abstract fun getParser(tokens: CommonTokenStream): ParserType
    abstract fun getExcludedTokens(): Set<Int>

    override fun getErrors(code: String): List<ErrorInfo> {
        val parserResult = getParseTreeWithErrors(code)
        val visitor = Visitor()
        visitor.visit(parserResult.tree)
        return parserResult.listener!!.syntaxErrors
    }

    private fun getParseTreeWithErrors(code: String): AntlrParserResult {
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

    override fun equals(node1: ParseTree, node2: ParseTree): Boolean {
        if (node1 === node2) return true
        if (node1 is TerminalNode && node2 is TerminalNode) {
            return node1.text == node2.text
        }
        if (node1 is TerminalNode || node2 is TerminalNode) {
            return false
        }
        if (node1 is ParserRuleContext && node2 is ParserRuleContext) {
            if (node1.ruleIndex != node2.ruleIndex) return false
        }
        if (node1.childCount != node2.childCount) return false
        return true
    }

    override fun getGraphFromTree(code: String): Pair<Graph<ParseTree, DefaultEdge>, ParseTree> {
        val graph: Graph<ParseTree, DefaultEdge> = SimpleGraph(
            DefaultEdge::class.java
        )
        val (treeRoot, _, _) = getParseResult(code)
        traverse(treeRoot, null, graph)
        return Pair(graph, treeRoot)
    }

    private fun traverse(
        node: ParseTree, parent: ParseTree?, graph: Graph<ParseTree, DefaultEdge>
    ) {
        graph.addVertex(node)
        if (parent != null) {
            graph.addEdge(parent, node)
        }

        for (i in 0..<node.childCount) {
            val child = node.getChild(i)
            traverse(child, node, graph)
        }
    }

    abstract fun getCompilationUnit(parser: ParserType): ParserRuleContext

    fun getParsedTree(code: String): ParseTree {
        return getParseResult(code).tree
    }

    fun getParseResult(code: String): AntlrParserResult {
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
            getParseResult(code)
        }
    }

    override fun getParserTokens(code: String): List<Int> {
        val parseTree = getParseResult(code).tree
        val visitor = Visitor()
        visitor.visit(parseTree)

        val excludedTypes = getExcludedTokens()
        return visitor.collectedTokens
            //.filter { it !in strategy!!.extraTokens }
            .filter { it.type !in excludedTypes }.map { it.type }
    }

    class LoggingErrorStrategy : DefaultErrorStrategy() {
        private val extraTokens = mutableListOf<Token>()

        override fun reportUnwantedToken(recognizer: Parser) {
            // Получение текущего "лишнего" токена
            val unwantedToken = recognizer.currentToken
            extraTokens.add(unwantedToken)
            super.reportUnwantedToken(recognizer)
        }
    }

    class Visitor : AbstractParseTreeVisitor<Int>() {
        val collectedTokens = mutableListOf<Token>()
        private val errorTokens = mutableListOf<Token>()

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