package com.app.splitter.ui.screens.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.splitter.data.repository.SplitterRepository
import com.app.splitter.domain.model.GroupCategory
import com.app.splitter.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FormParams(
    val name: String,
    val category: GroupCategory,
    val selectedMemberIds: Set<String>
)

data class StatusParams(
    val isSaving: Boolean,
    val error: String?
)

data class CreateGroupUiState(
    val name: String = "",
    val selectedCategory: GroupCategory = GroupCategory.HOME,
    val friends: List<User> = emptyList(),
    val selectedMemberIds: Set<String> = emptySet(),
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val repository: SplitterRepository
) : ViewModel() {

    private val _name = MutableStateFlow("")
    private val _category = MutableStateFlow(GroupCategory.HOME)
    private val _selectedMemberIds = MutableStateFlow<Set<String>>(emptySet())
    private val _isSaving = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    private val _formParams = combine(_name, _category, _selectedMemberIds) { name, category, members ->
        FormParams(name, category, members)
    }

    private val _statusParams = combine(_isSaving, _error) { saving, err ->
        StatusParams(saving, err)
    }

    val uiState: StateFlow<CreateGroupUiState> = combine(
        _formParams,
        repository.getFriends(),
        _statusParams
    ) { form, friends, status ->
        CreateGroupUiState(
            name = form.name,
            selectedCategory = form.category,
            friends = friends,
            selectedMemberIds = form.selectedMemberIds,
            isSaving = status.isSaving,
            error = status.error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CreateGroupUiState()
    )

    fun onNameChanged(newName: String) {
        _name.value = newName
    }

    fun onCategorySelected(category: GroupCategory) {
        _category.value = category
    }

    fun toggleMemberSelection(userId: String) {
        val current = _selectedMemberIds.value.toMutableSet()
        if (current.contains(userId)) {
            current.remove(userId)
        } else {
            current.add(userId)
        }
        _selectedMemberIds.value = current
    }

    fun createGroup(onSuccess: () -> Unit) {
        val nameStr = _name.value.trim()
        if (nameStr.isEmpty()) {
            _error.value = "Group name cannot be empty"
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            try {
                repository.createGroup(
                    name = nameStr,
                    category = _category.value,
                    memberIds = _selectedMemberIds.value.toList()
                )
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to create group"
            } finally {
                _isSaving.value = false
            }
        }
    }
}
