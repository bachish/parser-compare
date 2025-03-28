package analyzer.jdt


import analyzer.IRecoveryAnalyzer
import jflex.JavaScanner
import jflex.JavaToken
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.internal.core.dom.NaiveASTFlattener
import java.io.StringReader

class JDTAnalyzer : IRecoveryAnalyzer<Int> {
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
        val parser = ASTParser.newParser(AST.JLS8)
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

}

// Пример использования
fun main() {
    val analyzer = JDTAnalyzer()
    val code = """
import java.util.Random;
import java.util.Scanner;
 
public class Ejercicio {
 
    """.trimIndent()

    val lexerTokens = analyzer.getLexerTokens(code)
    val parserTokens = analyzer.getParserTokens(code)

    println("code: $code")
    println("Lexer Tokens (JFlex): $lexerTokens")
    println("Parser Tokens (JDT): $parserTokens")
    println("Similarity: ${analyzer.calculateSimilarity(code)}")
}