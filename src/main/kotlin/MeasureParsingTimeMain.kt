
fun main(args: Array<String>) {
    if (args.size < 4) {
        println("Usage: <directoryPath> <outputCsvPath> <analyzerType> <warmupFilesCount>")
        return
    }

    val directoryPath = args[0]
    val outputCsvPath = args[1]
    val analyzerType = args[2]
    val warmupFilesCount = args[3].toInt()

    val analyzer = AnalyzerFactory.create<Any>(analyzerType)
    val measurer = ParsingTimeMeasurer(
        directoryPath = directoryPath,
        outputCsvPath = outputCsvPath,
        analyzer = analyzer,
        warmupFilesCount = warmupFilesCount,
        append = true
    )
    measurer.measure()
}