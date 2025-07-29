package parsers.jdt

import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.ASTNode

import java.util.*

fun captureDotGraph(code: String): String {
    val parser = ASTParser.newParser(AST.JLS8)
    parser.setSource(code.toCharArray())
    parser.setKind(ASTParser.K_COMPILATION_UNIT)
    val cu = parser.createAST(null) as CompilationUnit

    val visitor = AstToDotVisitor()
    cu.toString()
    cu.accept(visitor)

    return generateDotGraph(visitor.nodeIds, visitor.children)
}

private class AstToDotVisitor : org.eclipse.jdt.core.dom.ASTVisitor() {
    private var nodeIdCounter = 0
    val nodeIds = mutableMapOf<ASTNode, Int>()
    val children = mutableMapOf<Int, MutableList<Int>>()
    private val stack = Stack<ASTNode>()

    override fun preVisit(node: ASTNode) {
        val parent = if (stack.isEmpty()) null else stack.peek()
        val id = nodeIdCounter++
        nodeIds[node] = id
        if (parent != null) {
            children.getOrPut(nodeIds[parent]!!) { mutableListOf() }.add(id)
        }
        stack.push(node)
    }

    override fun postVisit(node: ASTNode) {
        stack.pop()
    }
}

private fun generateDotGraph(nodeIds: Map<ASTNode, Int>, children: Map<Int, List<Int>>): String {
    val sb = StringBuilder()
    sb.append("digraph {\n")

    for ((node, id) in nodeIds) {
        val label = getLabel(node)
        sb.append("$id [label=\"$label\"];\n")
    }

    for ((parentId, childIds) in children) {
        for (childId in childIds) {
            sb.append("$parentId -> $childId;\n")
        }
    }

    sb.append("}\n")
    return sb.toString()
}

private fun getLabel(node: ASTNode): String {
    val type = node.javaClass.simpleName
    return when (node) {
        is org.eclipse.jdt.core.dom.CompilationUnit -> type
        is org.eclipse.jdt.core.dom.TypeDeclaration -> "$type: ${node.name.identifier}"
        is org.eclipse.jdt.core.dom.MethodDeclaration -> "$type: ${node.name.identifier}"
        else -> type
    }
}

fun main() {
    val code = "public class HelloWorld { public static void main(String[] args)  System.out.println(\"Hello, World!\"}"
    val dotGraph = captureDotGraph(code)
    println(dotGraph)
//    renderDotGraph(dotGraph)
}