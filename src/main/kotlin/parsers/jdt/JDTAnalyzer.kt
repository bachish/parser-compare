package parsers.jdt


import parsers.IRecoveryAnalyzer
import jflex.JavaScanner
import jflex.JavaToken
import measure.ErrorInfo
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.CompilationUnit
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import java.io.File
import java.io.StringReader
import kotlin.system.measureNanoTime

class JDTAnalyzer : IRecoveryAnalyzer<Int, Void> {
    // 1. Токены от лексера (JFlex Scanner) для исходного кода
    override fun getLexerTokens(code: String): List<Int> {
        val scanner = JavaScanner(StringReader(code))
        val tokens = mutableListOf<Int>()
        var token: JavaToken
        while (scanner.yylex().also { token = it } != JavaToken.EOF) {
            tokens.add(token.ordinal) // Извлекаем тип токена как Int (ordinal из enum JavaToken)
        }
        return tokens
    }

    // 2. Токены от парсера (JDT) через восстановленный код
    override fun getParserTokens(code: String): List<Int> {
        // Парсим код с помощью JDT
        val parser = ASTParser.newParser(AST.JLS21)
        parser.setSource(code.toCharArray())
        parser.setKind(ASTParser.K_COMPILATION_UNIT)
        val cu = parser.createAST(null) as CompilationUnit

//        // C:\data\java_src_files\10008483_351364663 - падает наив штука
//        // 3. Собираем корректный код через NaiveASTFlattener
//        val flattener = NaiveASTFlattener()
//        cu.accept(flattener)
//        val recoveredCode = flattener.result
        val recoveredCode = cu.toString()

        // 4. Разбираем восстановленный код нашим лексером
        val scanner = JavaScanner(StringReader(recoveredCode))
        val tokens = mutableListOf<Int>()
        var token: JavaToken
        while (scanner.yylex().also { token = it } != JavaToken.EOF) {
            tokens.add(token.ordinal) // Извлекаем тип токена как Int (ordinal из enum JavaToken)
        }
        return tokens
    }

    override fun getErrors(code: String): List<ErrorInfo> {
        TODO("Not yet implemented")
    }

    override fun getParseTree(code: String): Void {
        TODO("Not yet implemented")
    }

    override fun measureParse(code: String): Long {
        val parser = ASTParser.newParser(AST.JLS21)
        parser.setSource(code.toCharArray())
        parser.setKind(ASTParser.K_COMPILATION_UNIT)
        return measureNanoTime {
            val cu = parser.createAST(null) as CompilationUnit
        }
    }

    override fun equals(node1: Void, node2: Void): Boolean {
        TODO("Not yet implemented")
    }

    override fun getGraphFromTree(code: String): Pair<Graph<Void, DefaultEdge>, Void> {
        TODO("Not yet implemented")
    }
}

// Пример использования
fun main() {
    val analyzer = JDTAnalyzer()
//    val code = """
//import java.util.Random;
//import java.util.Scanner;
//
//public class Ejercicio {
//
//    """.trimIndent()

    val code = File("C:\\data\\java_src_files\\11529458_434215191").readText()

    val lexerTokens = analyzer.getLexerTokens(code)
    val parserTokens = analyzer.getParserTokens(code)

    println("code: $code")
    println("Lexer Tokens (JFlex): $lexerTokens")
    println("Parser Tokens (JDT): $parserTokens")
    println("Similarity: ${analyzer.calculateSimilarity(code)}")
}