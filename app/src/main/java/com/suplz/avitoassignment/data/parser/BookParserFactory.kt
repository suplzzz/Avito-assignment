package com.suplz.avitoassignment.data.parser

import java.io.File
import javax.inject.Inject

class BookParserFactory @Inject constructor(
    private val epubParser: EpubBookParser,
    private val pdfParser: PdfBookParser,
    private val txtParser: TxtBookParser
) {

    fun getParser(file: File): BookParser {
        return when {
            file.name.endsWith(".epub", ignoreCase = true) -> epubParser
            file.name.endsWith(".pdf", ignoreCase = true) -> pdfParser
            file.name.endsWith(".txt", ignoreCase = true) -> txtParser
            else -> txtParser
        }
    }
}