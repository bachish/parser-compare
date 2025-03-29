package analyzer.javac

import analyzer.IRecoveryAnalyzer
import com.sun.source.util.JavacTask
import java.io.File
import java.net.URI
import javax.tools.DiagnosticCollector
import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject
import javax.tools.ToolProvider
import kotlin.system.measureNanoTime

class JavacAnalyzer : IRecoveryAnalyzer<String> {
    override fun getLexerTokens(code: String): List<String> {
        TODO("Not yet implemented")
    }

    override fun getParserTokens(code: String): List<String> {
        TODO("Not yet implemented")
    }

    override fun measureParse(file: File): Long {
        val compiler = ToolProvider.getSystemJavaCompiler()
        val diagnostics = DiagnosticCollector<JavaFileObject>()
        val fileObject = JavaSourceFromFile(file)
        val task = compiler.getTask(null, null, diagnostics, null, null, listOf(fileObject)) as JavacTask

        // Измеряем только время парсинга
        return measureNanoTime {
            try {
                task.parse()
            } catch (e: Throwable) {
                // Обработка ошибок, если нужно
            }
        }
    }

    private class JavaSourceFromFile(file: File) :
        SimpleJavaFileObject(URI.create("file:///${file.absolutePath.replace("\\", "/")}"), JavaFileObject.Kind.SOURCE) {
        private val content: String = file.readText()
        override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence = content
    }
}