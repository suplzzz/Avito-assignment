package com.suplz.avitoassignment.presentation.screen.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suplz.avitoassignment.domain.entity.UserProfile
import com.suplz.avitoassignment.domain.usecase.profile.GetUserProfileUseCase
import com.suplz.avitoassignment.domain.usecase.profile.LogoutUseCase
import com.suplz.avitoassignment.domain.usecase.profile.UpdateAvatarUseCase
import com.suplz.avitoassignment.domain.usecase.profile.UpdateNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateAvatarUseCase: UpdateAvatarUseCase,
    private val updateNameUseCase: UpdateNameUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getUserProfileUseCase().collect { profile ->
                _state.update { it.copy(user = profile) }
            }
        }
    }

    fun processCommand(command: ProfileCommand) {
        when (command) {
            is ProfileCommand.UploadAvatar -> uploadAvatar(command.uri)
            is ProfileCommand.UpdateName -> updateName(command.newName)
            ProfileCommand.Logout -> logout()
            ProfileCommand.ClearMessage -> _state.update { it.copy(message = null) }
        }
    }

    private fun updateName(newName: String) {
        if (newName.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            updateNameUseCase(newName)
                .onSuccess {

                    _state.update { s ->
                        s.copy(
                            isLoading = false,
                            message = "Имя обновлено",
                            user = s.user?.copy(name = newName)
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, message = "Ошибка: ${e.localizedMessage}") }
                }
        }
    }

    private fun uploadAvatar(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            updateAvatarUseCase(uri)
                .onSuccess { newUrl ->

                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            message = "Фото профиля обновлено",
                            user = currentState.user?.copy(photoUrl = newUrl)
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, message = "Ошибка: ${e.localizedMessage}") }
                }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            logoutUseCase()

        }
    }
}

sealed interface ProfileCommand {
    data class UploadAvatar(val uri: Uri) : ProfileCommand
    data class UpdateName(val newName: String) : ProfileCommand
    data object Logout : ProfileCommand
    data object ClearMessage : ProfileCommand
}

data class ProfileUiState(
    val user: UserProfile? = null,
    val isLoading: Boolean = false,
    val message: String? = null
)