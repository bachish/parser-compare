
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.system.measureNanoTime
import me.tongfei.progressbar.ProgressBar
import analyzer.IRecoveryAnalyzer
import analyzer.antlr.AntlrJava8Analyzer
import analyzer.javac.JavacAnalyzer

class ParsingTimeMeasurer<T>(
    private val directoryPath: String,
    private val outputCsvPath: String,
    private val analyzer: IRecoveryAnalyzer<T>,
    private val warmupFilesCount: Int = 100,
    private val maxFiles: Int = Int.MAX_VALUE,
    private val append: Boolean = true
) {
    fun measure() {
        val files = getFiles()
        warmupJvm(files)

        val file = File(outputCsvPath)
        val isNewFile = !file.exists() || file.length() == 0L

        val writer = Files.newBufferedWriter(
            Paths.get(outputCsvPath),
            if (append && file.exists()) StandardOpenOption.APPEND else StandardOpenOption.CREATE
        ).apply {
            if (isNewFile) append("fileName,parsingTimeNanos\n")
        }

        ProgressBar("Measuring Parsing Time", files.size.toLong()).use { pb ->
            writer.use { w ->
                files.forEach { file ->
                    val time = measureNanoTime { analyzer.measureParse(file) }
                    w.append("${file.name},$time\n").flush()
                    pb.step()
                }
            }
        }
        println("Results written to $outputCsvPath")
    }

    private fun getFiles(): List<File> {
        val startFile = if (append) File(outputCsvPath).takeIf { it.exists() }?.readLines()?.lastOrNull()?.split(",")?.firstOrNull() else null
        return File(directoryPath).listFiles()
            ?.filter { it.isFile }
            ?.sortedBy { it.name }
            ?.let { list ->
                if (startFile != null) {
                    println("Continuing from file: $startFile")
                    list.drop(list.indexOfFirst { it.name == startFile }.takeIf { it != -1 }?.plus(1) ?: 0)
                } else list
            }
            ?.take(maxFiles) ?: emptyList()
    }

    private fun warmupJvm(files: List<File>) {
        println("Starting JVM warmup...")
        files.shuffled().take(warmupFilesCount).forEach { analyzer.measureParse(it) }
        println("JVM warmup complete.")
    }
}

// Пример использования
fun main() {
    val analyzer = AntlrJava8Analyzer()
    val measurer = ParsingTimeMeasurer(
//        directoryPath = "C:\\data\\java_src_files_java", // строго важно для жавака _java чтобы были правильные штуки эти после точки не помнб как их там расширение точно
//        outputCsvPath = "C:\\data\\${analyzer::class.simpleName}_measureParsingTime.csv",
        directoryPath = "C:\\data\\junit",
        outputCsvPath = "C:\\data\\${analyzer::class.simpleName}_measureParsingTime_junit.csv",
        analyzer = analyzer,
        warmupFilesCount = 100,
//        maxFiles = 50, // Ограничим для теста, можно убрать
        append = true
    )
    measurer.measure()
}