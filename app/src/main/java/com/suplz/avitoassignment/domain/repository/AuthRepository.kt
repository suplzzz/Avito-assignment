package com.suplz.avitoassignment.domain.repository

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    val currentUser: FirebaseUser?

    suspend fun login(email: String, pass: String): Result<Unit>

    suspend fun register(email: String, pass: String, name: String): Result<Unit>
    fun logout()
}