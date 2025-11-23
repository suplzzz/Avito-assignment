package com.suplz.avitoassignment.domain.usecase.books

import com.suplz.avitoassignment.domain.entity.Book
import com.suplz.avitoassignment.domain.repository.BooksRepository
import javax.inject.Inject

class DownloadBookUseCase @Inject constructor(private val repo: BooksRepository) {
    suspend operator fun invoke(book: Book) = repo.downloadBookFile(book)
}