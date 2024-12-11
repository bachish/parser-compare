import me.tongfei.progressbar.ProgressBar
import org.antlr.v4.gui.TreeViewer
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

fun parseList(fileNames: List<String>, directoryPath: String, analyzer: CodeAnalyzer) {
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
    analyzer: CodeAnalyzer,
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
            writer.append("fileName,numberOfSyntaxErrors,similarityScore\n")
        }




        // Используем прогресс-бар
        ProgressBar("Processing Files", files.size.toLong()).use { pb ->
            files.forEach { file ->
                val similarity = analyzer.calculateSimilarity(file)

                writer.append("${file.name},${analyzer.numberOfSyntaxErrors},${similarity}\n")
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
