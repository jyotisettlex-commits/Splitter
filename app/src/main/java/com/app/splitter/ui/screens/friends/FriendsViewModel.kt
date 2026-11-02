package com.app.splitter.ui.screens.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.splitter.data.repository.SplitterRepository
import com.app.splitter.domain.model.User
import com.app.splitter.domain.model.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class FriendItemUiState(
    val friend: User,
    val netBalance: Double = 0.0
)

data class FriendsUiState(
    val friends: List<FriendItemUiState> = emptyList(),
    val totalFriendsBalance: Double = 0.0,
    val userSettings: UserSettings = UserSettings(),
    val isLoading: Boolean = false
)

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val repository: SplitterRepository
) : ViewModel() {

    val uiState: StateFlow<FriendsUiState> = combine(
        repository.getFriends(),
        repository.getAllExpenses(),
        repository.getUserSettings(),
        repository.getCurrentUser()
    ) { friendsList, allExpenses, settings, currentUser ->
        val meId = currentUser?.id ?: "user_me"

        val friendItemStates = friendsList.map { friend ->
            var netBalance = 0.0
            for (exp in allExpenses) {
                val payersMap = exp.payers.associate { it.userId to it.amountPaid }
                val splitsMap = exp.splits.associate { it.userId to it.amountOwed }

                val participantIds = (payersMap.keys + splitsMap.keys).toSet()
                if (participantIds.contains(meId) && participantIds.contains(friend.id)) {
                    val mePaid = payersMap[meId] ?: 0.0
                    val meOwed = splitsMap[meId] ?: 0.0
                    // For shared expense involving me & friend:
                    // If non-group expense, net balance equals (mePaid - meOwed).
                    // If group expense, calculate pairwise balance proportional or direct.
                    if (exp.groupId == null) {
                        netBalance += (mePaid - meOwed)
                    } else {
                        val friendPaid = payersMap[friend.id] ?: 0.0
                        val friendOwed = splitsMap[friend.id] ?: 0.0
                        // Pairwise net between me & friend
                        val totalPeople = participantIds.size.toDouble().coerceAtLeast(1.0)
                        val netChange = (mePaid - meOwed) / (totalPeople - 1).coerceAtLeast(1.0)
                        netBalance += netChange
                    }
                }
            }
            FriendItemUiState(friend = friend, netBalance = netBalance)
        }.sortedBy { it.friend.name }

        val totalBalance = friendItemStates.sumOf { it.netBalance }

        FriendsUiState(
            friends = friendItemStates,
            totalFriendsBalance = totalBalance,
            userSettings = settings,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FriendsUiState(isLoading = true)
    )
}
