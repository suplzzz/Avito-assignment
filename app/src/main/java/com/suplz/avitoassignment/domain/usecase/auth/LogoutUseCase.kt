package com.suplz.avitoassignment.domain.usecase.auth

import com.suplz.avitoassignment.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke() {
        repository.logout()
    }
}