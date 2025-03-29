package analyzer.antlr

import analyzer.IRecoveryAnalyzer
import antlr.java.JavaLexer
import antlr.java.JavaParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import kotlin.system.measureNanoTime

// Реализация для Java с использованием ANTLR (токены как пара текст-тип)
class AntlrJavaAnalyzer : IRecoveryAnalyzer<Int> {
    override fun getLexerTokens(code: String): List<Int> {
        val lexer = JavaLexer(CharStreams.fromString(code))
        lexer.removeErrorListeners()
        val tokenStream = CommonTokenStream(lexer)
        tokenStream.fill()
        val excludedTypes = setOf(JavaLexer.WS, JavaLexer.COMMENT, JavaLexer.LINE_COMMENT, JavaLexer.EOF)
        return tokenStream.tokens
            .filter { it.type !in excludedTypes }
            .map {  it.type }
    }

    override fun getParserTokens(code: String): List<Int> {
        val lexer = JavaLexer(CharStreams.fromString(code))
        lexer.removeErrorListeners()
        val tokenStream = CommonTokenStream(lexer)
        val parser = JavaParser(tokenStream)
        parser.removeErrorListeners()
        val strategy = LoggingErrorStrategy()
        parser.errorHandler = strategy
        val tree = parser.compilationUnit()
        val visitor = TokenVisitorJava()
        visitor.visit(tree)

        val excludedTypes = setOf(JavaLexer.WS, JavaLexer.COMMENT, JavaLexer.LINE_COMMENT, JavaLexer.EOF)
        return visitor.collectedTokens
            .filter { it !in strategy.extraTokens }
            .filter { it.type !in excludedTypes }
            .map {  it.type }
    }

    override fun measureParse(code: String): Long {
        val lexer = JavaLexer(CharStreams.fromString(code))
        lexer.removeErrorListeners()
        val tokenStream = CommonTokenStream(lexer)
        val parser = JavaParser(tokenStream)
        parser.removeErrorListeners()
        return measureNanoTime {
            val tree = parser.compilationUnit()
        }
    }
}

