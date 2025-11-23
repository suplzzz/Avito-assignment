package com.suplz.avitoassignment.data.repository

import android.content.Context
import android.content.res.Configuration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.suplz.avitoassignment.domain.entity.ReaderSettings
import com.suplz.avitoassignment.domain.repository.ReaderPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "reader_prefs")

@Singleton
class ReaderPreferencesRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ReaderPreferencesRepository {

    private val dataStore = context.dataStore

    private object Keys {
        val TEXT_SIZE = intPreferencesKey("text_size")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        fun progressKey(bookId: String) = intPreferencesKey("progress_$bookId")
    }

    override fun getSettings(): Flow<ReaderSettings> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { prefs ->
                val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                val isSystemDark = currentNightMode == Configuration.UI_MODE_NIGHT_YES

                ReaderSettings(
                    textSizeSp = prefs[Keys.TEXT_SIZE] ?: 18,

                    isDarkMode = prefs[Keys.IS_DARK_MODE] ?: isSystemDark
                )
            }
    }

    override suspend fun saveTextSize(size: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.TEXT_SIZE] = size
        }
    }

    override suspend fun saveThemeMode(isDarkMode: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.IS_DARK_MODE] = isDarkMode
        }
    }

    override fun getBookProgress(bookId: String): Flow<Int> {
        return dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { prefs ->
                prefs[Keys.progressKey(bookId)] ?: 0
            }
    }

    override suspend fun saveBookProgress(bookId: String, index: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.progressKey(bookId)] = index
        }
    }
}