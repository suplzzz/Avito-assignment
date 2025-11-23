package com.suplz.avitoassignment.data.parser

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.suplz.avitoassignment.domain.entity.ReaderContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.io.File
import java.io.IOException
import java.net.URLDecoder
import java.util.zip.ZipFile
import javax.inject.Inject

class EpubBookParser @Inject constructor(
    private val documentParser: DocumentParser
) : BookParser {

    override suspend fun parseContent(file: File): List<ReaderContent> = withContext(Dispatchers.IO) {
        val result = mutableListOf<ReaderContent>()

        try {
            ZipFile(file).use { zip ->
                val opfEntry = zip.entries().asSequence().firstOrNull { it.name.endsWith(".opf", ignoreCase = true) }
                    ?: throw IOException("OPF file not found")

                val opfParentPath = File(opfEntry.name).parent ?: ""
                val opfInputStream = zip.getInputStream(opfEntry)
                val opfDoc = Jsoup.parse(opfInputStream, "UTF-8", "", Parser.xmlParser())

                val spineIds = opfDoc.select("spine > itemref").map { it.attr("idref") }

                val manifestMap = opfDoc.select("manifest > item").associate {
                    it.attr("id") to it.attr("href")
                }

                for (id in spineIds) {
                    val relativePath = manifestMap[id] ?: continue
                    val decodedPath = URLDecoder.decode(relativePath, "UTF-8")

                    val fullPath = if (opfParentPath.isNotEmpty()) "$opfParentPath/$decodedPath" else decodedPath
                    val entry = zip.getEntry(fullPath) ?: continue

                    zip.getInputStream(entry).use { stream ->
                        val htmlDoc = Jsoup.parse(stream, "UTF-8", "")

                        val parsedChapterContent = documentParser.parseDocument(
                            document = htmlDoc,
                            includeChapter = true
                        )

                        if (parsedChapterContent.isNotEmpty()) {
                            result.addAll(parsedChapterContent)
                            result.add(ReaderContent.Separator)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            throw IOException("Failed to parse EPUB", e)
        }

        if (result.isEmpty()) {
            throw IOException("EPUB content is empty")
        }

        result
    }

    override suspend fun getCover(file: File): Bitmap? = withContext(Dispatchers.IO) {
        try {
            ZipFile(file).use { zip ->
                val opfEntry = zip.entries().asSequence().firstOrNull { it.name.endsWith(".opf", ignoreCase = true) }
                    ?: return@withContext null
                val opfParentPath = File(opfEntry.name).parent ?: ""

                val opfDoc = Jsoup.parse(zip.getInputStream(opfEntry), "UTF-8", "", Parser.xmlParser())

                var coverId = opfDoc.select("metadata > meta[name=cover]").attr("content")

                if (coverId.isBlank()) {
                    coverId = opfDoc.select("manifest > item[media-type^=image][id*=cover]").attr("id")
                }

                val coverHref = opfDoc.select("manifest > item[id=$coverId]").attr("href")

                if (coverHref.isBlank()) return@withContext null

                val decodedHref = URLDecoder.decode(coverHref, "UTF-8")
                val fullPath = if (opfParentPath.isNotEmpty()) "$opfParentPath/$decodedHref" else decodedHref

                val entry = zip.getEntry(fullPath) ?: return@withContext null

                zip.getInputStream(entry).use {
                    BitmapFactory.decodeStream(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}