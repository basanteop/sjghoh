package com.ar.education.progress

import androidx.lifecycle.*
import com.ar.education.data.Lesson
import com.ar.education.data.LessonProgress
import com.ar.education.ui.ProgressAdapter
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel for Progress Activity
 */
class ProgressViewModel(private val progressRepository: ProgressRepository) : AndroidViewModel() {
    
    private val _progressList = MutableLiveData<List<ProgressAdapter.ProgressItem>>()
    val progressList: LiveData<List<ProgressAdapter.ProgressItem>> = _progressList
    
    private val _completedCount = MutableLiveData(0)
    val completedCount: LiveData<Int> = _completedCount
    
    private val _averageScore = MutableLiveData<Float?>()
    val averageScore: LiveData<Float?> = _averageScore
    
    private val _bookmarkedCount = MutableLiveData(0)
    val bookmarkedCount: LiveData<Int> = _bookmarkedCount
    
    init {
        loadProgress()
    }
    
    fun loadProgress() {
        viewModelScope.launch {
            try {
                val userId = getCurrentUserId()
                
                // Load progress and convert to ProgressItem
                val lessons = getAllLessons()
                val userProgress = progressRepository.getAllProgress(userId)
                
                // Convert to UI items
                val progressItems = mutableListOf<ProgressAdapter.ProgressItem>()
                
                lessons.forEach { lesson ->
                    val progress = userProgress.find { it.lessonId == lesson.id }
                    progress?.let {
                        val progressItem = ProgressAdapter.ProgressItem(
                            lessonId = lesson.id,
                            lessonTitle = lesson.title,
                            subject = lesson.subject,
                            completedSteps = it.completedSteps.size,
                            totalSteps = lesson.labSteps.size,
                            quizScore = it.quizScore,
                            isCompleted = it.isCompleted,
                            lastAccessed = it.lastAccessed
                        )
                        progressItems.add(progressItem)
                    }
                }
                
                _progressList.value = progressItems.sortedByDescending { it.lastAccessed }
                
                // Calculate statistics
                val completed = progressItems.count { it.isCompleted }
                _completedCount.value = completed
                
                val avgScore = if (progressItems.any { it.quizScore > 0 }) {
                    progressItems.filter { it.quizScore > 0 }.map { it.quizScore }.average().toFloat()
                } else null
                _averageScore.value = avgScore
                
                val bookmarked = userProgress.count { it.bookmarked }
                _bookmarkedCount.value = bookmarked
                
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    private suspend fun getAllLessons(): List<Lesson> {
        val repository = LessonRepository(getApplication())
        return repository.getAllLessons().getOrThrow()
    }
    
    private fun getCurrentUserId(): String {
        // In a real app, this would get from authentication
        return "default_user"
    }
}

/**
 * Factory for ProgressViewModel
 */
class ProgressViewModelFactory(private val progressRepository: ProgressRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProgressViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProgressViewModel(progressRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}