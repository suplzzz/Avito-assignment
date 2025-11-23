package com.suplz.avitoassignment.domain.entity

data class UserProfile(
    val uid: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null
)