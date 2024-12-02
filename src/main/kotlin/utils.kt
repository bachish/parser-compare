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


