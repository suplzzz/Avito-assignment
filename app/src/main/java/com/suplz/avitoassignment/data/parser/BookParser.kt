package com.suplz.avitoassignment.data.parser

import android.graphics.Bitmap
import com.suplz.avitoassignment.domain.entity.ReaderContent
import java.io.File

interface BookParser {

    suspend fun parseContent(file: File): List<ReaderContent>

    suspend fun getCover(file: File): Bitmap?
}