package old

import com.sun.source.util.JavacTask
import me.tongfei.progressbar.ProgressBar
import java.io.File
import java.io.IOException
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import javax.tools.DiagnosticCollector
import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject
import javax.tools.ToolProvider

data class CompilationError(
    val code: String,
    val message: String,
    val position: Long,
    val endPosition: Long,
    val columnNumber: Long,
    val lineNumber: Long
)



object JavacRunnerWithProgress {
    @JvmStatic
    fun main(args: Array<String>) {
        val directoryPath = "C:/data/java_src_files_java/" // Директория с файлами
        val outputCsvPath = "output_with_progress-3.csv"   // Путь к CSV для записи
        val maxFiles = Int.MAX_VALUE                       // Максимальное количество файлов
        val append = true                                  // Режим добавления
        processFiles(directoryPath, outputCsvPath, maxFiles, append)
    }

    fun processFiles(
        directoryPath: String,
        outputCsvPath: String,
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
                    println("Resuming from file: $startFile")
                    val startIndex = fileList.indexOfFirst { it.name == startFile }
                    if (startIndex != -1) fileList.drop(startIndex + 1) else emptyList()
                } else {
                    fileList
                }
            }
            ?.take(maxFiles)
            ?: emptyList()

        // Определение режима для записи (добавление или создание нового файла)
        val openOption = if (append && File(outputCsvPath).exists()) StandardOpenOption.APPEND else StandardOpenOption.CREATE

        // Создаем BufferedWriter для записи в CSV файл
        val writer = Files.newBufferedWriter(Paths.get(outputCsvPath), openOption)
        try {
            // Записываем заголовок, если файл создается с нуля
            if (openOption == StandardOpenOption.CREATE) {
                writer.append("fileName,code,message,position,end_position,column_number,line_number\n")
            }

            // Используем ProgressBar для отображения прогресса
            ProgressBar("Processing Files", files.size.toLong()).use { pb ->
                files.forEach { file ->
                    // Компилируем файл и собираем ошибки
                    val errors = compileJavaFile(file)

                    // Записываем результаты в CSV
                    if (errors.isEmpty()) {
                        writer.append("${file.name},No Error,Success,Null,Null,Null,Null\n")
                    } else {
                        for (error in errors) {
                            writer.append(
                                "${file.name}," +
                                "\"${error.code}\"," +
                                "\"${error.message}\"," +
                                "\"${error.position}\"," +
                                "\"${error.endPosition}\"," +
                                "\"${error.columnNumber}\"" +
                                "\"${error.lineNumber}\"\n"
                            )
                        }
                    }
                    writer.flush() // Сразу записываем в файл

                    pb.step() // Обновляем прогресс-бар
                }
            }
        } finally {
            writer.close() // Закрываем writer
        }

        println("Processing complete. Results written to $outputCsvPath")
    }

    @Throws(IOException::class)
    private fun compileJavaFile(file: File): List<CompilationError> {
        val compiler = ToolProvider.getSystemJavaCompiler()
        val diagnostics = DiagnosticCollector<JavaFileObject>()
        val fileObject = JavaSourceFromFile(file)
        val task = compiler.getTask(
            null, null,
            diagnostics, null, null,
            listOf(fileObject)
        ) as JavacTask

        try {
            task.parse()
        } catch (e: Throwable) {
//            return listOf(CompilationError("", "Error occurred: ${e.message}", -1, -1, -1, -1))
        }

        return diagnostics.diagnostics.map { diagnostic ->
            CompilationError(
                diagnostic.code,
                diagnostic.getMessage(null),
                diagnostic.position,
                diagnostic.endPosition,
                diagnostic.columnNumber.toLong(),
                diagnostic.lineNumber.toLong()
            )
        }
    }


    private class JavaSourceFromFile(file: File) :
        SimpleJavaFileObject(
            URI.create("file:///${file.absolutePath.replace("\\", "/")}"),
            JavaFileObject.Kind.SOURCE
        ) {
        private val content: String = file.readText()

        override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence {
            return content
        }
    }
}
