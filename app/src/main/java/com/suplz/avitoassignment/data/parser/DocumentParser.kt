package com.suplz.avitoassignment.data.parser

import com.suplz.avitoassignment.data.util.clearAllMarkdown
import com.suplz.avitoassignment.data.util.clearMarkdown
import com.suplz.avitoassignment.data.util.containsVisibleText
import com.suplz.avitoassignment.domain.entity.ReaderContent
import kotlinx.coroutines.yield
import org.jsoup.nodes.Document
import javax.inject.Inject

class DocumentParser @Inject constructor(
    private val markdownParser: MarkdownParser
) {
    suspend fun parseDocument(
        document: Document,
        includeChapter: Boolean = true
    ): List<ReaderContent> {
        yield()

        val result = mutableListOf<ReaderContent>()
        var chapterAdded = false
        val body = document.selectFirst("body") ?: document.body()

        body.apply {
            select("p").forEach { element ->
                element.html(element.html().replace(Regex("\\n+"), " "))
                element.append("\n")
            }
            select("a").forEach { element ->
                element.html(element.html().replace(Regex("\\n+"), ""))
            }

            select("title").remove()
            select("hr").append("\n---\n")
            select("b, strong").prepend("**").append("**")
            select("h1, h2, h3").prepend("**").append("**")
            select("em, i").prepend("_").append("_")

            select("a").forEach { element ->
                val link = element.attr("href")
                if (link.isNotBlank()) {
                    element.prepend("[")
                    element.append("]($link)")
                }
            }

            select("img").remove()
        }

        val wholeText = body.wholeText()

        wholeText.lines().forEach { line ->
            yield()
            val formattedLine = line
                .replace(Regex("\\*\\*\\*\\s*(.*?)\\*\\*\\*"), "_**$1**_")
                .replace(Regex("\\*\\*\\s*(.*?)\\*\\*"), "**$1**")
                .replace(Regex("_\\s*(.*?)\\s*_"), "_$1_")
                .trim()

            if (line.containsVisibleText()) {
                when {
                    line.trim() == "---" || line.trim() == "***" -> {
                        result.add(ReaderContent.Separator)
                    }
                    else -> {
                        if (!chapterAdded && includeChapter && formattedLine.clearAllMarkdown().containsVisibleText()) {
                            result.add(
                                ReaderContent.Chapter(title = formattedLine.clearAllMarkdown())
                            )
                            chapterAdded = true
                        } else if (formattedLine.clearMarkdown().containsVisibleText()) {
                            result.add(
                                ReaderContent.Text(text = markdownParser.parse(formattedLine))
                            )
                        }
                    }
                }
            }
        }

        return result
    }
}