package com.suplz.avitoassignment.domain.repository

import android.net.Uri
import com.suplz.avitoassignment.domain.entity.Book
import kotlinx.coroutines.flow.Flow

interface BooksRepository {

    suspend fun uploadBook(uri: Uri, title: String, author: String): Result<Unit>


    fun getUserBooks(): Flow<List<Book>>

    suspend fun syncBooks(): Result<Unit>

    suspend fun downloadBookFile(book: Book): Result<Unit>
    suspend fun deleteBookFile(book: Book): Result<Unit>

    suspend fun deleteBookCompletely(book: Book): Result<Unit>
}