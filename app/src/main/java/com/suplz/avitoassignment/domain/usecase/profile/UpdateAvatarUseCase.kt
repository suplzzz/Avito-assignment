package com.suplz.avitoassignment.domain.usecase.profile

import android.net.Uri
import com.suplz.avitoassignment.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdateAvatarUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(uri: Uri): Result<String> = repository.updateAvatar(uri)
}