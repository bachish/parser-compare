package parsers.antlr

import antlr.java.JavaLexer
import antlr.java.JavaParser
import measure.ErrorInfo
import org.antlr.v4.runtime.*

class AntlrJavaAnalyzer : AntlrAnalyzer() {
    override fun getLexer(code: CodePointCharStream): Lexer {
        return JavaLexer(code)
    }

    override fun <ParserType : Parser> getParser(tokens: CommonTokenStream): ParserType {
        return JavaParser(tokens) as ParserType
    }

    override fun getExcludedTokens(): Set<Int> {
        return setOf(JavaLexer.WS, JavaLexer.COMMENT, JavaLexer.LINE_COMMENT, JavaLexer.EOF)
    }

    override fun getErrors(code: String): List<ErrorInfo> {
        TODO("Not yet implemented")
    }

    override fun getParseTree(code: String): AntlrParserResult {
        val parser = buildParser(code) as JavaParser
        val tree = parser.compilationUnit()
        return AntlrParserResult(tree, parser)
    }

}

