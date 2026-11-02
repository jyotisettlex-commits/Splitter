package com.app.splitter.ui.screens.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.splitter.data.repository.SplitterRepository
import com.app.splitter.domain.model.BalanceSummary
import com.app.splitter.domain.model.Group
import com.app.splitter.domain.model.GroupCategory
import com.app.splitter.domain.model.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class GroupItemUiState(
    val group: Group,
    val userNetBalance: Double = 0.0,
    val totalSpend: Double = 0.0
)

data class GroupsUiState(
    val groups: List<GroupItemUiState> = emptyList(),
    val balanceSummary: BalanceSummary = BalanceSummary(),
    val userSettings: UserSettings = UserSettings(),
    val isLoading: Boolean = false
)

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val repository: SplitterRepository
) : ViewModel() {

    val uiState: StateFlow<GroupsUiState> = combine(
        repository.getGroups(),
        repository.getGlobalBalanceSummary(),
        repository.getUserSettings(),
        repository.getAllExpenses()
    ) { groups, globalBalance, settings, allExpenses ->
        val groupItemStates = groups.map { group ->
            val groupExpenses = allExpenses.filter { it.groupId == group.id }
            val totalSpend = groupExpenses.sumOf { it.amount }

            val currentUser = repository.getCurrentUserOneShot()
            val meId = currentUser?.id ?: "user_me"

            var userNet = 0.0
            for (exp in groupExpenses) {
                val paidByMe = exp.payers.firstOrNull { it.userId == meId }?.amountPaid ?: 0.0
                val owedByMe = exp.splits.firstOrNull { it.userId == meId }?.amountOwed ?: 0.0
                userNet += (paidByMe - owedByMe)
            }

            GroupItemUiState(
                group = group,
                userNetBalance = userNet,
                totalSpend = totalSpend
            )
        }

        GroupsUiState(
            groups = groupItemStates,
            balanceSummary = globalBalance,
            userSettings = settings,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GroupsUiState(isLoading = true)
    )
}
