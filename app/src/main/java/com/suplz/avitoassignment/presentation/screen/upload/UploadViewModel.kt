package com.suplz.avitoassignment.presentation.screen.upload

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suplz.avitoassignment.domain.usecase.books.UploadBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val uploadBookUseCase: UploadBookUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UploadState())
    val state = _state.asStateFlow()

    fun processCommand(command: UploadCommand) {
        when (command) {
            is UploadCommand.FileSelected -> {
                _state.update {
                    it.copy(selectedFileUri = command.uri, selectedFileName = command.name)
                }
            }
            is UploadCommand.InputTitle -> {
                _state.update { it.copy(title = command.value) }
            }
            is UploadCommand.InputAuthor -> {
                _state.update { it.copy(author = command.value) }
            }
            UploadCommand.UploadClick -> uploadBook()
            UploadCommand.ClearError -> _state.update { it.copy(error = null) }
            UploadCommand.ClearSuccess -> _state.update { it.copy(isSuccess = false) }
        }
    }

    private fun uploadBook() {
        val currentState = state.value


        if (currentState.selectedFileUri == null) {
            _state.update { it.copy(error = "Выберите файл") }
            return
        }


        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            uploadBookUseCase(
                uri = currentState.selectedFileUri,
                title = currentState.title,
                author = currentState.author
            ).onSuccess {

                _state.update { UploadState(isSuccess = true) }
            }.onFailure { error ->

                _state.update {
                    it.copy(isLoading = false, error = error.localizedMessage)
                }
            }
        }
    }
}


sealed interface UploadCommand {
    data class FileSelected(val uri: Uri, val name: String) : UploadCommand
    data class InputTitle(val value: String) : UploadCommand
    data class InputAuthor(val value: String) : UploadCommand
    data object UploadClick : UploadCommand
    data object ClearError : UploadCommand
    data object ClearSuccess : UploadCommand
}

data class UploadState(
    val title: String = "",
    val author: String = "",
    val selectedFileUri: Uri? = null,
    val selectedFileName: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)