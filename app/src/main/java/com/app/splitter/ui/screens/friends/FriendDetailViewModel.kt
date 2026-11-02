package com.app.splitter.ui.screens.friends

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.splitter.data.repository.SplitterRepository
import com.app.splitter.domain.model.Expense
import com.app.splitter.domain.model.FriendBalanceSummary
import com.app.splitter.domain.model.User
import com.app.splitter.domain.model.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FriendDetailUiState(
    val summary: FriendBalanceSummary? = null,
    val expenses: List<Expense> = emptyList(),
    val usersMap: Map<String, User> = emptyMap(),
    val userSettings: UserSettings = UserSettings(),
    val currentUser: User? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class FriendDetailViewModel @Inject constructor(
    private val repository: SplitterRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val friendId: String = checkNotNull(savedStateHandle["friendId"])

    val uiState: StateFlow<FriendDetailUiState> = combine(
        repository.getFriendBalanceSummary(friendId),
        repository.getFriendExpenses(friendId),
        repository.getAllUsers(),
        repository.getUserSettings(),
        repository.getCurrentUser()
    ) { summary, expenses, users, settings, currentUser ->
        FriendDetailUiState(
            summary = summary,
            expenses = expenses,
            usersMap = users.associateBy { it.id },
            userSettings = settings,
            currentUser = currentUser,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FriendDetailUiState(isLoading = true)
    )

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            repository.deleteExpense(expenseId)
        }
    }
}
