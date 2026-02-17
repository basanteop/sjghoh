package com.ar.education.data

data class LessonProgress(
    val lessonId: String,
    val userId: String,
    val completedSteps: List<Int> = emptyList(),
    val quizScore: Int = 0,
    val quizAttempts: Int = 0,
    val isCompleted: Boolean = false,
    val lastAccessed: Long = 0,
    val timeSpent: Long = 0,
    val bookmarked: Boolean = false
)
