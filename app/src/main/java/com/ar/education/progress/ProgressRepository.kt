package com.ar.education.progress

import android.content.Context
import androidx.room.Room
import com.ar.education.data.LessonProgress
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for managing user progress
 */
class ProgressRepository(context: Context) {
    
    private val database = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "ar_education_db"
    ).build()
    
    private val progressDao = database.lessonProgressDao()
    private val gson = Gson()
    
    /**
     * Get progress for a specific lesson
     */
    suspend fun getLessonProgress(lessonId: String): LessonProgress? {
        val entity = progressDao.getProgress(lessonId)
        return entity?.toLessonProgress()
    }
    
    /**
     * Get all progress as a Flow
     */
    fun getAllProgress(userId: String): Flow<List<LessonProgress>> {
        return progressDao.getAllProgress(userId).map { entities ->
            entities.map { it.toLessonProgress() }
        }
    }
    
    /**
     * Get completed lessons
     */
    fun getCompletedLessons(userId: String): Flow<List<LessonProgress>> {
        return progressDao.getCompletedLessons(userId).map { entities ->
            entities.map { it.toLessonProgress() }
        }
    }
    
    /**
     * Get bookmarked lessons
     */
    fun getBookmarkedLessons(): Flow<List<LessonProgress>> {
        return progressDao.getBookmarkedLessons().map { entities ->
            entities.map { it.toLessonProgress() }
        }
    }
    
    /**
     * Save or update progress
     */
    suspend fun saveProgress(progress: LessonProgress) {
        progressDao.insertProgress(progress.toEntity())
    }
    
    /**
     * Mark a lesson step as completed
     */
    suspend fun markStepCompleted(lessonId: String, stepNumber: Int, userId: String) {
        val currentProgress = getLessonProgress(lessonId)
        val completedSteps = currentProgress?.completedSteps?.toMutableList() ?: mutableListOf()
        
        if (!completedSteps.contains(stepNumber)) {
            completedSteps.add(stepNumber)
            
            val newProgress = LessonProgress(
                lessonId = lessonId,
                userId = userId,
                completedSteps = completedSteps,
                quizScore = currentProgress?.quizScore ?: 0,
                quizAttempts = currentProgress?.quizAttempts ?: 0,
                isCompleted = currentProgress?.isCompleted ?: false,
                lastAccessed = System.currentTimeMillis(),
                timeSpent = currentProgress?.timeSpent ?: 0,
                bookmarked = currentProgress?.bookmarked ?: false
            )
            saveProgress(newProgress)
        }
    }
    
    /**
     * Save quiz result
     */
    suspend fun saveQuizResult(lessonId: String, score: Int, userId: String) {
        val currentProgress = getLessonProgress(lessonId)
        val newAttempts = (currentProgress?.quizAttempts ?: 0) + 1
        
        val newProgress = LessonProgress(
            lessonId = lessonId,
            userId = userId,
            completedSteps = currentProgress?.completedSteps ?: emptyList(),
            quizScore = score,
            quizAttempts = newAttempts,
            isCompleted = score >= 70, // Assuming 70% passing score
            lastAccessed = System.currentTimeMillis(),
            timeSpent = currentProgress?.timeSpent ?: 0,
            bookmarked = currentProgress?.bookmarked ?: false
        )
        saveProgress(newProgress)
    }
    
    /**
     * Toggle bookmark status
     */
    suspend fun toggleBookmark(lessonId: String, userId: String) {
        val currentProgress = getLessonProgress(lessonId)
        
        val newProgress = LessonProgress(
            lessonId = lessonId,
            userId = userId,
            completedSteps = currentProgress?.completedSteps ?: emptyList(),
            quizScore = currentProgress?.quizScore ?: 0,
            quizAttempts = currentProgress?.quizAttempts ?: 0,
            isCompleted = currentProgress?.isCompleted ?: false,
            lastAccessed = System.currentTimeMillis(),
            timeSpent = currentProgress?.timeSpent ?: 0,
            bookmarked = !(currentProgress?.bookmarked ?: false)
        )
        saveProgress(newProgress)
    }
    
    /**
     * Get completed lesson count
     */
    fun getCompletedLessonCount(userId: String): Flow<Int> {
        return progressDao.getCompletedLessonCount(userId)
    }
    
    /**
     * Get average quiz score
     */
    fun getAverageQuizScore(userId: String): Flow<Float?> {
        return progressDao.getAverageQuizScore(userId)
    }
    
    /**
     * Reset progress for a lesson
     */
    suspend fun resetProgress(lessonId: String) {
        progressDao.deleteProgressById(lessonId)
    }
    
    // Extension functions for conversion
    private fun LessonProgressEntity.toLessonProgress(): LessonProgress {
        val completedStepsList: List<Int> = try {
            gson.fromJson(completedSteps, object : TypeToken<List<Int>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
        
        return LessonProgress(
            lessonId = lessonId,
            userId = userId,
            completedSteps = completedStepsList,
            quizScore = quizScore,
            quizAttempts = quizAttempts,
            isCompleted = isCompleted,
            lastAccessed = lastAccessed,
            timeSpent = timeSpent,
            bookmarked = bookmarked
        )
    }
    
    private fun LessonProgress.toEntity(): LessonProgressEntity {
        return LessonProgressEntity(
            lessonId = lessonId,
            userId = userId,
            completedSteps = gson.toJson(completedSteps),
            quizScore = quizScore,
            quizAttempts = quizAttempts,
            isCompleted = isCompleted,
            lastAccessed = lastAccessed,
            timeSpent = timeSpent,
            bookmarked = bookmarked
        )
    }
}