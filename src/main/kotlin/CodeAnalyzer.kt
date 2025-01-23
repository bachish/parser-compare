import antlr.java.JavaLexer
import antlr.java.JavaParserBaseVisitor
import antlr.java8.Java8ParserBaseVisitor
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.RuleNode
import org.antlr.v4.runtime.tree.TerminalNode
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.max

import kotlin.math.min



// Универсальный класс для анализа исходного кода
class CodeAnalyzer(private val language: String) {
    var numberOfSyntaxErrors: Int? = null
    var numberOfLexerErrors: Int? = null
    var distance: Int? = null
    var numberOfParsedTokens: Int? = null

    fun calculateSimilarity(file: File): Double {
        val code = Files.readString(Paths.get(file.absolutePath))
        return calculateSimilarity(code)
    }
    // Общая функция для создания лексера
    private fun createLexer(code: String): Lexer {
        val lexer = ParserFactory.createLexer(language, code)
        lexer.removeErrorListeners()
        return lexer
    }

    // Общая функция для создания парсера
    private fun createParser(tokenStream: CommonTokenStream): Parser {
        val parser = ParserFactory.createParser(language, tokenStream)
        parser.removeErrorListeners()
        return parser
    }

    // Общая функция для создания дерева разбора
    private fun createParseTree(code: String): ParseTree {
        val lexer = createLexer(code)
        val tokenStream = CommonTokenStream(lexer)
        val parser = createParser(tokenStream)
        return ParserFactory.createParseTree(language, parser)
    }

    fun calculateSimilarity(code: String): Double {
        val lexer = createLexer(code)

        // Создаем кастомный обработчик ошибок для лексера
        val lexerErrorListener = object : BaseErrorListener() {
            var errorCount = 0
            override fun syntaxError(
                recognizer: Recognizer<*, *>?,
                offendingSymbol: Any?,
                line: Int,
                charPositionInLine: Int,
                msg: String?,
                e: RecognitionException?
            ) {
                errorCount++ // Увеличиваем счетчик ошибок
            }
        }
        lexer.removeErrorListeners()
        lexer.addErrorListener(lexerErrorListener)

        val tokenStream = CommonTokenStream(lexer)
        val parser = createParser(tokenStream)

        val strategy = LoggingErrorStrategy()
        parser.errorHandler = strategy

        val tree = ParserFactory.createParseTree(language, parser)
        val visitor = TokenVisitor()
        visitor.visit(tree)

        val filteredTokens = visitor.collectedTokens.filter { token ->
            token !in strategy.extraTokens
        }

        val excludedTypes = setOf(JavaLexer.WS, JavaLexer.COMMENT, JavaLexer.LINE_COMMENT, JavaLexer.EOF)
        val originalTokens = tokenStream.tokens.filter { it.type !in excludedTypes }.map { it.text }
        val collectedTokens = filteredTokens.filter { it.type !in excludedTypes }.map { it.text }
        this.numberOfSyntaxErrors = parser.numberOfSyntaxErrors

        // Сохраняем количество ошибок лексера
        this.numberOfLexerErrors = lexerErrorListener.errorCount
        this.numberOfParsedTokens = collectedTokens.size

        return calculateLevenshteinSimilarity(originalTokens, collectedTokens)
    }


    // Функция для разбора файла
    fun parseFile(file: File): ParseTree {
        val code = Files.readString(Paths.get(file.absolutePath))
        return createParseTree(code)
    }

    // Функция для разбора кода
    fun parseCode(code: String): ParseTree {
        return createParseTree(code)
    }

    // Функция для разбора файла с возвращением парсера
    fun parseFileWithParser(file: File): Pair<ParseTree, Parser> {
        val code = Files.readString(Paths.get(file.absolutePath))
        return parseCodeWithParser(code)
    }

    // Функция для разбора кода с возвращением парсера
    fun parseCodeWithParser(code: String): Pair<ParseTree, Parser> {
        val lexer = createLexer(code)
        val tokenStream = CommonTokenStream(lexer)
        val parser = createParser(tokenStream)
        val tree = ParserFactory.createParseTree(language, parser)
        return tree to parser
    }


