package com.suplz.avitoassignment.domain.usecase.books

import com.suplz.avitoassignment.domain.entity.Book
import com.suplz.avitoassignment.domain.repository.BooksRepository
import javax.inject.Inject

class DeleteBookCompletelyUseCase @Inject constructor(
    private val repository: BooksRepository
) {
    suspend operator fun invoke(book: Book) = repository.deleteBookCompletely(book)
}