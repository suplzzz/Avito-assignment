package com.suplz.avitoassignment.domain.usecase.books

import android.net.Uri
import com.suplz.avitoassignment.domain.repository.BooksRepository
import javax.inject.Inject

class UploadBookUseCase @Inject constructor(
    private val repository: BooksRepository
) {
    suspend operator fun invoke(uri: Uri, title: String, author: String): Result<Unit> {
        if (title.isBlank() || author.isBlank()) {
            return Result.failure(IllegalArgumentException("Fill in the title and author"))
        }
        return repository.uploadBook(uri, title, author)
    }
}