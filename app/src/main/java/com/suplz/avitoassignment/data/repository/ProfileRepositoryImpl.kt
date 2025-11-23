package com.suplz.avitoassignment.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.suplz.avitoassignment.BuildConfig
import com.suplz.avitoassignment.R
import com.suplz.avitoassignment.domain.entity.UserProfile
import com.suplz.avitoassignment.domain.repository.ProfileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val s3Client: AmazonS3Client,
    @param:ApplicationContext private val context: Context
) : ProfileRepository {


    override fun getUserProfile(): Flow<UserProfile> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                trySend(
                    UserProfile(
                        uid = user.uid,
                        name = user.displayName ?: context.getString(R.string.user),
                        email = user.email ?: "",
                        photoUrl = user.photoUrl?.toString()
                    )
                )
            }
        }

        auth.addAuthStateListener(listener)
        listener.onAuthStateChanged(auth)

        awaitClose { auth.removeAuthStateListener(listener) }
    }.flowOn(Dispatchers.IO)

    override suspend fun updateAvatar(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser ?: throw IllegalStateException("User not found")

            val fileName = "${UUID.randomUUID()}.jpg"
            val objectKey = "avatars/${user.uid}/$fileName"

            context.contentResolver.openInputStream(uri)?.use { stream ->
                val metadata = ObjectMetadata()
                if (stream.available() > 0) {
                    metadata.contentLength = stream.available().toLong()
                }
                s3Client.putObject(BuildConfig.S3_BUCKET_NAME, objectKey, stream, metadata)
            }

            val newPhotoUrl = s3Client.getResourceUrl(BuildConfig.S3_BUCKET_NAME, objectKey)
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setPhotoUri(newPhotoUrl.toUri())
                .build()

            val task = user.updateProfile(profileUpdates)
            while (!task.isComplete) { Thread.sleep(10) }
            if (!task.isSuccessful) throw task.exception ?: Exception("Update failed")
            user.reload()
            Result.success(newPhotoUrl)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        auth.signOut()
    }

    override suspend fun updateName(newName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser ?: throw IllegalStateException("User not found")

            val updates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()

            user.updateProfile(updates).await()
            user.reload()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}