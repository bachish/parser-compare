import me.tongfei.progressbar.ProgressBar
import org.antlr.v4.gui.TreeViewer
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane
import org.antlr.v4.runtime.Parser
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

fun showParseTree(parser: Parser, tree: ParseTree) {
    val frame = JFrame("ANTLR Interactive Parse Tree")

    // Получаем имена правил для текущего парсера
    val ruleNames = ParserFactory.getRuleNames(parser)

    val viewer = TreeViewer(ruleNames, tree)
    viewer.scale = 1.5 // Начальный масштаб
    val panel = JPanel(BorderLayout())
    panel.add(viewer)

    // Добавляем прокрутку
    val scrollPane = JScrollPane(panel)
    scrollPane.preferredSize = Dimension(800, 600)

    // Обработчик мыши для перетаскивания
    var dragStartX = 0
    var dragStartY = 0

    panel.addMouseListener(object : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            dragStartX = e.x
            dragStartY = e.y
        }
    })

    panel.addMouseMotionListener(object : MouseAdapter() {
        override fun mouseDragged(e: MouseEvent) {
            val dx = e.x - dragStartX
            val dy = e.y - dragStartY
            viewer.location = viewer.location.apply { setLocation(x + dx, y + dy) }
            dragStartX = e.x
            dragStartY = e.y
        }
    })

    // Обработчик прокрутки колесика мыши для масштабирования
    panel.addMouseWheelListener { e: MouseWheelEvent ->
        val notches = e.wheelRotation
        if (notches < 0) {
            viewer.scale += 0.1 // Увеличение масштаба
        } else {
            viewer.scale = maxOf(0.1, viewer.scale - 0.1) // Уменьшение масштаба, но не менее 0.1
        }
        viewer.revalidate()
    }

    frame.contentPane.add(scrollPane)
    frame.pack()
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    // Разворачиваем окно на весь экран
    frame.extendedState = JFrame.MAXIMIZED_BOTH
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.isVisible = true
}

fun parseList(fileNames: List<String>, directoryPath: String, analyzer: OldCodeAnalyzer) {
    val directory = File(directoryPath)

    // Проверяем, что файлы из списка существуют
    val filesToProcess = fileNames.mapNotNull { fileName ->
        val file = File(directory, fileName)
        if (file.exists() && file.isFile) file else null
    }

    val results = mutableListOf<Pair<String, Double>>()

    for (file in filesToProcess) {
        val filePath = file.absolutePath
        println("Processing file: $filePath")


        // Вычисляем значение
        val score = analyzer.calculateSimilarity(file)
        println("Score for $filePath: $score")

        // Сохраняем результат
        results.add(file.name to score)
    }
}

fun processFiles(
    directoryPath: String,
    outputCsvPath: String,
    analyzer: OldCodeAnalyzer,
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
            writer.append("fileName,numberOfSyntaxErrors,numberOfLexerErrors,similarityScore,distance,numberOfParsedTokens\n")
        }




        // Используем прогресс-бар
        ProgressBar("Processing Files", files.size.toLong()).use { pb ->
            files.forEach { file ->
                val similarity = analyzer.calculateSimilarity(file)

                writer.append(
                            "${file.name}," +
                            "${analyzer.numberOfSyntaxErrors}," +
                            "${analyzer.numberOfLexerErrors}," +
                            "${similarity},${analyzer.distance}," +
                            "${analyzer.numberOfParsedTokens}\n"
                )
                writer.flush() // Сразу записываем в файл

                pb.step()

//                слишком сильно сжирает бар, невидно ничего
//                pb.setExtraMessage("last chunk: ${file.name}")
            }
        }
    } finally {
        writer.close() // Закрытие writer после окончания записи
    }

    println("Processing complete. Results written to $outputCsvPath")
}



fun processFilesForTokensAndChars(
    language: String,
    directoryPath: String,
    outputCsvPath: String,
    append: Boolean = true
) {
    // Определяем режим открытия файла
    val openOption = if (append && File(outputCsvPath).exists()) StandardOpenOption.APPEND else StandardOpenOption.CREATE

    // Создаем BufferedWriter для записи в CSV
    val writer = Files.newBufferedWriter(Paths.get(outputCsvPath), openOption)
    try {
        // Если создаем новый файл, записываем заголовок
        if (openOption == StandardOpenOption.CREATE) {
            writer.append("fileName,numberOfTokens,numberOfCharacters\n")
        }

        // Получаем список файлов из директории
        val files = File(directoryPath).listFiles()?.filter { it.isFile } ?: emptyList()
        ProgressBar("Processing Files", files.size.toLong()).use { pb ->

            files.forEach { file ->
                try {
                    // Считываем содержимое файла
                    val content = file.readText()

                    // Создаем лексер и токенизируем содержимое
                    val lexer = ParserFactory.createLexer(language, content)
                    lexer.removeErrorListeners()

                    val tokenStream = CommonTokenStream(lexer)
                    tokenStream.fill() // Загружаем все токены
                    val tokenCount = tokenStream.tokens.size

                    // Считаем количество символов в файле
                    val charCount = content.length

                    // Записываем данные в CSV
                    writer.append("${file.name},$tokenCount,$charCount\n")
                    writer.flush()
                    pb.step()
                } catch (e: Exception) {
                    println("Ошибка обработки файла ${file.name}: ${e.message}")
                }
            }
        }
    } finally {
        writer.close() // Закрываем writer после использования
    }

    println("Обработка завершена. Результаты записаны в $outputCsvPath")
}

fun collectTokenCount() {

    val language = "java"
    val directoryPath = "C:\\data\\java_src_files"
    val outputCsvPath = "tokens_and_chars.csv" // Укажите путь к выходному CSV-файлу

    processFilesForTokensAndChars(
        language = language,
        directoryPath = directoryPath,
        outputCsvPath = outputCsvPath,
        append = true
    )

}