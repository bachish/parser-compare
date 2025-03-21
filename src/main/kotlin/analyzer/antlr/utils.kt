package analyzer.antlr

import antlr.java.JavaLexer
import antlr.java.JavaParserBaseVisitor
import antlr.java8.Java8ParserBaseVisitor
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.RuleNode
import org.antlr.v4.runtime.tree.TerminalNode

class TokenVisitorJava8 : Java8ParserBaseVisitor<Unit>() {
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

class TokenVisitorJava : JavaParserBaseVisitor<Unit>() {
    val collectedTokens = mutableListOf<Token>()

    override fun visitTerminal(node: TerminalNode) {
        collectedTokens.add(node.symbol)
    }

    override fun visitErrorNode(node: ErrorNode) {
        val errorToken = CommonToken(Token.INVALID_TYPE).apply {
            text = "<error token>"
            line = node.symbol.line
            charPositionInLine = node.symbol.charPositionInLine
            channel = Token.DEFAULT_CHANNEL
        }
        collectedTokens.add(errorToken)
    }

    override fun visitChildren(node: RuleNode): Unit {
        if (node is ParserRuleContext && (node.children == null || node.children.isEmpty())) {
            val fakeToken = CommonToken(Token.INVALID_TYPE, "<missing some>")
            collectedTokens.add(fakeToken)
        }
        super.visitChildren(node)
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

