package com.app.splitter.ui.screens.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.splitter.data.repository.SplitterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddFriendUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddFriendViewModel @Inject constructor(
    private val repository: SplitterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddFriendUiState())
    val uiState: StateFlow<AddFriendUiState> = _uiState

    fun onNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(name = name, error = null)
    }

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun onPhoneChanged(phone: String) {
        _uiState.value = _uiState.value.copy(phone = phone, error = null)
    }

    fun addFriend(onSuccess: () -> Unit) {
        val name = _uiState.value.name.trim()
        val email = _uiState.value.email.trim()
        if (name.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Friend's name cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                repository.addFriend(name, email, _uiState.value.phone.trim())
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.localizedMessage ?: "Failed to add friend")
            }
        }
    }
}
