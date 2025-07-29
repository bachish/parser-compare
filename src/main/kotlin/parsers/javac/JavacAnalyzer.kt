@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")
package parsers.javac

import com.sun.source.util.JavacTask
import com.sun.tools.javac.api.ClientCodeWrapper
import measure.ErrorInfo
import measure.ParseError
import parsers.IRecoveryAnalyzer
import java.io.File
import java.net.URI
import javax.tools.*
import kotlin.system.measureNanoTime

class JavacAnalyzer : IRecoveryAnalyzer<String> {
    override fun getLexerTokens(code: String): List<String> {
        TODO("Not yet implemented")
    }

    override fun getParserTokens(code: String): List<String> {
        TODO("Not yet implemented")
    }

    override fun getErrors(code: String): List<ErrorInfo> {
        val compiler = ToolProvider.getSystemJavaCompiler()
        val diagnostics = DiagnosticCollector<JavaFileObject>()
        val fileManager = compiler.getStandardFileManager(diagnostics, null, null)

        val compilationUnits =
            listOf(object : SimpleJavaFileObject(URI.create("string:///Test.java"), JavaFileObject.Kind.SOURCE) {
                override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence = code
            })

        val task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits)
        task.call()
        return diagnostics.diagnostics.map { getErrorInfo(it) }.filterNotNull().toList()
    }

    fun getErrorInfo(diagnostic: Diagnostic<*>): ErrorInfo? {
        if (diagnostic.kind != Diagnostic.Kind.ERROR) {
            return null
        }
        return when(diagnostic) {
            is ClientCodeWrapper.DiagnosticSourceUnwrapper -> {
                when (diagnostic.d.code) {
                    "compiler.err.expected" -> {
                        val args = diagnostic.d.args
                        if(args.size != 1) {
                            return ErrorInfo(ParseError.UNKNOWN)
                        }
                        val arg = args[0].toString()
                        when(arg) {
                            "';'" -> ErrorInfo(ParseError.SEMICOLON_EXPECTED, diagnostic.getMessage(null))
                            "'{'" -> ErrorInfo(ParseError.OPEN_BRACKET_EXPECTED, diagnostic.getMessage(null))
                            "->" -> ErrorInfo(ParseError.ARROW_EXPECTED, diagnostic.getMessage(null))
                            else ->  ErrorInfo(ParseError.UNKNOWN, diagnostic.getMessage(null))
                        }
                    }
                    "compiler.err.not.stmt" -> {
                        ErrorInfo(ParseError.NOT_A_STATEMENT, diagnostic.getMessage(null))
                    }
                    else -> ErrorInfo(ParseError.UNKNOWN)
                }
            }
            else -> ErrorInfo(ParseError.UNKNOWN)
        }
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

    private class JavaSourceFromFile(file: File) : SimpleJavaFileObject(
        URI.create("file:///${file.absolutePath.replace("\\", "/")}"),
        JavaFileObject.Kind.SOURCE
    ) {
        private val content: String = file.readText()
        override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence = content
    }
}