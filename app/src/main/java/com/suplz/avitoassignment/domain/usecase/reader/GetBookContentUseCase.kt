package com.suplz.avitoassignment.domain.usecase.reader

import com.suplz.avitoassignment.domain.entity.ReaderContent
import com.suplz.avitoassignment.domain.repository.ReaderRepository
import javax.inject.Inject

class GetBookContentUseCase @Inject constructor(
    private val repository: ReaderRepository
) {
    suspend operator fun invoke(bookId: String): Result<List<ReaderContent>> {
        return repository.getBookContent(bookId)
    }
}