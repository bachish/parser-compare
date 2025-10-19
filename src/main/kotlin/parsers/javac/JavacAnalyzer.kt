@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package parsers.javac

import com.sun.source.tree.*
import com.sun.source.util.JavacTask
import com.sun.source.util.TreeScanner
import com.sun.tools.javac.api.ClientCodeWrapper
import com.sun.tools.javac.api.JavacTool
import com.sun.tools.javac.tree.JCTree
import measure.ErrorInfo
import measure.ParseError
import measure.ParseErrorType
import measure.UNKNOWN_ERROR
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph
import org.jgrapht.nio.dot.DOTExporter
import parsers.IRecoveryAnalyzer
import java.io.File
import java.io.StringWriter
import java.net.URI
import javax.tools.*
import kotlin.system.measureNanoTime


class JavacAnalyzer : IRecoveryAnalyzer<String, Tree> {
    override fun getLexerTokens(code: String): List<String> {
        TODO("Not yet implemented")
    }

    override fun getParserTokens(code: String): List<String> {
        TODO("Not yet implemented")
    }

    override fun getErrors(code: String): List<ErrorInfo> {
        val compiler = JavacTool.create()
        val diagnostics = DiagnosticCollector<JavaFileObject>()
        val fileManager = compiler.getStandardFileManager(diagnostics, null, null)

        val compilationUnits =
            listOf(object : SimpleJavaFileObject(URI.create("string:///Test.java"), JavaFileObject.Kind.SOURCE) {
                override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence = code
            })

        val task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits)
        task.call()
        return diagnostics.diagnostics.mapNotNull { getErrorInfo(it) }.toList()
    }


    override fun getParseTree(code: String): Tree {
        val compiler = JavacTool.create() // Use JavacTool instead of ToolProvider
        val diagnostics = DiagnosticCollector<JavaFileObject>()
        val fileManager = compiler.getStandardFileManager(diagnostics, null, null)

        val compilationUnits =
            listOf(object : SimpleJavaFileObject(URI.create("string:///Test.java"), JavaFileObject.Kind.SOURCE) {
                override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence = code
            })

        val task = compiler.getTask(
            null, fileManager, diagnostics, null, null, compilationUnits
        ) as JavacTask

        val asts: Iterable<CompilationUnitTree> = task.parse() // Parse to get AST
        return asts.toList()[0]
    }


    private fun getErrorInfo(diagnostic: Diagnostic<*>): ErrorInfo? {
        if (diagnostic.kind != Diagnostic.Kind.ERROR) {
            return null
        }
        val errorInfo = when (diagnostic) {
            is ClientCodeWrapper.DiagnosticSourceUnwrapper -> {
                when (diagnostic.d.code) {
                    "compiler.err.expected" -> {
                        val args = diagnostic.d.args
                        if (args.size != 1) {
                            ErrorInfo(UNKNOWN_ERROR)
                        }
                        else {
                            val arg = args[0].toString().removeSurrounding("'")
                            ErrorInfo(ParseError(ParseErrorType.REMOVED_TOKEN, arg))
                        }
                    }

                    "compiler.err.expected2" -> {
                        ErrorInfo(ParseError(ParseErrorType.REMOVED_TOKEN, "VARIANT_NUMBER_IS_2"))
                    }

                    "compiler.err.premature.eof" -> ErrorInfo(
                        ParseError(ParseErrorType.UNEXPECTED_EOF, "eof"), diagnostic.toString()
                    )

                    else -> ErrorInfo(UNKNOWN_ERROR)
                }
            }

            else -> ErrorInfo(UNKNOWN_ERROR)
        }
        errorInfo.line = diagnostic.lineNumber
        errorInfo.col = diagnostic.columnNumber
        errorInfo.msg = diagnostic.toString()
        return errorInfo
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
        URI.create("file:///${file.absolutePath.replace("\\", "/")}"), JavaFileObject.Kind.SOURCE
    ) {
        private val content: String = file.readText()
        override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence = content
    }

    override fun equals(node1: Tree, node2: Tree): Boolean {
        // Compare kind
        if (node1.kind != node2.kind) return false
        if (node1.javaClass != node2.javaClass) return false

        // Compare terminal-like nodes by text/value
        when (node1) {
            is IdentifierTree -> {
                if (node2 !is IdentifierTree) return false
                if (node1.name != node2.name) return false
            }

            is ClassTree -> {
                if (node2 !is ClassTree) return false
                if (node1.simpleName.toString() != node2.simpleName.toString()) return false
                if (node1.members.size != node2.members.size) return false
            }

            is LiteralTree -> {
                if (node2 !is LiteralTree) return false
                if (node1.value != node2.value) return false
            }

            is PrimitiveTypeTree -> {
                if (node2 !is PrimitiveTypeTree) return false
                if (node1.primitiveTypeKind != node2.primitiveTypeKind) return false
            }

            is MemberSelectTree -> {
                if (node2 !is MemberSelectTree) return false
                if (node1.identifier != node2.identifier) return false
            }
        }
        return true
    }

    fun printGraphAsDot(graph: Graph<Tree, DefaultEdge>) {
        val exporter = DOTExporter<Tree, DefaultEdge>()  // No arguments here!
        exporter.setVertexIdProvider { node -> node.kind.toString() + node.hashCode() }
        val writer = StringWriter()
        exporter.exportGraph(graph, writer)
        println(writer.toString())
    }

    override fun getGraphFromTree(code: String): Pair<Graph<Tree, DefaultEdge>, Tree> {
        val tree = getParseTree(code)
        val graphBuilder = GraphBuilderScanner()
        tree.accept(graphBuilder, tree)
        val graph = graphBuilder.graph
        return Pair(graph, tree)
    }


    fun printAst(tree: Tree) {
        tree.accept(TreePrinterScanner(), null)
    }

    class GraphBuilderScanner : TreeScanner<Void, Tree?>() {
        val graph: Graph<Tree, DefaultEdge> = SimpleGraph(DefaultEdge::class.java)
        override fun scan(node: Tree?, parent: Tree?): Void? {
            if (node == null) {
                return null
            }
            if(!graph.addVertex(node)) {
                //in  cases whith ellpsis, first params with same type and error before it
                // javac use same nodes for different nodes (ellipsis presents as two arguments
                // and both use same Indend and Modifier childs).
                //see example for code
                /**
                public interface C { {
                void foo(String s, String... b);
                **/
                if(node is JCTree) {
                    var duplicateNode = node.clone()
                    graph.addVertex(duplicateNode as Tree?)
                    if (parent != null) {
                        graph.addVertex(parent)
                        graph.addEdge(parent, duplicateNode)
                    }
                    return null
                }

            }
            if (parent != null) {
                graph.addVertex(parent)
                graph.addEdge(parent, node)
            }
            super.scan(node, node)
            return null
        }
    }

    class TreePrinterScanner : TreeScanner<Void, Void>() {
        private var indent = 0
        private fun printIndent() {
            repeat(indent) { print("  ") }
        }

        override fun scan(node: Tree?, p: Void?): Void? {
            if (node != null) {
                printIndent()
                println("${node.kind}: $node")
                indent++
                super.scan(node, p)
                indent--
            }
            return null
        }
    }

}