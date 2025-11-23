package com.suplz.avitoassignment.data.parser

import android.graphics.Bitmap
import com.suplz.avitoassignment.domain.entity.ReaderContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class TxtBookParser @Inject constructor(
    private val markdownParser: MarkdownParser
) : BookParser {

    override suspend fun parseContent(file: File): List<ReaderContent> = withContext(Dispatchers.IO) {
        if (!file.exists()) throw IOException("File not found")

        val result = mutableListOf<ReaderContent>()
        val charset = detectCharset(file)

        file.bufferedReader(charset).useLines { lines ->
            lines.forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isNotEmpty()) {
                    if (trimmed == "***" || trimmed == "---") {
                        result.add(ReaderContent.Separator)
                    } else {
                        val cleanText = markdownParser.parse(trimmed)
                        result.add(ReaderContent.Text(cleanText))
                    }
                }
            }
        }

        if (result.isEmpty()) {
            throw IOException("File is empty")
        }

        result
    }

    override suspend fun getCover(file: File): Bitmap? {
        return null
    }

    private fun detectCharset(file: File): Charset {
        try {
            val buf = ByteArray(2048)
            val length: Int

            FileInputStream(file).use { fis ->
                val bis = BufferedInputStream(fis)
                length = bis.read(buf)
            }

            if (length == -1) return StandardCharsets.UTF_8

            if (length >= 3 &&
                (buf[0].toInt() and 0xFF) == 0xEF &&
                (buf[1].toInt() and 0xFF) == 0xBB &&
                (buf[2].toInt() and 0xFF) == 0xBF) {
                return StandardCharsets.UTF_8
            }

            val decoder = StandardCharsets.UTF_8.newDecoder()
            decoder.onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
            decoder.onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT)

            val byteBuffer = java.nio.ByteBuffer.wrap(buf, 0, length)
            decoder.decode(byteBuffer)

            return StandardCharsets.UTF_8

        } catch (_: Exception) {
            return try {
                Charset.forName("Windows-1251")
            } catch (_: Exception) {
                Charset.defaultCharset()
            }
        }
    }
}