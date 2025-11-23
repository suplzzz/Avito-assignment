package com.suplz.avitoassignment.domain.usecase.books

import com.suplz.avitoassignment.domain.entity.Book
import com.suplz.avitoassignment.domain.repository.BooksRepository
import javax.inject.Inject

class DeleteBookUseCase @Inject constructor(private val repo: BooksRepository) {
    suspend operator fun invoke(book: Book) = repo.deleteBookFile(book)
}