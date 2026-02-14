package com.ar.education.ui

import androidx.lifecycle.*
import com.ar.education.data.Lesson
import com.ar.education.data.LessonProgress
import com.ar.education.progress.ProgressRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for LessonDetail Activity
 */
class LessonDetailViewModel(
    private val lessonId: String,
    private val progressRepository: ProgressRepository
) : ViewModel() {
    
    private val _lesson = MutableLiveData<Lesson?>()
    val lesson: LiveData<Lesson?> = _lesson
    
    private val _progress = MutableLiveData<LessonProgress?>()
    val progress: LiveData<LessonProgress?> = _progress
    
    init {
        loadLesson()
        loadProgress()
    }
    
    fun loadLesson() {
        viewModelScope.launch {
            val repository = LessonRepository(getApplication())
            val result = repository.getLessonById(lessonId)
            result.onSuccess { lessonData ->
                _lesson.value = lessonData
            }
        }
    }
    
    private fun loadProgress() {
        viewModelScope.launch {
            val progressData = progressRepository.getLessonProgress(lessonId)
            _progress.value = progressData
        }
    }
}

/**
 * Factory for LessonDetailViewModel
 */
class LessonDetailViewModelFactory(
    private val lessonId: String,
    private val progressRepository: ProgressRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LessonDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LessonDetailViewModel(lessonId, progressRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}