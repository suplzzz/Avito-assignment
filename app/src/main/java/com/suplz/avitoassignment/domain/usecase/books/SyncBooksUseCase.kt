package com.suplz.avitoassignment.domain.usecase.books

import com.suplz.avitoassignment.domain.repository.BooksRepository
import javax.inject.Inject

class SyncBooksUseCase @Inject constructor(private val repo: BooksRepository) {
    suspend operator fun invoke() = repo.syncBooks()
}