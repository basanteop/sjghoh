package com.ar.education.data

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a lesson in the AR education app
 */
data class Lesson(
    val id: String,
    val subject: Subject,
    val title: String,
    val description: String,
    val difficulty: Difficulty,
    val estimatedDuration: Int, // in minutes
    val markerId: String,
    val modelPath: String,
    val labSteps: List<LabStep>,
    val quiz: Quiz,
    val prerequisites: List<String> = emptyList(),
    val tags: List<String> = emptyList()
)

enum class Subject(val displayName: String) {
    @SerializedName("physics")
    PHYSICS("Physics"),
    
    @SerializedName("biology")
    BIOLOGY("Biology"),
    
    @SerializedName("chemistry")
    CHEMISTRY("Chemistry")
}

enum class Difficulty(val displayName: String, val level: Int) {
    @SerializedName("beginner")
    BEGINNER("Beginner", 1),
    
    @SerializedName("intermediate")
    INTERMEDIATE("Intermediate", 2),
    
    @SerializedName("advanced")
    ADVANCED("Advanced", 3)
}

/**
 * Data class representing a lab step with AR interaction
 */
data class LabStep(
    val stepNumber: Int,
    val title: String,
    val instruction: String,
    val modelHighlighting: ModelHighlighting? = null,
    val requiresInteraction: Boolean = false,
    val interactionType: InteractionType? = null,
    val expectedOutcome: String? = null,
    val imageUrl: String? = null,
    val audioUrl: String? = null
)

/**
 * Data class for model highlighting during steps
 */
data class ModelHighlighting(
    val objectId: String,
    val color: String, // hex color code
    val highlightDuration: Int = 3000 // milliseconds
)

enum class InteractionType {
    TAP, ROTATE, SCALE, DRAG, NONE
}

/**
 * Data class representing a quiz
 */
data class Quiz(
    val id: String,
    val title: String,
    val questions: List<QuizQuestion>,
    val passingScore: Int = 70,
    val timeLimit: Int = 0 // 0 means no time limit
)

/**
 * Data class representing a quiz question
 */
data class QuizQuestion(
    val id: String,
    val question: String,
    val questionType: QuestionType,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String? = null,
    val imageUrl: String? = null
)

enum class QuestionType {
    @SerializedName("multiple_choice")
    MULTIPLE_CHOICE,
    
    @SerializedName("true_false")
    TRUE_FALSE
}

/**
 * Data class for user progress tracking
 */
data class LessonProgress(
    val lessonId: String,
    val userId: String,
    val completedSteps: List<Int> = emptyList(),
    val quizScore: Int = 0,
    val quizAttempts: Int = 0,
    val isCompleted: Boolean = false,
    val lastAccessed: Long = System.currentTimeMillis(),
    val timeSpent: Long = 0, // in milliseconds
    val bookmarked: Boolean = false
)

/**
 * Data class for app configuration
 */
data class AppConfig(
    val arCoreRequired: Boolean = true,
    val offlineMode: Boolean = true,
    val maxModelSize: Long = 10 * 1024 * 1024, // 10MB
    val supportedSubjects: List<Subject> = listOf(Subject.PHYSICS, Subject.BIOLOGY, Subject.CHEMISTRY),
    val enableAnalytics: Boolean = false,
    val lowEndDeviceOptimization: Boolean = true
)