import measure.*
import org.jgrapht.GraphTests
import org.junit.jupiter.api.Test
import parsers.AnalyzerType
import parsers.ParserFactory
import parsers.javac.JavacAnalyzer
import kotlin.test.assertEquals

class TestJavacErrorCollector : IErrorCollectorTest {


    override fun getParser() = ParserFactory.create(AnalyzerType.JavacAnalyzer)

    @Test
    fun testCollectableError() {
        collectJavaError(missingSemicolon, MISSING_SEMICOLON)
        collectJavaError(missingArrow, MISSING_ARROW)
        collectJavaError(missingOpenBracket, MISSING_OPEN_BRACKET)
        collectJavaError(additionalBracket, ParseError(ParseErrorType.UNEXPECTED_EOF, "eof"))
        collectJavaError(missingOpenBrace, ParseError(ParseErrorType.REMOVED_TOKEN, "VARIANT_NUMBER_IS_2"))
        collectJavaError(complicatedMissingRPar, ParseError(ParseErrorType.REMOVED_TOKEN, ")"))
        collectJavaError(missedRParInMethodCall, ParseError(ParseErrorType.REMOVED_TOKEN, "VARIANT_NUMBER_IS_2"))
    }

    @Test
    fun cascadeErrorsTest(){
        findCascadeErrors(addedLBracketAndBrokeJavac)
    }

    @Test
    fun debugTest() {

    }

    @Test
    fun testTed(){
        val analyzer = getParser()
        assertEquals(0.0, analyzer.getTreeEditDistance(correctCode, correctCode))
      //  assertEquals(1.0, analyzer.getTreeEditDistance("class Main{}", "class Foo{}"))
      //  assertEquals(2.0, analyzer.getTreeEditDistance("class Main {int x = 12}", "class Main {int x = 12};"))
    }

    @Test
    fun testGraphBuilding() {
        val analyzer = JavacAnalyzer()
        val (graph, tree) = analyzer.getGraphFromTree(addedLBracketAndBrokeJavac)
        analyzer.printAst(tree)
        assert(GraphTests.isTree(graph))
    }
    val code = """
                public interface C { {
                    void foo(String s, String... b);
    """.trimIndent()

}