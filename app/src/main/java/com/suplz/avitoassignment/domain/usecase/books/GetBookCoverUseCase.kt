package com.suplz.avitoassignment.domain.usecase.books

import android.graphics.Bitmap
import com.suplz.avitoassignment.domain.repository.ReaderRepository
import javax.inject.Inject

class GetBookCoverUseCase @Inject constructor(
    private val readerRepository: ReaderRepository
) {
    suspend operator fun invoke(filePath: String): Bitmap? {
        return readerRepository.getBookCover(filePath)
    }
}