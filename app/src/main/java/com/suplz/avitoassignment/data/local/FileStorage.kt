package com.suplz.avitoassignment.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileStorage @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    fun saveFile(fileName: String, bytes: ByteArray): String {
        val file = File(context.filesDir, fileName)
        FileOutputStream(file).use { output ->
            output.write(bytes)
        }
        return file.absolutePath
    }

    fun deleteFile(fileName: String): Boolean {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) file.delete() else true
    }
}