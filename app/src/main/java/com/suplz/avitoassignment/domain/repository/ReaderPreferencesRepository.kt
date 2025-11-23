package com.suplz.avitoassignment.domain.repository

import com.suplz.avitoassignment.domain.entity.ReaderSettings
import kotlinx.coroutines.flow.Flow

interface ReaderPreferencesRepository {

    fun getSettings(): Flow<ReaderSettings>

    suspend fun saveTextSize(size: Int)

    suspend fun saveThemeMode(isDarkMode: Boolean)

    fun getBookProgress(bookId: String): Flow<Int>

    suspend fun saveBookProgress(bookId: String, index: Int)
}