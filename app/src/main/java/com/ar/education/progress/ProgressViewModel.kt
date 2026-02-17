package com.ar.education.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ar.education.data.Lesson
import com.ar.education.data.LessonProgress
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProgressViewModel(application: Application, private val progressRepository: ProgressRepository) : AndroidViewModel(application) {

    private val _userProgress = MutableLiveData<Pair<List<String>, List<String>>>()
    val userProgress: LiveData<Pair<List<String>, List<String>>> = _userProgress

    fun loadUserProgress(userId: String) {
        viewModelScope.launch {
            val allProgress = progressRepository.getAllProgress(userId).first()
            val bookmarkedLessons = allProgress.filter { it.bookmarked }.map { it.lessonId }
            val completedLessons = allProgress.filter { it.isCompleted }.map { it.lessonId }

            _userProgress.value = Pair(bookmarkedLessons, completedLessons)
        }
    }

    fun getProgressForLesson(lessonId: String): LiveData<LessonProgress?> {
        val progressLiveData = MutableLiveData<LessonProgress?>()
        viewModelScope.launch {
            progressLiveData.value = progressRepository.getLessonProgress(lessonId)
        }
        return progressLiveData
    }

    fun getCompletionPercentage(lesson: Lesson): Float {
        val progress = userProgress.value?.second?.find { it == lesson.id }
        if (progress != null) {
            return 100f
        }
        return 0f
    }

    fun getTotalCompletedLessons(): Int {
        return userProgress.value?.second?.size ?: 0
    }

    fun getTotalBookmarkedLessons(): Int {
        return userProgress.value?.first?.size ?: 0
    }
}