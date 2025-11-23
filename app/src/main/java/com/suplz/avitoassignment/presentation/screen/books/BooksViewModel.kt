package com.suplz.avitoassignment.presentation.screen.books

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suplz.avitoassignment.domain.entity.Book
import com.suplz.avitoassignment.domain.usecase.books.DeleteBookCompletelyUseCase
import com.suplz.avitoassignment.domain.usecase.books.DeleteBookUseCase
import com.suplz.avitoassignment.domain.usecase.books.DownloadBookUseCase
import com.suplz.avitoassignment.domain.usecase.books.GetBookCoverUseCase
import com.suplz.avitoassignment.domain.usecase.books.GetBooksUseCase
import com.suplz.avitoassignment.domain.usecase.books.SyncBooksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BooksViewModel @Inject constructor(
    getBooksUseCase: GetBooksUseCase,
    private val syncBooksUseCase: SyncBooksUseCase,
    private val downloadBookUseCase: DownloadBookUseCase,
    private val deleteBookUseCase: DeleteBookUseCase,
    private val deleteBookCompletelyUseCase: DeleteBookCompletelyUseCase,
    private val getBookCoverUseCase: GetBookCoverUseCase
) : ViewModel() {



    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow(BooksUiState())
    val uiState = _uiState.asStateFlow()

    val booksState = combine(getBooksUseCase(), _searchQuery) { books, query ->
        filterBooks(books, query)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        processCommand(BooksCommand.Refresh)
    }


    fun processCommand(command: BooksCommand) {
        when (command) {
            is BooksCommand.Search -> _searchQuery.update { command.query }
            is BooksCommand.DownloadBook -> downloadBook(command.book)
            is BooksCommand.DeleteLocalBook -> deleteLocalBook(command.book)
            is BooksCommand.DeleteCompletely -> deleteCompletely(command.book)
            BooksCommand.Refresh -> refreshBooks()
            BooksCommand.ClearMessage -> _uiState.update { it.copy(userMessage = null) }
        }
    }


    suspend fun loadCover(path: String): Bitmap? {
        return getBookCoverUseCase(path)
    }



    private fun filterBooks(books: List<Book>, query: String): List<Book> {
        if (query.isBlank()) return books
        return books.filter { book ->
            book.title.contains(query, ignoreCase = true) ||
                    book.author.contains(query, ignoreCase = true)
        }
    }

    private fun refreshBooks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGlobalLoading = true) }
            syncBooksUseCase()
                .onFailure { e -> showUserMessage("Ошибка синхронизации: ${e.localizedMessage}") }
            _uiState.update { it.copy(isGlobalLoading = false) }
        }
    }

    private fun downloadBook(book: Book) {
        launchWithItemLoading(book.id) {
            downloadBookUseCase(book)
                .onSuccess { showUserMessage("Книга скачана") }
                .onFailure { showUserMessage("Ошибка скачивания") }
        }
    }

    private fun deleteLocalBook(book: Book) {
        launchWithItemLoading(book.id) {
            deleteBookUseCase(book)
                .onSuccess { showUserMessage("Файл удален с устройства") }
        }
    }

    private fun deleteCompletely(book: Book) {
        launchWithItemLoading(book.id) {
            deleteBookCompletelyUseCase(book)
                .onSuccess { showUserMessage("Книга удалена из библиотеки") }
                .onFailure { e ->
                    val message = if (e is TimeoutCancellationException) "Нет сети. Удаление отложено." else "Ошибка: ${e.localizedMessage}"
                    showUserMessage(message)
                }
        }
    }

    private fun launchWithItemLoading(bookId: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            toggleItemLoading(bookId, true)
            block()
            toggleItemLoading(bookId, false)
        }
    }

    private fun toggleItemLoading(bookId: String, isLoading: Boolean) {
        _uiState.update { state ->
            val newSet = if (isLoading) state.loadingItemIds + bookId else state.loadingItemIds - bookId
            state.copy(loadingItemIds = newSet)
        }
    }

    private fun showUserMessage(message: String) {
        _uiState.update { it.copy(userMessage = message) }
    }
}

sealed interface BooksCommand {
    data class Search(val query: String) : BooksCommand
    data class DownloadBook(val book: Book) : BooksCommand
    data class DeleteLocalBook(val book: Book) : BooksCommand
    data class DeleteCompletely(val book: Book) : BooksCommand
    data object Refresh : BooksCommand
    data object ClearMessage : BooksCommand
}

data class BooksUiState(
    val isGlobalLoading: Boolean = false,
    val userMessage: String? = null,
    val loadingItemIds: Set<String> = emptySet()
)