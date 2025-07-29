package parsers

import parsers.antlr.AntlrJava8Analyzer
import parsers.antlr.AntlrJavaAnalyzer
import parsers.javac.JavacAnalyzer
import parsers.jdt.JDTAnalyzer
import parsers.treesitter.TreeSitterAnalyzer

object ParserFactory {
    fun create(analyzerType: AnalyzerType): IRecoveryAnalyzer<*> {
        return when (analyzerType) {
            AnalyzerType.AntlrJava8Analyzer -> AntlrJava8Analyzer()
            AnalyzerType.AntlrJavaAnalyzer -> AntlrJavaAnalyzer()
            AnalyzerType.JavacAnalyzer -> JavacAnalyzer()
            AnalyzerType.JDTAnalyzer -> JDTAnalyzer()
            AnalyzerType.TreeSitterAnalyzer -> TreeSitterAnalyzer()
        }
    }

    fun create(analyzerTypeView: String): IRecoveryAnalyzer<*> {
        val analyzerType = runCatching { AnalyzerType.valueOf(analyzerTypeView) }.getOrElse {
            throw IllegalArgumentException("Unknown analyzer type: $analyzerTypeView")
        }
        return create(analyzerType)
    }
}

enum class AnalyzerType {
    AntlrJava8Analyzer,
    AntlrJavaAnalyzer,
    JavacAnalyzer,
    JDTAnalyzer,
    TreeSitterAnalyzer
}
