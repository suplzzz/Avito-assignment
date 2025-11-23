package com.suplz.avitoassignment.domain.usecase.auth

import com.suplz.avitoassignment.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, pass: String, name: String): Result<Unit> {
        return repository.register(email, pass, name)
    }
}