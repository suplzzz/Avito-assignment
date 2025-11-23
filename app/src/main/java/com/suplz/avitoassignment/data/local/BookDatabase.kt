package com.suplz.avitoassignment.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.suplz.avitoassignment.data.local.dao.BookDao
import com.suplz.avitoassignment.data.local.entity.BookEntity

@Database(entities = [BookEntity::class], version = 1, exportSchema = false)
abstract class BookDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
}