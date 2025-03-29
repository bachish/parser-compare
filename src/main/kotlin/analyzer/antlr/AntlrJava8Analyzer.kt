package analyzer.antlr

import analyzer.IRecoveryAnalyzer
import antlr.java8.Java8Lexer
import antlr.java8.Java8Parser
import org.antlr.v4.runtime.*
import kotlin.system.measureNanoTime

// Реализация для Java8 с использованием ANTLR
class AntlrJava8Analyzer : IRecoveryAnalyzer<Int> {
    override fun getLexerTokens(code: String): List<Int> {
        val lexer = Java8Lexer(CharStreams.fromString(code))
        lexer.removeErrorListeners()
        val tokenStream = CommonTokenStream(lexer)
        tokenStream.fill()
        val excludedTypes = setOf(Java8Lexer.WS, Java8Lexer.COMMENT, Java8Lexer.LINE_COMMENT, Java8Lexer.EOF)
        return tokenStream.tokens
            .filter { it.type !in excludedTypes }
            .map { it.type }
    }

    override fun getParserTokens(code: String): List<Int> {
        val lexer = Java8Lexer(CharStreams.fromString(code))
        lexer.removeErrorListeners()
        val tokenStream = CommonTokenStream(lexer)
        val parser = Java8Parser(tokenStream)
        parser.removeErrorListeners()
        val strategy = LoggingErrorStrategy()
        parser.errorHandler = strategy
        val tree = parser.compilationUnit()
        val visitor = TokenVisitorJava8()
        visitor.visit(tree)

        val excludedTypes = setOf(Java8Lexer.WS, Java8Lexer.COMMENT, Java8Lexer.LINE_COMMENT, Java8Lexer.EOF)
        return visitor.collectedTokens
            .filter { it !in strategy.extraTokens }
            .filter { it.type !in excludedTypes }
            .map { it.type }
    }
    override fun measureParse(code: String): Long {
        val lexer = Java8Lexer(CharStreams.fromString(code))
        lexer.removeErrorListeners()
        val tokenStream = CommonTokenStream(lexer)
        val parser = Java8Parser(tokenStream)
        parser.removeErrorListeners()
        return measureNanoTime {
            val tree = parser.compilationUnit()
        }
    }
}
