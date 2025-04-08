package old

fun main() {
    val language = "java"
    val analyzer = OldCodeAnalyzer(language)
    val directoryPath = "C:\\data\\java_src_files"              // где лежит датасет
//    val maxFiles = 10000
    val outputCsvPath = "C:\\data\\${language}_all_scores.csv"
//    val outputCsvPath = "C:\\data\\${language}_scores.csv"


//    collectTokenCount()

///////////////////    сбор времени исполнения
//    measureParsingTime(directoryPath,"C:\\data\\${language}_times.csv",analyzer, warmupFilesCount = 100)



///////////////////    основной процессинг
//    processFiles(directoryPath, outputCsvPath, analyzer)



/////////////////////////  Парсим один файл
//    val filePath = "$directoryPath\\38614002_1457569052"
//    val file = File(filePath)
//    val (tree, parser) = analyzer.parseFileWithParser(file)
//    val score = analyzer.calculateSimilarity(file)
//    println("score: $score")
//    showParseTree(parser, tree)




//////////////////////    Парсим строчку
//    val javaCode = "public class ProductTester;".trimIndent()
    val javaCode = """
        public class Test {}
    """.trimIndent()
    val (treeFromString, parserFromString) = analyzer.parseCodeWithParser(javaCode)
    val score = analyzer.calculateSimilarity(javaCode)

    println("score: $score")
    println(parserFromString.numberOfSyntaxErrors)
//    println(parserFromString)
    showParseTree(parserFromString, treeFromString)


///////////////////////////////////////////// Парсим список
//    val files = listOf(
//        "1981175_88001536", //bmv
//        "45493781_1758766325",
//        "48506454_1863037852",
//        "8821304_307656826", //xml
//        )
//
//    parseList(files, directoryPath, analyzer)
}

