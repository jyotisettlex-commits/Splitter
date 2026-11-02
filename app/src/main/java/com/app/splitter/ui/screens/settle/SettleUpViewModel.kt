package com.app.splitter.ui.screens.settle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.splitter.data.repository.SplitterRepository
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

data class SettleUpUiState(
    val payerId: String = "",
    val payeeId: String = "",
    val amountStr: String = "",
    val groupId: String? = null,
    val availableUsers: List<User> = emptyList(),
    val currentUser: User? = null,
    val userSettings: UserSettings = UserSettings(),
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SettleUpViewModel @Inject constructor(
    private val repository: SplitterRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialGroupId: String? = savedStateHandle["groupId"]
    private val initialFriendId: String? = savedStateHandle["friendId"]

    private val _payerId = MutableStateFlow("")
    private val _payeeId = MutableStateFlow("")
    private val _amountStr = MutableStateFlow("")
    private val _isSaving = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettleUpUiState> = combine(
        combine(_payerId, _payeeId, _amountStr, _isSaving, _error) { p, py, a, s, e ->
            listOf(p, py, a, s, e)
        },
        combine(repository.getAllUsers(), repository.getCurrentUser(), repository.getUserSettings()) { users, currentUser, settings ->
            listOf(users, currentUser, settings)
        }
    ) { (params1, params2) ->
        val payerId = params1[0] as String
        val payeeId = params1[1] as String
        val amt = params1[2] as String
        val saving = params1[3] as Boolean
        val err = params1[4] as String?

        @Suppress("UNCHECKED_CAST")
        val users = params2[0] as List<User>
        val currentUser = params2[1] as User?
        val settings = params2[2] as UserSettings

        val meId = currentUser?.id ?: "user_me"

        val defaultPayer = if (payerId.isEmpty()) meId else payerId
        val defaultPayee = if (payeeId.isEmpty()) (initialFriendId ?: users.firstOrNull { it.id != meId }?.id ?: "") else payeeId

        SettleUpUiState(
            payerId = defaultPayer,
            payeeId = defaultPayee,
            amountStr = amt,
            groupId = initialGroupId,
            availableUsers = users,
            currentUser = currentUser,
            userSettings = settings,
            isSaving = saving,
            error = err
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettleUpUiState()
    )

    init {
        viewModelScope.launch {
            if (!initialFriendId.isNullOrEmpty()) {
                val friendSummary = repository.getFriendBalanceSummary(initialFriendId)
                friendSummary.collect { summary ->
                    if (summary != null) {
                        val net = summary.netBalance
                        val meId = repository.getCurrentUserOneShot()?.id ?: "user_me"
                        if (net < 0) {
                            // You owe friend
                            _payerId.value = meId
                            _payeeId.value = initialFriendId
                            _amountStr.value = String.format("%.2f", -net)
                        } else if (net > 0) {
                            // Friend owes you
                            _payerId.value = initialFriendId
                            _payeeId.value = meId
                            _amountStr.value = String.format("%.2f", net)
                        }
                    }
                }
            }
        }
    }

    fun onPayerChanged(id: String) { _payerId.value = id; _error.value = null }
    fun onPayeeChanged(id: String) { _payeeId.value = id; _error.value = null }
    fun onAmountChanged(v: String) { _amountStr.value = v; _error.value = null }

    fun confirmSettlement(onSuccess: () -> Unit) {
        val state = uiState.value
        val amount = state.amountStr.toDoubleOrNull() ?: 0.0

        if (state.payerId == state.payeeId) {
            _error.value = "Payer and Payee must be different people"
            return
        }
        if (amount <= 0) {
            _error.value = "Please enter a valid amount greater than 0"
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            try {
                repository.settleUp(
                    payerId = state.payerId,
                    payeeId = state.payeeId,
                    amount = amount,
                    groupId = state.groupId,
                    currencySymbol = state.userSettings.currencySymbol
                )
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to settle up"
            } finally {
                _isSaving.value = false
            }
        }
    }
}
