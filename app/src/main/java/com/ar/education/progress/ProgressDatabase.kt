package com.ar.education.progress

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Room entity for storing lesson progress
 */
@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey
    val lessonId: String,
    val userId: String,
    val completedSteps: String, // JSON string of List<Int>
    val quizScore: Int,
    val quizAttempts: Int,
    val isCompleted: Boolean,
    val lastAccessed: Long,
    val timeSpent: Long,
    val bookmarked: Boolean
)

/**
 * DAO for lesson progress operations
 */
@Dao
interface LessonProgressDao {
    
    @Query("SELECT * FROM lesson_progress WHERE lessonId = :lessonId")
    suspend fun getProgress(lessonId: String): LessonProgressEntity?
    
    @Query("SELECT * FROM lesson_progress WHERE userId = :userId")
    fun getAllProgress(userId: String): Flow<List<LessonProgressEntity>>
    
    @Query("SELECT * FROM lesson_progress WHERE userId = :userId AND isCompleted = 1")
    fun getCompletedLessons(userId: String): Flow<List<LessonProgressEntity>>
    
    @Query("SELECT * FROM lesson_progress WHERE bookmarked = 1")
    fun getBookmarkedLessons(): Flow<List<LessonProgressEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: LessonProgressEntity)
    
    @Update
    suspend fun updateProgress(progress: LessonProgressEntity)
    
    @Delete
    suspend fun deleteProgress(progress: LessonProgressEntity)
    
    @Query("DELETE FROM lesson_progress WHERE lessonId = :lessonId")
    suspend fun deleteProgressById(lessonId: String)
    
    @Query("SELECT COUNT(*) FROM lesson_progress WHERE userId = :userId AND isCompleted = 1")
    fun getCompletedLessonCount(userId: String): Flow<Int>
    
    @Query("SELECT AVG(quizScore) FROM lesson_progress WHERE userId = :userId AND quizScore > 0")
    fun getAverageQuizScore(userId: String): Flow<Float?>
}

/**
 * Room database for the AR education app
 */
@Database(
    entities = [LessonProgressEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessonProgressDao(): LessonProgressDao
}