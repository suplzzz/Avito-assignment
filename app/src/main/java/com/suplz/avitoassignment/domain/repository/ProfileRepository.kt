package com.suplz.avitoassignment.domain.repository

import android.net.Uri
import com.suplz.avitoassignment.domain.entity.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getUserProfile(): Flow<UserProfile>

    suspend fun updateAvatar(uri: Uri): Result<String>
    suspend fun logout()

    suspend fun updateName(newName: String): Result<Unit>
}