package com.app.splitter.ui.screens.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.splitter.data.repository.SplitterRepository
import com.app.splitter.domain.model.ActivityLog
import com.app.splitter.domain.model.Group
import com.app.splitter.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class ActivityFilter {
    ALL, BY_GROUP, BY_PERSON
}

data class FilterParams(
    val filter: ActivityFilter,
    val selectedGroupId: String?,
    val selectedUserId: String?
)

data class ActivityUiState(
    val activities: List<ActivityLog> = emptyList(),
    val filter: ActivityFilter = ActivityFilter.ALL,
    val selectedGroupId: String? = null,
    val selectedUserId: String? = null,
    val groups: List<Group> = emptyList(),
    val friends: List<User> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val repository: SplitterRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(ActivityFilter.ALL)
    private val _selectedGroupId = MutableStateFlow<String?>(null)
    private val _selectedUserId = MutableStateFlow<String?>(null)

    private val _filterParams = combine(_filter, _selectedGroupId, _selectedUserId) { f, g, u ->
        FilterParams(f, g, u)
    }

    val uiState: StateFlow<ActivityUiState> = combine(
        repository.getActivities(),
        _filterParams,
        repository.getGroups(),
        repository.getFriends()
    ) { logs, params, groups, friends ->
        val filteredLogs = logs.filter { log ->
            when (params.filter) {
                ActivityFilter.ALL -> true
                ActivityFilter.BY_GROUP -> params.selectedGroupId == null || log.groupId == params.selectedGroupId
                ActivityFilter.BY_PERSON -> params.selectedUserId == null || log.relatedUserId == params.selectedUserId
            }
        }
        ActivityUiState(
            activities = filteredLogs,
            filter = params.filter,
            selectedGroupId = params.selectedGroupId,
            selectedUserId = params.selectedUserId,
            groups = groups,
            friends = friends,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ActivityUiState(isLoading = true)
    )

    fun setFilter(filter: ActivityFilter) {
        _filter.value = filter
    }

    fun setSelectedGroup(groupId: String?) {
        _selectedGroupId.value = groupId
    }

    fun setSelectedUser(userId: String?) {
        _selectedUserId.value = userId
    }
}
