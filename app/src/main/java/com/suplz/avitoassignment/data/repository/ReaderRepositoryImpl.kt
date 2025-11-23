package com.suplz.avitoassignment.data.repository

import android.graphics.Bitmap
import com.suplz.avitoassignment.data.local.dao.BookDao
import com.suplz.avitoassignment.data.parser.BookParserFactory
import com.suplz.avitoassignment.domain.entity.Book
import com.suplz.avitoassignment.domain.entity.ReaderContent
import com.suplz.avitoassignment.domain.repository.ReaderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject

class ReaderRepositoryImpl @Inject constructor(
    private val bookDao: BookDao,
    private val parserFactory: BookParserFactory
) : ReaderRepository {

    override suspend fun getBook(bookId: String): Result<Book> = withContext(Dispatchers.IO) {
        runCatching {
            val entity = bookDao.getBookById(bookId)
                ?: throw IllegalStateException("Книга не найдена в базе данных")

            val realFileExists = entity.localPath?.let { File(it).exists() } == true

            Book(
                id = entity.id,
                title = entity.title,
                author = entity.author,
                fileUrl = entity.fileUrl,
                ownerId = entity.ownerId,
                isDownloaded = entity.isDownloaded && realFileExists,
                localPath = entity.localPath
            )
        }
    }

    override suspend fun getBookContent(bookId: String): Result<List<ReaderContent>> = withContext(Dispatchers.IO) {
        runCatching {
            val entity = bookDao.getBookById(bookId)
                ?: throw IllegalStateException("Книга не найдена")

            val path = entity.localPath
                ?: throw IllegalStateException("Файл книги не скачан")

            val file = File(path)
            if (!file.exists()) {
                throw FileNotFoundException("Файл удален или перемещен: $path")
            }

            val parser = parserFactory.getParser(file)
            parser.parseContent(file)
        }
    }

    override suspend fun getBookCover(filePath: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) return@withContext null

            val parser = parserFactory.getParser(file)

            parser.getCover(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}