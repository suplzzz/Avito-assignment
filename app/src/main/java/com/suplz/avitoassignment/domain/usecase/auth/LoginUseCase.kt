package com.suplz.avitoassignment.domain.usecase.auth

import com.suplz.avitoassignment.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, pass: String): Result<Unit> {
        return repository.login(email, pass)
    }
}