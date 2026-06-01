package com.nk.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nk.app.data.repository.AuthRepository
import com.nk.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val user: User? = null,
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                if (authRepo.isLoggedIn()) {
                    val user = authRepo.getProfile()
                    _state.value = ProfileState(user = user, isLoggedIn = true, isLoading = false)
                } else {
                    _state.value = ProfileState(isLoggedIn = false, isLoading = false)
                }
            } catch (_: Exception) {
                _state.value = ProfileState(isLoggedIn = false, isLoading = false)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepo.logout()
            _state.value = ProfileState(isLoggedIn = false, isLoading = false)
        }
    }
}
