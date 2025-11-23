package com.suplz.avitoassignment.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toUri
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.suplz.avitoassignment.BuildConfig
import com.suplz.avitoassignment.R
import com.suplz.avitoassignment.data.local.FileStorage
import com.suplz.avitoassignment.data.local.dao.BookDao
import com.suplz.avitoassignment.data.local.entity.BookEntity
import com.suplz.avitoassignment.domain.entity.Book
import com.suplz.avitoassignment.domain.repository.BooksRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.UUID
import javax.inject.Inject

class BooksRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val s3Client: AmazonS3Client,
    private val bookDao: BookDao,
    private val fileStorage: FileStorage,
    @param:ApplicationContext private val context: Context
) : BooksRepository {


    override suspend fun uploadBook(uri: Uri, title: String, author: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw IllegalStateException("User not logged in")

            val fileNameRaw = getFileNameFromUri(uri)
            val objectKey = "${user.uid}/${UUID.randomUUID()}_$fileNameRaw"

            val fileUrl = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val metadata = ObjectMetadata()
                    if (stream.available() > 0) {
                        metadata.contentLength = stream.available().toLong()
                    }
                    s3Client.putObject(BuildConfig.S3_BUCKET_NAME, objectKey, stream, metadata)
                }
                s3Client.getResourceUrl(BuildConfig.S3_BUCKET_NAME, objectKey)
            }

            val bookData = hashMapOf(
                "title" to title,
                "author" to author,
                "fileUrl" to fileUrl,
                "ownerId" to user.uid,
                "createdAt" to System.currentTimeMillis()
            )

            val docRef = firestore.collection("books").add(bookData).await()
            val newBookId = docRef.id

            val localExtension = when {
                fileNameRaw.contains(".pdf", true) -> "pdf"
                fileNameRaw.contains(".epub", true) -> "epub"
                else -> "txt"
            }
            val savedFileName = "book_$newBookId.$localExtension"

            val localPath = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    fileStorage.saveFile(savedFileName, input.readBytes())
                }
            }

            if (localPath != null) {
                val newEntity = BookEntity(
                    id = newBookId,
                    title = title,
                    author = author,
                    fileUrl = fileUrl,
                    ownerId = user.uid,
                    isDownloaded = true,
                    localPath = localPath
                )
                bookDao.insertBook(newEntity)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override fun getUserBooks(): Flow<List<Book>> {
        val userId = auth.currentUser?.uid ?: return kotlinx.coroutines.flow.flowOf(emptyList())

        return bookDao.getBooksFlow(userId).map { entities ->
            entities.map { entity ->
                val realFileExists = entity.localPath?.let { path ->
                    java.io.File(path).exists()
                } == true

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
    }

    override suspend fun syncBooks(): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw IllegalStateException("No user")

            val snapshot = firestore.collection("books")
                .whereEqualTo("ownerId", userId)
                .get()
                .await()

            val remoteBooks = snapshot.documents.map { doc ->
                val data = doc.data!!
                BookEntity(
                    id = doc.id,
                    title = data["title"] as? String ?: context.getString(R.string.without_title),
                    author = data["author"] as? String ?: context.getString(R.string.noname),
                    fileUrl = data["fileUrl"] as? String ?: "",
                    ownerId = userId,
                    isDownloaded = false,
                    localPath = null
                )
            }
            val remoteIds = remoteBooks.map { it.id }.toSet()

            val localIds = bookDao.getBookIds(userId)

            val idsToDelete = localIds.filter { it !in remoteIds }
            idsToDelete.forEach { id ->
                bookDao.deleteBook(id)
            }
            remoteBooks.forEach { remoteBook ->
                val localBook = bookDao.getBookById(remoteBook.id)
                if (localBook != null) {
                    val updatedBook = remoteBook.copy(
                        isDownloaded = localBook.isDownloaded,
                        localPath = localBook.localPath
                    )
                    bookDao.insertBook(updatedBook)
                } else {
                    bookDao.insertBook(remoteBook)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun downloadBookFile(book: Book): Result<Unit> {
        return try {
            val path = withContext(Dispatchers.IO) {
                val extension = when {
                    book.fileUrl.contains(".pdf", true) -> "pdf"
                    book.fileUrl.contains(".epub", true) -> "epub"
                    else -> "txt"
                }
                val fileName = "book_${book.id}.$extension"

                URL(book.fileUrl).openStream().use { input ->
                    fileStorage.saveFile(fileName, input.readBytes())
                }
            }

            bookDao.updateDownloadStatus(
                bookId = book.id,
                isDownloaded = true,
                path = path
            )

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteBookFile(book: Book): Result<Unit> {
        return try {
            book.localPath?.let { path ->
                val fileName = path.substringAfterLast("/")
                fileStorage.deleteFile(fileName)
            }

            bookDao.updateDownloadStatus(
                bookId = book.id,
                isDownloaded = false,
                path = null
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteBookCompletely(book: Book): Result<Unit> {
        return try {
            deleteBookFile(book)

            kotlinx.coroutines.withTimeout(5000L) {
                firestore.collection("books").document(book.id).delete().await()

                try {
                    val uri = book.fileUrl.toUri()

                    var rawKey = uri.path?.removePrefix("/") ?: ""

                    if (rawKey.startsWith("${BuildConfig.S3_BUCKET_NAME}/")) {
                        rawKey = rawKey.removePrefix("${BuildConfig.S3_BUCKET_NAME}/")
                    }

                    val objectKey = java.net.URLDecoder.decode(rawKey, "UTF-8")

                    if (objectKey.isNotEmpty()) {
                        android.util.Log.d("S3_DEBUG", "Deleting parsed key: '$objectKey'")

                        withContext(Dispatchers.IO) {
                            s3Client.deleteObject(BuildConfig.S3_BUCKET_NAME, objectKey)
                        }
                        android.util.Log.d("S3_DEBUG", "Delete request sent successfully")
                    } else {
                        android.util.Log.e("S3_DEBUG", "Empty key parsed from: ${book.fileUrl}")
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    android.util.Log.e("S3_DEBUG", "Error S3: ${e.message}")
                }
            }

            bookDao.deleteBook(book.id)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor.use {
                if (it != null && it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = it.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result ?: "unknown_file"
    }
}