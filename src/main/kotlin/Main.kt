fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: <command> [arguments]")
        println("Commands:")
        println("  measure <directoryPath> <outputCsvPath> <analyzerType> <warmupFilesCount>")
        println("  scores <directoryPath> <outputCsvPath> <analyzerType>")
        println("  errors <directoryPath> <outputCsvPath> [--progress]")
        return
    }

    val command = args[0]
    when (command) {
        "measure" -> {
            if (args.size < 5) {
                println("Usage for measure: measure <directoryPath> <outputCsvPath> <analyzerType> <warmupFilesCount>")
                return
            }
            val directoryPath = args[1]
            val outputCsvPath = args[2]
            val analyzerType = args[3]
            val warmupFilesCount = args[4].toInt()
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
        "scores" -> {
            if (args.size < 4) {
                println("Usage for scores: scores <directoryPath> <outputCsvPath> <analyzerType>")
                return
            }
            val directoryPath = args[1]
            val outputCsvPath = args[2]
            val analyzerType = args[3]
            val analyzer = AnalyzerFactory.create<Any>(analyzerType)
            processFiles(
                directoryPath = directoryPath,
                outputCsvPath = outputCsvPath,
                analyzer = analyzer,
                append = true
            )
        }
        "errors" -> {
            if (args.size < 3) {
                println("Usage for errors: errors <directoryPath> <outputCsvPath> [--progress]")
                return
            }
            val directoryPath = args[1]
            val outputCsvPath = args[2]
            val withProgress = args.contains("--progress")

            JavacRunner.processFiles(
                directoryPath = directoryPath,
                outputCsvPath = outputCsvPath,
                append = true,
                withProgressBar = withProgress
            )
        }
        else -> {
            println("Invalid command. Use 'measure', 'scores', or 'errors'")
        }
    }
}
