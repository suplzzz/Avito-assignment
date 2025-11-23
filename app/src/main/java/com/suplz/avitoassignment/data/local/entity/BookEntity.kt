package com.suplz.avitoassignment.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String? = null,
    val fileUrl: String,
    val localPath: String? = null,
    val isDownloaded: Boolean = false,
    val ownerId: String
)