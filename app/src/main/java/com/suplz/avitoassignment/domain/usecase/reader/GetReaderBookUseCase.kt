package com.suplz.avitoassignment.domain.usecase.reader

import com.suplz.avitoassignment.domain.entity.Book
import com.suplz.avitoassignment.domain.repository.ReaderRepository
import javax.inject.Inject

class GetReaderBookUseCase @Inject constructor(
    private val repository: ReaderRepository
) {
    suspend operator fun invoke(bookId: String): Result<Book> {
        return repository.getBook(bookId)
    }
}