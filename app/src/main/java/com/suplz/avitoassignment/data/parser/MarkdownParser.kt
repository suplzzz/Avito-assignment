package com.suplz.avitoassignment.data.parser

import org.commonmark.node.Text
import org.commonmark.node.*
import org.commonmark.parser.Parser
import javax.inject.Inject

class MarkdownParser @Inject constructor() {

    private val parser: Parser = Parser.builder().build()

    fun parse(markdown: String): String {
        return try {
            val node = parser.parse(markdown)
            val sb = StringBuilder()
            extractText(node, sb)
            sb.toString().trim()
        } catch (e: Exception) {
            e.printStackTrace()
            markdown
        }
    }

    private fun extractText(node: Node, sb: StringBuilder) {
        when (node) {
            is Text -> sb.append(node.literal)
            is SoftLineBreak, is HardLineBreak -> sb.append("\n")
        }

        var child = node.firstChild
        while (child != null) {
            extractText(child, sb)
            child = child.next
        }
    }
}