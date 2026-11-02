package com.app.splitter.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.splitter.data.repository.SplitterRepository
import com.app.splitter.domain.model.BalanceSummary
import com.app.splitter.domain.model.User
import com.app.splitter.domain.model.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val currentUser: User? = null,
    val allUsers: List<User> = emptyList(),
    val settings: UserSettings = UserSettings(),
    val balanceSummary: BalanceSummary = BalanceSummary(),
    val isLoading: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: SplitterRepository
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        repository.getCurrentUser(),
        repository.getAllUsers(),
        repository.getUserSettings(),
        repository.getGlobalBalanceSummary()
    ) { user, users, settings, summary ->
        ProfileUiState(
            currentUser = user,
            allUsers = users,
            settings = settings,
            balanceSummary = summary,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState(isLoading = true)
    )

    fun switchUser(userId: String) {
        viewModelScope.launch {
            repository.switchCurrentUser(userId)
        }
    }

    fun updateCurrency(currencySymbol: String) {
        viewModelScope.launch {
            val current = uiState.value.settings
            repository.updateUserSettings(current.copy(currencySymbol = currencySymbol))
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            val current = uiState.value.settings
            repository.updateUserSettings(current.copy(isDarkMode = enabled))
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            val current = uiState.value.settings
            repository.updateUserSettings(current.copy(notificationsEnabled = enabled))
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.resetData()
        }
    }
}
