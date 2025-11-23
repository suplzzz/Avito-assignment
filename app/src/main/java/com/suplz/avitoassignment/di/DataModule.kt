package com.suplz.avitoassignment.di

import android.content.Context
import androidx.room.Room
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.suplz.avitoassignment.BuildConfig
import com.suplz.avitoassignment.data.local.BookDatabase
import com.suplz.avitoassignment.data.local.dao.BookDao
import com.suplz.avitoassignment.data.repository.AuthRepositoryImpl
import com.suplz.avitoassignment.data.repository.BooksRepositoryImpl
import com.suplz.avitoassignment.data.repository.ProfileRepositoryImpl
import com.suplz.avitoassignment.data.repository.ReaderPreferencesRepositoryImpl
import com.suplz.avitoassignment.data.repository.ReaderRepositoryImpl
import com.suplz.avitoassignment.domain.repository.AuthRepository
import com.suplz.avitoassignment.domain.repository.BooksRepository
import com.suplz.avitoassignment.domain.repository.ProfileRepository
import com.suplz.avitoassignment.domain.repository.ReaderPreferencesRepository
import com.suplz.avitoassignment.domain.repository.ReaderRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    @Singleton
    fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    fun bindBooksRepository(impl: BooksRepositoryImpl): BooksRepository

    @Binds
    @Singleton
    fun bindReaderRepository(impl: ReaderRepositoryImpl): ReaderRepository

    @Binds
    @Singleton
    fun bindReaderPreferencesRepository(impl: ReaderPreferencesRepositoryImpl): ReaderPreferencesRepository

    @Binds
    @Singleton
    fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    companion object {

        @Provides
        @Singleton
        fun provideAmazonS3Client(): AmazonS3Client {
            val accessKey = BuildConfig.S3_ACCESS_KEY
            val secretKey = BuildConfig.S3_SECRET_KEY

            val credentials = BasicAWSCredentials(accessKey, secretKey)
            val s3Client = AmazonS3Client(credentials)

            s3Client.endpoint = BuildConfig.S3_ENDPOINT

            s3Client.setS3ClientOptions(
                com.amazonaws.services.s3.S3ClientOptions.builder()
                    .setPathStyleAccess(true)
                    .build()
            )

            return s3Client
        }

        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth {
            return FirebaseAuth.getInstance()
        }

        @Provides
        @Singleton
        fun provideFirebaseFirestore(): FirebaseFirestore {
            return FirebaseFirestore.getInstance()
        }

        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): BookDatabase {
            return Room.databaseBuilder(
                context,
                BookDatabase::class.java,
                "books.db"
            ).build()
        }

        @Provides
        @Singleton
        fun provideBookDao(database: BookDatabase): BookDao {
            return database.bookDao()
        }
    }
}