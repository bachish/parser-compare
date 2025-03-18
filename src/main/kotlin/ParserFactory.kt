import antlr.java.JavaLexer
import antlr.java.JavaParser
import antlr.java8.Java8Lexer
import antlr.java8.Java8Parser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.TokenStream
import org.antlr.v4.runtime.tree.ParseTree

// Фабрика для выбора парсера
class ParserFactory {
    companion object {
        fun createLexer(language: String, code: String): Lexer = when (language) {
            "java" -> JavaLexer(CharStreams.fromString(code))
            "java8" -> Java8Lexer(CharStreams.fromString(code))
            else -> throw IllegalArgumentException("Unsupported language: $language")
        }

        fun createParser(language: String, tokenStream: TokenStream): Parser = when (language) {
            "java" -> JavaParser(tokenStream)
            "java8" -> Java8Parser(tokenStream)
            else -> throw IllegalArgumentException("Unsupported language: $language")
        }

        fun createParseTree(language: String, parser: Parser): ParseTree = when (language) {
            "java" -> (parser as JavaParser).compilationUnit()
            "java8" -> (parser as Java8Parser).compilationUnit()
            else -> throw IllegalArgumentException("Unsupported language: $language")
        }

        fun getRuleNames(parser: Parser): List<String> = when (parser) {
            is JavaParser -> parser.ruleNames.toList()
            is Java8Parser -> parser.ruleNames.toList()
            else -> throw IllegalArgumentException("Unsupported parser type")
        }
    }
}

//public interface IParser {
//    fun parse(input: String) : ArrayList<*>
//    fun getRuleNames(parser: Parser): List<String>
//    fun random(a: Int) = parse()
//}
//
//
//class Java8Parser : IParser{
//    val parser : Parser
//    override fun parse(input: String): ArrayList<*> {
//        TODO("Not yet implemented")
//    }
//
//    override fun getRuleNames(parser: Parser): List<String> {
//        parser.ruleNames.toList()
//    }
//
//}