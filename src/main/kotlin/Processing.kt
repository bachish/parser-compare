import analyzer.IRecoveryAnalyzer
import analyzer.treesitter.TreeSitterAnalyzer
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import me.tongfei.progressbar.ProgressBar

fun <T> processFiles(
    directoryPath: String,
    outputCsvPath: String,
    analyzer: IRecoveryAnalyzer<T>,
    maxFiles: Int = Int.MAX_VALUE,
    append: Boolean = true
) {
    // Определяем последний обработанный файл, если append = true
    val startFile = if (append) {
        val existingLines = File(outputCsvPath).takeIf { it.exists() }?.readLines().orEmpty()
        existingLines.lastOrNull()?.split(",")?.firstOrNull() // Имя последнего обработанного файла
    } else {
        null // Если не append, начинаем с первого файла
    }

    val files = File(directoryPath).listFiles()
        ?.filter { it.isFile }
        ?.sortedBy { it.name } // Сортируем файлы по имени для предсказуемости
        ?.let { fileList ->
            if (startFile != null) {
                println("startFile = $startFile")
                val startIndex = fileList.indexOfFirst { it.name == startFile }
                if (startIndex != -1) fileList.drop(startIndex + 1) else emptyList()
            } else {
                fileList
            }
        }
        ?.take(maxFiles)
        ?: emptyList()

    // Определение режима для BufferedWriter
    val openOption = if (append && File(outputCsvPath).exists()) StandardOpenOption.APPEND else StandardOpenOption.CREATE

    // Создание BufferedWriter для записи в CSV файл
    val writer = Files.newBufferedWriter(Paths.get(outputCsvPath), openOption)
    try {
        // Запись заголовков, если файл перезаписывается
        if (openOption == StandardOpenOption.CREATE) {
            writer.append("fileName,similarityScore\n")
        }

        // Используем прогресс-бар
        ProgressBar("Processing Files", files.size.toLong()).use { pb ->
            files.forEach { file ->
                val similarity = analyzer.calculateSimilarity(file)

                writer.append("${file.name},$similarity\n")
                writer.flush() // Сразу записываем в файл

                pb.step()
            }
        }
    } finally {
        writer.close() // Закрытие writer после окончания записи
    }

    println("Processing complete. Results written to $outputCsvPath")
}

fun main() {

    val analyzer = TreeSitterAnalyzer()
    processFiles(
        directoryPath = "C:\\data\\java_src_files",
        outputCsvPath = "C:\\data\\${analyzer::class.simpleName}_all_scores.csv",
        analyzer = analyzer,
//        maxFiles = 50,
        append = true
    )
}