    private fun <T> calculateLevenshteinSimilarity(a: List<T>, b: List<T>): Double {
//        Надо памяти больше выделять -Xms4g -Xmx16g
//        (для этих все равно не хватает)
//        1981175_88001536, //bmv
//        45493781_1758766325,
//        48506454_1863037852, //бананы
//        8821304_307656826, //xml

        val maxLength = max(a.size, b.size)
        // Ограничение максимального размера для предотвращения Java heap space
//        if (maxLength > 50000) {
//            return -1.0  // Слишком большие данные для вычисления
//        }

//      Пробуем больше 50к токенов считать линейно
//        val distance = if (maxLength > 50000) levenshteinLine(a, b) else  levenshteinLine(a, b)
        val distance = levenshteinLine(a, b)

        this.distance = distance

        return if (maxLength > 0) 1 - (distance.toDouble() / maxLength) else 1.0
    }

    //линейный ливенштейн (медленное(или нет))
    private fun<T> levenshteinLine(lhs : List<T>, rhs : List<T>) : Int {
        if(lhs == rhs) { return 0 }
        if(lhs.isEmpty()) { return rhs.size }
        if(rhs.isEmpty()) { return lhs.size }

        val lhsLength = lhs.size + 1
        val rhsLength = rhs.size + 1

        var cost = Array(lhsLength) { it }
        var newCost = Array(lhsLength) { 0 }

        for (i in 1..rhsLength-1) {
            newCost[0] = i

            for (j in 1..lhsLength-1) {
                val match = if(lhs[j - 1] == rhs[i - 1]) 0 else 1

                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1

                newCost[j] = min(min(costInsert, costDelete), costReplace)
            }

            val swap = cost
            cost = newCost
            newCost = swap
        }

        return cost[lhsLength - 1]
    }

    private fun <T> levenshtein(a: List<T>, b: List<T>): Int {
        val dp = Array(a.size + 1) { IntArray(b.size + 1) }
        for (i in 0..a.size) dp[i][0] = i
        for (j in 0..b.size) dp[0][j] = j

        for (i in 1..a.size) {
            for (j in 1..b.size) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,       // Удаление
                    dp[i][j - 1] + 1,       // Вставка
                    dp[i - 1][j - 1] + cost // Замена
                )
            }
        }
        return dp[a.size][b.size]
    }


    // В классе CodeAnalyzer должен быть метод parse, который парсит файл без возвращения значений
    fun hollowParse(code: String) {
        val lexer = createLexer(code)
        val tokenStream = CommonTokenStream(lexer)
        val parser = createParser(tokenStream)
        val tree = ParserFactory.createParseTree(language, parser)
        val visitor = TokenVisitor()
        visitor.visit(tree)
    }

}


//class TokenVisitor : JavaParserBaseVisitor<Unit>() {
class TokenVisitor : Java8ParserBaseVisitor<Unit>() {
    val collectedTokens = mutableListOf<Token>()

    override fun visitTerminal(node: TerminalNode) {
        collectedTokens.add(node.symbol)
    }

    // штука, чтобы еррор токены тоже добавлялись
    override fun visitErrorNode(node: ErrorNode) {
        // Создаем фиктивный токен с пометкой "ERROR"
        val errorToken = CommonToken(Token.INVALID_TYPE).apply {
            text = "<error token>" // Текст фиктивного токена
            line = node.symbol.line // Присваиваем строку и позицию ошибки из исходного узла
            charPositionInLine = node.symbol.charPositionInLine
            channel = Token.DEFAULT_CHANNEL
        }

        collectedTokens.add(errorToken)
    }

    // Для обхода всех дочерних узлов
    override fun visitChildren(node: RuleNode): Unit {
        if (node is ParserRuleContext) {
            // Проверка наличия исключений и недостающих токенов
            if (node.children == null || node.children.isEmpty()) {
                // Если узел пустой, добавляем фиктивный токен, чтобы указать на недостающее выражение
                val fakeToken = CommonToken(JavaLexer.IDENTIFIER, "<missing some>")
                collectedTokens.add(fakeToken)
            }
        }
        return super.visitChildren(node)
    }

}

class LoggingErrorStrategy : DefaultErrorStrategy() {
    val extraTokens = mutableListOf<Token>()

    override fun reportUnwantedToken(recognizer: Parser) {
        // Получение текущего "лишнего" токена
        val unwantedToken = recognizer.currentToken
        extraTokens.add(unwantedToken)
        super.reportUnwantedToken(recognizer)
    }
}

