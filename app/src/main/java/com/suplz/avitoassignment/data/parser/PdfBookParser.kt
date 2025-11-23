package com.suplz.avitoassignment.data.parser

import android.content.Context
import android.graphics.Bitmap
import com.suplz.avitoassignment.domain.entity.ReaderContent
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.rendering.ImageType
import com.tom_roush.pdfbox.rendering.PDFRenderer
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfBookParser @Inject constructor(
    @param:ApplicationContext private val context: Context
) : BookParser {

    private val isInitialized = AtomicBoolean(false)
    private val initMutex = Mutex()

    private suspend fun ensureLibraryInitialized() {
        if (!isInitialized.get()) {
            initMutex.withLock {
                if (!isInitialized.get()) {
                    try {
                        PDFBoxResourceLoader.init(context)
                        isInitialized.set(true)
                    } catch (e: Exception) {
                        throw IOException("Failed to initialize PDF library", e)
                    }
                }
            }
        }
    }

    override suspend fun parseContent(file: File): List<ReaderContent> = withContext(Dispatchers.IO) {
        ensureLibraryInitialized()

        val result = mutableListOf<ReaderContent>()

        try {
            PDDocument.load(file).use { document ->
                val stripper = PDFTextStripper().apply {
                    sortByPosition = true
                }

                val fullText = stripper.getText(document)
                fullText.lineSequence().forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty()) {
                        if (isValidTextLine(trimmed)) {
                            result.add(ReaderContent.Text(trimmed))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            throw IOException("Failed to parse PDF", e)
        }

        if (result.isEmpty()) {
            throw IOException("PDF content is empty")
        }

        result
    }

    override suspend fun getCover(file: File): Bitmap? = withContext(Dispatchers.IO) {
        ensureLibraryInitialized()

        try {
            PDDocument.load(file).use { document ->
                if (document.numberOfPages > 0) {
                    val renderer = PDFRenderer(document)
                    return@withContext renderer.renderImage(0, 0.5f, ImageType.RGB)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    private fun isValidTextLine(text: String): Boolean {
        return !(text.length < 5 && text.all { it.isDigit() })
    }
}