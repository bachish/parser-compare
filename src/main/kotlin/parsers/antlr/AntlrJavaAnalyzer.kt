package parsers.antlr

import antlr.java.JavaLexer
import antlr.java.JavaParser
import org.antlr.v4.runtime.CodePointCharStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.ParserRuleContext

class AntlrJavaAnalyzer : AntlrAnalyzer<JavaParser>() {
    override fun getLexer(code: CodePointCharStream): Lexer {
        return JavaLexer(code)
    }

    override fun getParser(tokens: CommonTokenStream): JavaParser {
        return JavaParser(tokens)
    }

    override fun getExcludedTokens(): Set<Int> {
        return setOf(JavaLexer.WS, JavaLexer.COMMENT, JavaLexer.LINE_COMMENT, JavaLexer.EOF)
    }

    override fun getCompilationUnit(parser: JavaParser): ParserRuleContext {
        return parser.compilationUnit()
    }

    override fun getErrorListener(): CollectedErrorListener {
        return JavaErrorListener()
    }

    class JavaErrorListener : CollectedErrorListener() {
        override fun getSemi(): Int = JavaLexer.SEMI
        override fun getLBrace(): Int = JavaLexer.LBRACE
        override fun getArrow(): Int = JavaLexer.ARROW
    }
}

