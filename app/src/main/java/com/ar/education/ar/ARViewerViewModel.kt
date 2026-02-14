package com.ar.education.ar

import androidx.lifecycle.*
import com.ar.education.data.*
import com.ar.education.progress.ProgressRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for AR Viewer Activity
 */
class ARViewerViewModel(
    private val lessonId: String,
    private val progressRepository: ProgressRepository
) : ViewModel() {
    
    private val _currentLesson = MutableLiveData<Lesson?>()
    val currentLesson: LiveData<Lesson?> = _currentLesson
    
    private val _currentStep = MutableLiveData(0)
    val currentStep: LiveData<Int> = _currentStep
    
    private val _progress = MutableLiveData<LessonProgress?>()
    val progress: LiveData<LessonProgress?> = _progress
    
    private var lesson: Lesson? = null
    
    init {
        loadProgress()
    }
    
    fun loadLesson() {
        viewModelScope.launch {
            val repository = LessonRepository(getApplication())
            val result = repository.getLessonById(lessonId)
            result.onSuccess { lessonData ->
                lesson = lessonData
                _currentLesson.value = lessonData
            }
        }
    }
    
    private fun loadProgress() {
        viewModelScope.launch {
            val progressData = progressRepository.getLessonProgress(lessonId)
            _progress.value = progressData
            
            // Set current step to the last completed step
            progressData?.let {
                if (it.completedSteps.isNotEmpty()) {
                    _currentStep.value = it.completedSteps.maxOrNull()?.minus(1) ?: 0
                }
            }
        }
    }
    
    fun nextStep() {
        val lessonData = lesson ?: return
        val current = _currentStep.value ?: 0
        if (current < lessonData.labSteps.size - 1) {
            _currentStep.value = current + 1
        }
    }
    
    fun previousStep() {
        val current = _currentStep.value ?: 0
        if (current > 0) {
            _currentStep.value = current - 1
        }
    }
    
    fun markStepCompleted(stepNumber: Int) {
        viewModelScope.launch {
            progressRepository.markStepCompleted(lessonId, stepNumber, getCurrentUserId())
            loadProgress()
        }
    }
    
    fun toggleBookmark() {
        viewModelScope.launch {
            progressRepository.toggleBookmark(lessonId, getCurrentUserId())
            loadProgress()
        }
    }
    
    private fun getCurrentUserId(): String {
        // In a real app, this would get from authentication
        return "default_user"
    }
}

/**
 * Factory for ARViewerViewModel
 */
class ARViewerViewModelFactory(
    private val lessonId: String,
    private val progressRepository: ProgressRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ARViewerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ARViewerViewModel(lessonId, progressRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}