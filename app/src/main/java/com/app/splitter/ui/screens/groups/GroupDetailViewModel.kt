package com.app.splitter.ui.screens.groups

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.splitter.data.repository.SplitterRepository
import com.app.splitter.domain.model.Expense
import com.app.splitter.domain.model.GroupBalanceSummary
import com.app.splitter.domain.model.User
import com.app.splitter.domain.model.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupDetailUiState(
    val summary: GroupBalanceSummary? = null,
    val expenses: List<Expense> = emptyList(),
    val usersMap: Map<String, User> = emptyMap(),
    val userSettings: UserSettings = UserSettings(),
    val currentUser: User? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val repository: SplitterRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val groupId: String = checkNotNull(savedStateHandle["groupId"])

    val uiState: StateFlow<GroupDetailUiState> = combine(
        repository.getGroupBalanceSummary(groupId),
        repository.getGroupExpenses(groupId),
        repository.getAllUsers(),
        repository.getUserSettings(),
        repository.getCurrentUser()
    ) { summary, expenses, users, settings, currentUser ->
        GroupDetailUiState(
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
        initialValue = GroupDetailUiState(isLoading = true)
    )

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            repository.deleteExpense(expenseId)
        }
    }
}
