import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.parse.Parser
import java.io.File



fun captureDotGraph(tree: org.treesitter.TSTree): String {
    val tempFile = File.createTempFile("temp_", ".dot")
    tree.printDotGraphs(tempFile)
    val dotContent = tempFile.readText()
    tempFile.delete()
    return dotContent
}

fun renderDotGraph(dotString: String) {
    try {
        // Создаем папку output в корне проекта, если её нет
        val outputDir = File("output")
        if (!outputDir.exists()) {
            outputDir.mkdir()
        }

        // Парсим DOT-граф
        val graph = Parser().read(dotString)
        val graphviz = Graphviz.fromGraph(graph)

        // Генерируем уникальное имя файла (например, с временной меткой)
        val fileName = "syntax_tree_${System.currentTimeMillis()}.svg"
        val outputFile = File(outputDir, fileName)

        // Сохраняем как SVG
        graphviz.render(Format.SVG).toFile(outputFile)

        println("SVG сохранен в: ${outputFile.absolutePath}")
    } catch (e: Exception) {
        println("Ошибка при рендеринге графа: ${e.message}")
    }
}