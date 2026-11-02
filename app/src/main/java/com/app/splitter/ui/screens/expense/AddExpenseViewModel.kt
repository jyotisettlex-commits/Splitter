package com.app.splitter.ui.screens.expense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.splitter.data.repository.SplitterRepository
import com.app.splitter.domain.model.ExpenseCategory
import com.app.splitter.domain.model.ExpensePayer
import com.app.splitter.domain.model.ExpenseSplit
import com.app.splitter.domain.model.Group
import com.app.splitter.domain.model.SplitType
import com.app.splitter.domain.model.User
import com.app.splitter.domain.model.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExpenseInputData(
    val description: String,
    val amountStr: String,
    val category: ExpenseCategory,
    val selectedGroupId: String?,
    val selectedFriendIds: Set<String>
)

data class SplitConfigData(
    val paidByUserId: String,
    val splitType: SplitType,
    val exactAmounts: Map<String, String>,
    val percentages: Map<String, String>,
    val shares: Map<String, String>
)

data class RepositoryData(
    val groups: List<Group>,
    val friends: List<User>,
    val settings: UserSettings,
    val currentUser: User?
)

data class StatusData(
    val isSaving: Boolean,
    val error: String?
)

data class AddExpenseUiState(
    val description: String = "",
    val amountStr: String = "",
    val category: ExpenseCategory = ExpenseCategory.GENERAL,
    val selectedGroupId: String? = null,
    val selectedFriendIds: Set<String> = emptySet(),
    val groups: List<Group> = emptyList(),
    val friends: List<User> = emptyList(),
    val availableParticipants: List<User> = emptyList(),
    val paidByUserId: String = "user_me",
    val splitType: SplitType = SplitType.EQUAL,
    val exactAmounts: Map<String, String> = emptyMap(),
    val percentages: Map<String, String> = emptyMap(),
    val shares: Map<String, String> = emptyMap(),
    val userSettings: UserSettings = UserSettings(),
    val currentUser: User? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val repository: SplitterRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialGroupId: String? = savedStateHandle["groupId"]
    private val initialFriendId: String? = savedStateHandle["friendId"]
    private val initialExpenseId: String? = savedStateHandle["expenseId"]

    private val _description = MutableStateFlow("")
    private val _amountStr = MutableStateFlow("")
    private val _category = MutableStateFlow(ExpenseCategory.GENERAL)
    private val _selectedGroupId = MutableStateFlow<String?>(initialGroupId)
    private val _selectedFriendIds = MutableStateFlow<Set<String>>(
        if (!initialFriendId.isNullOrEmpty()) setOf(initialFriendId) else emptySet()
    )
    private val _paidByUserId = MutableStateFlow("user_me")
    private val _splitType = MutableStateFlow(SplitType.EQUAL)
    private val _exactAmounts = MutableStateFlow<Map<String, String>>(emptyMap())
    private val _percentages = MutableStateFlow<Map<String, String>>(emptyMap())
    private val _shares = MutableStateFlow<Map<String, String>>(emptyMap())
    private val _isSaving = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    private val _inputData = combine(_description, _amountStr, _category, _selectedGroupId, _selectedFriendIds) { d, a, c, g, f ->
        ExpenseInputData(d, a, c, g, f)
    }

    private val _splitConfigData = combine(_paidByUserId, _splitType, _exactAmounts, _percentages, _shares) { p, st, ex, pc, sh ->
        SplitConfigData(p, st, ex, pc, sh)
    }

    private val _repoData = combine(repository.getGroups(), repository.getFriends(), repository.getUserSettings(), repository.getCurrentUser()) { groups, friends, settings, currentUser ->
        RepositoryData(groups, friends, settings, currentUser)
    }

    private val _statusData = combine(_isSaving, _error) { saving, err ->
        StatusData(saving, err)
    }

    val uiState: StateFlow<AddExpenseUiState> = combine(
        _inputData,
        _splitConfigData,
        _repoData,
        _statusData
    ) { input, config, repo, status ->
        val participants = mutableListOf<User>()
        repo.currentUser?.let { participants.add(it) }

        if (input.selectedGroupId != null) {
            val group = repo.groups.firstOrNull { it.id == input.selectedGroupId }
            if (group != null) {
                val memberUsers = (group.memberIds).mapNotNull { memberId ->
                    if (memberId == repo.currentUser?.id) repo.currentUser
                    else repo.friends.firstOrNull { it.id == memberId }
                }
                participants.clear()
                participants.addAll(memberUsers.distinctBy { it.id })
            }
        } else {
            val selectedFriends = repo.friends.filter { input.selectedFriendIds.contains(it.id) }
            participants.addAll(selectedFriends)
        }

        AddExpenseUiState(
            description = input.description,
            amountStr = input.amountStr,
            category = input.category,
            selectedGroupId = input.selectedGroupId,
            selectedFriendIds = input.selectedFriendIds,
            groups = repo.groups,
            friends = repo.friends,
            availableParticipants = participants.distinctBy { it.id },
            paidByUserId = if (config.paidByUserId.isEmpty()) (repo.currentUser?.id ?: "user_me") else config.paidByUserId,
            splitType = config.splitType,
            exactAmounts = config.exactAmounts,
            percentages = config.percentages,
            shares = config.shares,
            userSettings = repo.settings,
            currentUser = repo.currentUser,
            isEditing = !initialExpenseId.isNullOrEmpty(),
            isSaving = status.isSaving,
            error = status.error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddExpenseUiState()
    )

    init {
        viewModelScope.launch {
            val me = repository.getCurrentUserOneShot()
            if (me != null) {
                _paidByUserId.value = me.id
            }
            if (!initialExpenseId.isNullOrEmpty()) {
                val existing = repository.getExpenseById(initialExpenseId)
                if (existing != null) {
                    _description.value = existing.description
                    _amountStr.value = existing.amount.toString()
                    _category.value = existing.category
                    _selectedGroupId.value = existing.groupId
                    _paidByUserId.value = existing.payers.firstOrNull()?.userId ?: (me?.id ?: "user_me")
                    _splitType.value = existing.splitType
                }
            }
        }
    }

    fun onDescriptionChanged(v: String) { _description.value = v; _error.value = null }
    fun onAmountChanged(v: String) { _amountStr.value = v; _error.value = null }
    fun onCategorySelected(cat: ExpenseCategory) { _category.value = cat }
    fun onGroupSelected(groupId: String?) { _selectedGroupId.value = groupId }
    fun toggleFriendSelection(friendId: String) {
        val current = _selectedFriendIds.value.toMutableSet()
        if (current.contains(friendId)) current.remove(friendId) else current.add(friendId)
        _selectedFriendIds.value = current
    }
    fun onPaidByChanged(userId: String) { _paidByUserId.value = userId }
    fun onSplitTypeChanged(st: SplitType) { _splitType.value = st; _error.value = null }
    fun onExactAmountChanged(userId: String, v: String) {
        _exactAmounts.value = _exactAmounts.value + (userId to v)
    }
    fun onPercentageChanged(userId: String, v: String) {
        _percentages.value = _percentages.value + (userId to v)
    }
    fun onShareChanged(userId: String, v: String) {
        _shares.value = _shares.value + (userId to v)
    }

    fun saveExpense(onSuccess: () -> Unit) {
        val state = uiState.value
        val desc = state.description.trim()
        val totalAmount = state.amountStr.toDoubleOrNull() ?: 0.0

        if (desc.isEmpty()) {
            _error.value = "Description cannot be empty"
            return
        }
        if (totalAmount <= 0) {
            _error.value = "Please enter a valid amount greater than 0"
            return
        }
        if (state.availableParticipants.isEmpty()) {
            _error.value = "Please select participants"
            return
        }

        val payers = listOf(ExpensePayer(state.paidByUserId, totalAmount))
        val participantIds = state.availableParticipants.map { it.id }

        val customExacts = state.exactAmounts.mapValues { it.value.toDoubleOrNull() ?: 0.0 }
        val customPercentages = state.percentages.mapValues { it.value.toDoubleOrNull() ?: 0.0 }
        val customShares = state.shares.mapValues { it.value.toIntOrNull() ?: 1 }

        if (state.splitType == SplitType.EXACT) {
            val sumExact = customExacts.values.sum()
            if (Math.abs(sumExact - totalAmount) > 0.05) {
                _error.value = "Sum of exact amounts (${state.userSettings.currencySymbol}${String.format("%.2f", sumExact)}) must equal total amount (${state.userSettings.currencySymbol}${String.format("%.2f", totalAmount)})"
                return
            }
        } else if (state.splitType == SplitType.PERCENTAGE) {
            val sumPerc = customPercentages.values.sum()
            if (Math.abs(sumPerc - 100.0) > 0.1) {
                _error.value = "Percentages sum to ${String.format("%.1f", sumPerc)}% (must be 100%)"
                return
            }
        }

        val splits = com.app.splitter.domain.usecase.SplitCalculator.calculateSplits(
            totalAmount = totalAmount,
            participantIds = participantIds,
            splitType = state.splitType,
            customExacts = customExacts,
            customPercentages = customPercentages,
            customShares = customShares
        )

        viewModelScope.launch {
            _isSaving.value = true
            try {
                repository.addExpense(
                    description = desc,
                    amount = totalAmount,
                    currencySymbol = state.userSettings.currencySymbol,
                    groupId = state.selectedGroupId,
                    createdUserId = state.currentUser?.id ?: "user_me",
                    splitType = state.splitType,
                    category = state.category,
                    payers = payers,
                    splits = splits
                )
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to save expense"
            } finally {
                _isSaving.value = false
            }
        }
    }
}
