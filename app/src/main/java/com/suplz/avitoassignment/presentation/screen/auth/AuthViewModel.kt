package com.suplz.avitoassignment.presentation.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suplz.avitoassignment.domain.usecase.auth.CheckAuthUseCase
import com.suplz.avitoassignment.domain.usecase.auth.LoginUseCase
import com.suplz.avitoassignment.domain.usecase.auth.LogoutUseCase
import com.suplz.avitoassignment.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val checkAuthUseCase: CheckAuthUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    init {
        checkAuthStatus()
    }

    fun processCommand(command: AuthCommand) {
        when (command) {
            is AuthCommand.Login -> login(command)
            is AuthCommand.Register -> register(command)
            AuthCommand.Logout -> logout()
            AuthCommand.ClearError -> clearError()
        }
    }

    private fun checkAuthStatus() {
        if (checkAuthUseCase()) {
            _state.update { it.copy(isAuthenticated = true) }
        }
    }

    private fun login(command: AuthCommand.Login) {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            loginUseCase(command.email, command.pass)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, isAuthenticated = true) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.localizedMessage) }
                }
        }
    }

    private fun register(command: AuthCommand.Register) {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            registerUseCase(command.email, command.pass, command.name)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, isAuthenticated = true) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.localizedMessage) }
                }
        }
    }

    private fun logout() {
        logoutUseCase()
        _state.update { it.copy(isAuthenticated = false) }
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

sealed interface AuthCommand {
    data class Login(val email: String, val pass: String) : AuthCommand
    data class Register(val email: String, val pass: String, val name: String) : AuthCommand
    data object Logout : AuthCommand
    data object ClearError : AuthCommand
}

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null
)