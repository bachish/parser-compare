import parsers.AnalyzerType
import parsers.ParserFactory
import parsers.treesitter.TreeSitterAnalyzer
import java.io.File

fun main(args: Array<String>) {
    measureErrors()

    val argsSize = 4
    if (args.size != argsSize) {
        println("Wronr argumnent count! (expected: $argsSize, actual: ${args.size}")
        println("Usage: <measurement> <directoryPath> <outputCsvPath> <parser>")
        return
    }

    val measurement = args[0]
    val directoryPath = args[1]
    val outputCsvPath = args[2]
    val parser = args[3]
    //  val warmupFilesCount = args[4].toInt()

    val parserAnalyzer = ParserFactory.create(parser)

}

fun _main(args: Array<String>) {
    val analyzer = TreeSitterAnalyzer()
    val filePath = args[0]
//    val filePath = "C:\\data\\java_src_files\\49030395_1874025707"
    val file = File(filePath)
    val s = analyzer.measureParse(file.readText())
}

fun measureErrors() {
    val code = "clacc Main(){var x = 12 12 12}"
   // val code = "clacc Main(){}"
    var parser = ParserFactory.create(AnalyzerType.AntlrJava8Analyzer)
    parser.getErrors(code)

}

//fun main() {
//    println("Начинаем потреблять память...")
//
//    // Максимальный лимит памяти в мегабайтах (например, 500 MB)
//    val maxMemoryLimitMB = 500
//
//    // Размер одного объекта в мегабайтах
//    val objectSizeMB = 10
//
//    // Создаем список для хранения больших объектов
//    val memoryHog = mutableListOf<ByteArray>()
//
//    var allocatedMemoryMB = 0
//    while (allocatedMemoryMB < maxMemoryLimitMB) {
//        // Добавляем массив байтов размером objectSizeMB МБ
//        val largeObject = ByteArray(objectSizeMB * 1024 * 1024) // objectSizeMB MB
//        memoryHog.add(largeObject)
//
//        allocatedMemoryMB += objectSizeMB
//        println("Выделено: $allocatedMemoryMB MB")
//    }
//
//}
//
