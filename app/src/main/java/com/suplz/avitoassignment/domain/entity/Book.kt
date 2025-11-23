package com.suplz.avitoassignment.domain.entity

data class Book(
    val id: String = "",
    val title: String,
    val author: String,
    val fileUrl: String,
    val ownerId: String,
    val isDownloaded: Boolean = false,
    val localPath: String? = null
)