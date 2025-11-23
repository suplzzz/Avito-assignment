package com.suplz.avitoassignment.presentation.screen.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suplz.avitoassignment.domain.entity.ReaderContent
import com.suplz.avitoassignment.domain.repository.ReaderPreferencesRepository
import com.suplz.avitoassignment.domain.usecase.books.DeleteBookUseCase
import com.suplz.avitoassignment.domain.usecase.reader.GetBookContentUseCase
import com.suplz.avitoassignment.domain.usecase.reader.GetReaderBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val getBookContentUseCase: GetBookContentUseCase,
    private val getReaderBookUseCase: GetReaderBookUseCase,
    private val preferencesRepository: ReaderPreferencesRepository,
    private val deleteBookUseCase: DeleteBookUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookId: String = checkNotNull(savedStateHandle["bookId"])

    private val _state = MutableStateFlow(ReaderUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.getSettings().collect { settings ->
                _state.update { it.copy(textSizeSp = settings.textSizeSp, isDarkMode = settings.isDarkMode) }
            }
        }
        loadData()
    }

    fun processCommand(command: ReaderCommand) {
        when (command) {
            is ReaderCommand.ChangeFontSize -> changeFontSize(command.newSize)
            ReaderCommand.ToggleTheme -> toggleTheme()
            is ReaderCommand.SaveProgress -> saveProgress(command.index)
            ReaderCommand.DeleteBook -> deleteBook()
            ReaderCommand.Retry -> loadData()
        }
    }

    private fun deleteBook() {
        viewModelScope.launch {

            getReaderBookUseCase(bookId).onSuccess { book ->

                deleteBookUseCase(book)
                    .onSuccess {

                        _state.update { it.copy(isDeleted = true) }
                    }
                    .onFailure { e ->

                        _state.update { it.copy(error = "Не удалось удалить: ${e.localizedMessage}") }
                    }
            }
        }
    }

    private fun loadData() {
        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            getReaderBookUseCase(bookId).onSuccess { book ->
                _state.update { it.copy(bookTitle = book.title) }
            }

            val savedIndex = preferencesRepository.getBookProgress(bookId).first()

            getBookContentUseCase(bookId)
                .onSuccess { content ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            content = content,
                            initialScrollIndex = savedIndex
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.localizedMessage) }
                }
        }
    }

    private fun changeFontSize(newSize: Int) {
        val clampedSize = newSize.coerceIn(12, 40)
        viewModelScope.launch { preferencesRepository.saveTextSize(clampedSize) }
    }

    private fun toggleTheme() {
        val currentMode = state.value.isDarkMode
        viewModelScope.launch { preferencesRepository.saveThemeMode(!currentMode) }
    }

    private fun saveProgress(index: Int) {
        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            preferencesRepository.saveBookProgress(bookId, index)
        }
    }
}

sealed interface ReaderCommand {
    data class ChangeFontSize(val newSize: Int) : ReaderCommand
    data class SaveProgress(val index: Int) : ReaderCommand
    data object ToggleTheme : ReaderCommand
    data object Retry : ReaderCommand
    data object DeleteBook : ReaderCommand
}

data class ReaderUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeleted: Boolean = false,
    val bookTitle: String = "",
    val content: List<ReaderContent> = emptyList(),
    val textSizeSp: Int = 18,
    val isDarkMode: Boolean = false,
    val initialScrollIndex: Int = 0
)