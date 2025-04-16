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

object JavacRunner {
    @JvmStatic
    fun main(args: Array<String>) {
        val directoryPath = "C:\\Users\\huawei\\Desktop\\test_error_files" // Директория с файлами
        val outputCsvPath = "output_errors.csv"   // Путь к CSV для записи
        val maxFiles = Int.MAX_VALUE                       // Максимальное количество файлов
        val append = true                                  // Режим добавления

        // Пример вызова с прогресс-баром:
//        processFiles(directoryPath, outputCsvPath, maxFiles, append, withProgressBar = true)

        // Пример вызова без прогресс-бара:
         processFiles(directoryPath, outputCsvPath, maxFiles, append)
    }

    fun processFiles(
        directoryPath: String,
        outputCsvPath: String,
        maxFiles: Int = Int.MAX_VALUE,
        append: Boolean = true,
        withProgressBar: Boolean = false
    ) {
        val startFile = if (append) {
            val existingLines = File(outputCsvPath).takeIf { it.exists() }?.readLines().orEmpty()
            existingLines.lastOrNull()?.split(",")?.firstOrNull()
        } else null

        val files = File(directoryPath).listFiles()
            ?.filter { it.isFile }
            ?.sortedBy { it.name }
            ?.let { fileList ->
                if (startFile != null) {
                    println("Resuming from file: $startFile")
                    val startIndex = fileList.indexOfFirst { it.name == startFile }
                    if (startIndex != -1) fileList.drop(startIndex + 1) else emptyList()
                } else fileList
            }
            ?.take(maxFiles)
            ?: emptyList()

        val openOption = if (append && File(outputCsvPath).exists()) StandardOpenOption.APPEND else StandardOpenOption.CREATE
        val writer = Files.newBufferedWriter(Paths.get(outputCsvPath), openOption)

        try {
            if (openOption == StandardOpenOption.CREATE) {
                writer.append("fileName,code,message,position,end_position,column_number,line_number\n")
            }

            val process: (File) -> Unit = { file ->
                val errors = compileJavaFile(file)
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
                                    "\"${error.columnNumber}\"," +
                                    "\"${error.lineNumber}\"\n"
                        )
                    }
                }
                writer.flush()
            }

            if (withProgressBar) {
                ProgressBar("Processing Files", files.size.toLong()).use { pb ->
                    files.forEach { file ->
                        process(file)
                        pb.step()
                    }
                }
            } else {
                files.forEach { file -> process(file) }
            }

        } finally {
            writer.close()
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
                diagnostic.columnNumber,
                diagnostic.lineNumber
            )
        }
    }

    private class JavaSourceFromFile(originalFile: File) :
        SimpleJavaFileObject(
            URI.create("file:///${ensureJavaExtension(originalFile).absolutePath.replace("\\", "/")}"),
            JavaFileObject.Kind.SOURCE
        ) {

        private val content: String = originalFile.readText()

        override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence = content

        companion object {
            private fun ensureJavaExtension(file: File): File {
                return if (file.name.endsWith(".java")) {
                    file
                } else {
                    val tempFile = File.createTempFile(file.nameWithoutExtension, ".java")
                    tempFile.writeText(file.readText())
                    tempFile.deleteOnExit()
                    tempFile
                }
            }
        }
    }
}
