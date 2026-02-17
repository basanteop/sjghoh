package com.ar.education.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "lessons")
data class Lesson(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val description: String,
    val difficulty: String,
    val estimatedDuration: String,
    val markerId: String,
    val modelPath: String, // Path to the 3D model in assets
    val labSteps: List<LabStep>,
    val quiz: Quiz,
    val prerequisites: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    var bookmarked: Boolean = false,
    var completed: Boolean = false
) : Parcelable

@Parcelize
data class LabStep(
    val stepNumber: Int,
    val title: String,
    val instruction: String,
    val modelHighlighting: ModelHighlighting? = null, // Optional model highlighting info
    val requiresInteraction: Boolean = false,
    val interactionType: InteractionType? = null,
    val expectedOutcome: String? = null,
    val imageUrl: String? = null,
    val audioUrl: String? = null
) : Parcelable

@Parcelize
data class ModelHighlighting(
    val objectId: String,
    val color: String, // hex color code
    val highlightDuration: Int = 3000 // milliseconds
) : Parcelable

@Parcelize
enum class InteractionType : Parcelable {
    TAP, ROTATE, SCALE, DRAG, NONE
}

@Parcelize
@Entity(tableName = "quizzes")
data class Quiz(
    @PrimaryKey val id: String,
    val lessonId: String,
    val title: String,
    val questions: List<QuizQuestion>,
    val passingScore: Int = 70,
    val timeLimit: Int = 0 // 0 means no time limit
) : Parcelable

@Parcelize
data class QuizQuestion(
    val id: String,
    val question: String,
    val questionType: QuestionType,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String? = null,
    val imageUrl: String? = null
) : Parcelable

@Parcelize
enum class QuestionType : Parcelable {
    @SerializedName("multiple_choice")
    MULTIPLE_CHOICE,

    @SerializedName("true_false")
    TRUE_FALSE
}

@Parcelize
@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val lessonId: String,
    val userId: String,
    val completedSteps: List<Int> = emptyList(),
    val quizScore: Int? = null,
    val bookmarked: Boolean = false
) : Parcelable
