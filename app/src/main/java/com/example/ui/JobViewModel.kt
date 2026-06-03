package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.BookmarkedJob
import com.example.data.JobAlert
import com.example.data.JobRepository
import com.example.data.MockJobAlertData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JobViewModel(private val repository: JobRepository) : ViewModel() {

    private val _activeTab = MutableStateFlow("home")
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<JobAlert>>(emptyList())
    val searchResults: StateFlow<List<JobAlert>> = _searchResults.asStateFlow()

    private val _selectedAlert = MutableStateFlow<JobAlert?>(null)
    val selectedAlert: StateFlow<JobAlert?> = _selectedAlert.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    val bookmarkedJobs: StateFlow<List<BookmarkedJob>> = repository.allBookmarks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setActiveTab(tab: String) {
        _activeTab.value = tab
        // Clear search when switching tabs to make experience smooth
        if (tab != "search") {
            _searchQuery.value = ""
            _searchResults.value = emptyList()
        }
    }

    fun submitSearch(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
        } else {
            val q = query.trim().lowercase()
            _searchResults.value = MockJobAlertData.allAlerts.filter {
                it.title.lowercase().contains(q) ||
                it.dept.lowercase().contains(q) ||
                it.category.lowercase().contains(q)
            }
            _activeTab.value = "search"
        }
    }

    fun selectAlert(alert: JobAlert?) {
        _selectedAlert.value = alert
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun toggleBookmark(alert: JobAlert) {
        viewModelScope.launch {
            val isBookmarkedNow = bookmarkedJobs.value.any { it.title == alert.title }
            if (isBookmarkedNow) {
                repository.deleteByTitle(alert.title)
            } else {
                repository.insert(alert.toBookmarkedJob())
            }
        }
    }
}

class JobViewModelFactory(private val repository: JobRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JobViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JobViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
