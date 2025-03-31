import analyzer.IRecoveryAnalyzer
import analyzer.antlr.AntlrJava8Analyzer
import analyzer.antlr.AntlrJavaAnalyzer
import analyzer.javac.JavacAnalyzer
import analyzer.jdt.JDTAnalyzer
import analyzer.treesitter.TreeSitterAnalyzer

// Фабрика для создания анализаторов
object AnalyzerFactory {
    fun <T> create(analyzerType: String): IRecoveryAnalyzer<T> {
        @Suppress("UNCHECKED_CAST")
        return when (analyzerType) {
            "AntlrJava8Analyzer" -> AntlrJava8Analyzer()
            "AntlrJavaAnalyzer" -> AntlrJavaAnalyzer()
            "JavacAnalyzer" -> JavacAnalyzer()
            "JDTAnalyzer" -> JDTAnalyzer()
            "TreeSitterAnalyzer" -> TreeSitterAnalyzer()
            else -> throw IllegalArgumentException("Unknown analyzer type: $analyzerType")
        } as IRecoveryAnalyzer<T>
    }
}