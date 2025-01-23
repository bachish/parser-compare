import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.system.measureNanoTime
import kotlin.random.Random
import me.tongfei.progressbar.ProgressBar

fun measureParsingTime(
    directoryPath: String,
    outputCsvPath: String,
    analyzer: CodeAnalyzer,
    warmupFilesCount: Int = 10,
    maxFiles: Int = Int.MAX_VALUE
) {
    val files = File(directoryPath).listFiles()
        ?.filter { it.isFile }
        ?.sortedBy { it.name }
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
    val openOption = if (File(outputCsvPath).exists()) StandardOpenOption.APPEND else StandardOpenOption.CREATE

    // Создание BufferedWriter для записи в CSV файл
    val writer = Files.newBufferedWriter(Paths.get(outputCsvPath), openOption)
    try {
        // Запись заголовков, если файл перезаписывается
        if (openOption == StandardOpenOption.CREATE) {
            writer.append("fileName,parsingTimeNanos\n")
        }

        // Используем прогресс-бар
        ProgressBar("Measuring Parsing Time", files.size.toLong()).use { pb ->
            files.take(maxFiles).forEach { file ->
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

