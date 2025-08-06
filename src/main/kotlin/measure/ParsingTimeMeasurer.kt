package measure

import me.tongfei.progressbar.ProgressBar
import parsers.IRecoveryAnalyzer
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class ParsingTimeMeasurer : IMeasurer {
    private val warmupFilesCount: Int = 100
    private val maxFiles: Int = Int.MAX_VALUE
    private val append: Boolean = true

    override fun <T, N> measure(analyzer: IRecoveryAnalyzer<T, N>, inputDirectory: String, outputCsvFile: String) {
        val files = getFiles(inputDirectory, outputCsvFile)
        warmupJvm(files, analyzer)

        val file = File(outputCsvFile)
        val isNewFile = !file.exists() || file.length() == 0L

        val writer = Files.newBufferedWriter(
            Paths.get(outputCsvFile),
            if (append && file.exists()) StandardOpenOption.APPEND else StandardOpenOption.CREATE
        ).apply {
            if (isNewFile) append("fileName,parsingTimeNanos\n")
        }

        ProgressBar("Measuring Parsing Time", files.size.toLong()).use { pb ->
            writer.use { w ->
                files.forEach { file ->
                    val times = List(40) {
                        System.gc()
                        analyzer.measureParse(file)
                    }
                    val timesString = times.joinToString(",")
                    w.append("${file.name},$timesString\n").flush()
                    pb.step()
                }
            }
        }
        println("Results written to $outputCsvFile")
    }

    private fun getFiles(inputDirectory: String, outputCsvPath: String): List<File> {
        val startFile = if (append) File(outputCsvPath).takeIf { it.exists() }?.readLines()?.lastOrNull()?.split(",")
            ?.firstOrNull() else null
        return File(inputDirectory).listFiles()?.filter { it.isFile }?.sortedBy { it.name }?.let { list ->
            if (startFile != null) {
                println("Continuing from file: $startFile")
                list.drop(list.indexOfFirst { it.name == startFile }.takeIf { it != -1 }?.plus(1) ?: 0)
            } else list
        }?.take(maxFiles) ?: emptyList()
    }

    private fun <T, N> warmupJvm(files: List<File>, analyzer: IRecoveryAnalyzer<T, N>) {
        println("Starting JVM warmup...")
        files.shuffled().take(warmupFilesCount).forEach { analyzer.measureParse(it) }
        println("JVM warmup complete.")
    }
}