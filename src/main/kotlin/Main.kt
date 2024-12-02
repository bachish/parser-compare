import java.io.File

// 10 штук руками посмотреть которые никто не распарсил
// отфильтровать торлько те которые один рапарсил и достать от туда сообщения об ошибках
// те которые никто не распарсил прогнать чеерез жвм (через питон легко)


fun main() {
    val language = "java8"
    val analyzer = CodeAnalyzer(language)
    val directoryPath = "C:\\data\\java_src_files"
//    val maxFiles = 10000
    val outputCsvPath = "C:\\data\\${language}_all_scores.csv"
//    val outputCsvPath = "C:\\data\\${language}_scores.csv"


/////////////////////    основной процессинг
//    processFiles(directoryPath, outputCsvPath, analyzer, maxFiles)
    processFiles(directoryPath, outputCsvPath, analyzer,  append = true)



///////////////////////  Парсим один файл
//    val filePath = "$directoryPath\\3142580_110474263"
//    val file = File(filePath)
//    val (tree, parser) = analyzer.parseFileWithParser(file)
//    val score = analyzer.calculateSimilarity(file)
//    println("score: $score")
//    showParseTree(parser, tree)
//
//
//

////////////////////////    Парсим строчку
////    val javaCode = "public class ProductTester;".trimIndent()
//    val javaCode = """
//        public class Main {
//          public static void main(String[] args)
//          {
//            do {}
//          }
//        }
//    """.trimIndent()
//    val (treeFromString, parserFromString) = analyzer.parseCodeWithParser(javaCode)
//    val score = analyzer.calculateSimilarity(javaCode)
//
//    println("score: $score")
//    println(parserFromString.numberOfSyntaxErrors)
////    println(parserFromString)
//    showParseTree(parserFromString, treeFromString)

}
