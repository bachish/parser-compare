

import java.awt.*
import javax.swing.*
import org.treesitter.*

class TreePanel(val tree: TSTree) : JPanel() {
    private val nodePositions = mutableMapOf<TSNode, Point>() // Теперь эта переменная доступна всем методам
    private val nodeSize = Dimension(120, 30)

    init {
        preferredSize = Dimension(1200, 800)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Рисуем дерево, начиная с корневого узла
        val root = tree.rootNode
        drawTree(g2, root, width / 2, 50, width / 4)
    }

    private fun drawTree(g: Graphics2D, node: TSNode, x: Int, y: Int, xOffset: Int) {
        val pos = Point(x, y)
        nodePositions[node] = pos

        // Используем курсор для обхода детей текущего узла, а не корневого
        val cursor = TSTreeCursor(node)
        if (cursor.gotoFirstChild()) {
            var childX = x - xOffset
            do {
                val child = cursor.currentNode()
                val childPos = Point(childX, y + 100)
                nodePositions[child] = childPos

                // Линия между узлами
                g.drawLine(pos.x + nodeSize.width / 2, pos.y + nodeSize.height,
                    childPos.x + nodeSize.width / 2, childPos.y)

                // Рекурсивно рисуем дочерний узел
                drawTree(g, child, childX, y + 100, xOffset / 2)
                childX += xOffset
            } while (cursor.gotoNextSibling())
        }

        // Рисуем сам узел
        g.color = Color(200, 220, 255)
        g.fillRoundRect(pos.x, pos.y, nodeSize.width, nodeSize.height, 15, 15)
        g.color = Color.BLACK
        g.drawRoundRect(pos.x, pos.y, nodeSize.width, nodeSize.height, 15, 15)
        g.drawString(node.type, pos.x + 10, pos.y + 20)
    }
}

fun showTree(tree: TSTree) {
    val frame = JFrame("Tree-sitter Parse Tree")
    val panel = TreePanel(tree)

    val scrollPane = JScrollPane(panel)
    frame.add(scrollPane)

    frame.setSize(1200, 800)
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.isVisible = true
}



fun main() {
    val parser = TSParser()
    parser.setLanguage(TreeSitterJava())
    val tree = parser.parseString(null, """
        public class Test {
            public static void main(String[] args) {
                System.out.println("Hello World!");
            }
        }
    """.trimIndent())

    showTree(tree)
}
