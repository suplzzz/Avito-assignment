package com.suplz.avitoassignment.domain.repository

import android.graphics.Bitmap
import com.suplz.avitoassignment.domain.entity.Book
import com.suplz.avitoassignment.domain.entity.ReaderContent

interface ReaderRepository {

    suspend fun getBook(bookId: String): Result<Book>

    suspend fun getBookContent(bookId: String): Result<List<ReaderContent>>

    suspend fun getBookCover(filePath: String): Bitmap?
}