import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.system.measureNanoTime
import me.tongfei.progressbar.ProgressBar

fun measureParsingTime(
    directoryPath: String,
    outputCsvPath: String,
    analyzer: OldCodeAnalyzer,
    warmupFilesCount: Int = 10,
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
                println("Continuing from file: $startFile")
                val startIndex = fileList.indexOfFirst { it.name == startFile }
                if (startIndex != -1) fileList.drop(startIndex + 1) else emptyList()
            } else {
                fileList
            }
        }
        ?.take(maxFiles)
        ?: emptyList()

    // Прогрев JVM на случайных файлах
    println("Starting JVM warmup...")
    val randomFiles = files.shuffled().take(warmupFilesCount)

    randomFiles.forEach { file ->
        val code = file.readText()
        measureNanoTime {
            analyzer.hollowParse(code)
        } // Просто вызываем парсинг, результаты не сохраняем
    }
    println("JVM warmup complete.")

    // Определение режима для BufferedWriter
    val openOption = if (append && File(outputCsvPath).exists()) StandardOpenOption.APPEND else StandardOpenOption.CREATE

    // Создание BufferedWriter для записи в CSV файл
    val writer = Files.newBufferedWriter(Paths.get(outputCsvPath), openOption)
    try {
        // Запись заголовков, если файл создается заново
        if (openOption == StandardOpenOption.CREATE) {
            writer.append("fileName,parsingTimeNanos\n")
        }

        // Используем прогресс-бар
        ProgressBar("Measuring Parsing Time", files.size.toLong()).use { pb ->
            files.forEach { file ->
                val code = file.readText()

                val parsingTime = measureNanoTime {
                    analyzer.hollowParse(code)
                }

                writer.append("${file.name},$parsingTime\n")
                writer.flush() // Сразу записываем в файл

                pb.step()
            }
        }
    } finally {
        writer.close() // Закрытие writer после окончания записи
    }

    println("Parsing time measurement complete. Results written to $outputCsvPath")
}
