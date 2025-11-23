package com.suplz.avitoassignment.domain.usecase.profile

import com.suplz.avitoassignment.domain.entity.UserProfile
import com.suplz.avitoassignment.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke(): Flow<UserProfile> = repository.getUserProfile()
}