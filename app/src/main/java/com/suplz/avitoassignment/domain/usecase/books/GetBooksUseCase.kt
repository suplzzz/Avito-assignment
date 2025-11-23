package com.suplz.avitoassignment.domain.usecase.books

import com.suplz.avitoassignment.domain.repository.BooksRepository
import javax.inject.Inject

class GetBooksUseCase @Inject constructor(private val repo: BooksRepository) {
    operator fun invoke() = repo.getUserBooks()
}