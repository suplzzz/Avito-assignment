package com.suplz.avitoassignment.domain.usecase.profile

import com.suplz.avitoassignment.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdateNameUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(newName: String): Result<Unit> = repository.updateName(newName)
}