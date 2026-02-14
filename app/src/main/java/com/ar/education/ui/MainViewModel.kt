package com.ar.education.ui

import androidx.lifecycle.*
import com.ar.education.data.*
import com.ar.education.progress.ProgressRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for Main Activity
 */
class MainViewModel(private val progressRepository: ProgressRepository) : AndroidViewModel() {
    
    private val _lessons = MutableLiveData<List<Lesson>>()
    val lessons: LiveData<List<Lesson>> = _lessons
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private var allLessons = emptyList<Lesson>()
    private var currentFilter: Subject? = null
    
    init {
        loadAllLessons()
    }
    
    fun loadAllLessons() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val repository = LessonRepository(getApplication())
                val result = repository.getAllLessons()
                result.onSuccess { lessons ->
                    allLessons = lessons
                    applyFilter()
                }.onFailure { exception ->
                    _error.value = "Failed to load lessons: ${exception.message}"
                }
            } catch (e: Exception) {
                _error.value = "Error loading lessons: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun filterBySubject(subject: Subject?) {
        currentFilter = subject
        applyFilter()
    }
    
    private fun applyFilter() {
        val filteredLessons = if (currentFilter == null) {
            allLessons
        } else {
            allLessons.filter { it.subject == currentFilter }
        }
        _lessons.value = filteredLessons.sortedWith(
            compareBy<Lesson> { it.subject.displayName }
                .thenBy { it.difficulty.level }
                .thenBy { it.title }
        )
    }
    
    fun refreshData() {
        loadAllLessons()
    }
    
    fun clearError() {
        _error.value = null
    }
}

/**
 * Factory for MainViewModel
 */
class MainViewModelFactory(private val progressRepository: ProgressRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(progressRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}