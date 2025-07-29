package parsers.antlr

import antlr.java8.Java8Lexer
import antlr.java8.Java8Parser
import org.antlr.v4.runtime.CodePointCharStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.ParserRuleContext
import parsers.CollectedErrorListener

// Реализация для Java8 с использованием ANTLR
class AntlrJava8Analyzer : AntlrAnalyzer<Java8Parser>() {
    override fun getLexer(code: CodePointCharStream): Lexer {
        return Java8Lexer(code)
    }

    override fun getParser(tokenStream: CommonTokenStream): Java8Parser {
        return Java8Parser(tokenStream)
    }

    override fun getExcludedTokens(): Set<Int> {
        return setOf(Java8Lexer.WS, Java8Lexer.COMMENT, Java8Lexer.LINE_COMMENT, Java8Lexer.EOF)
    }

    override fun getCompilationUnit(parser: Java8Parser): ParserRuleContext {
        return parser.compilationUnit()
    }

    override fun getErrorListener(): CollectedErrorListener = Java8ErrorListener()

    class Java8ErrorListener : CollectedErrorListener() {
        override fun getSemi(): Int = Java8Lexer.SEMI
        override fun getLBrace(): Int = Java8Lexer.LBRACE
        override fun getArrow(): Int = Java8Lexer.ARROW
    }

}
