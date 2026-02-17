package com.ar.education.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing lesson data and progress
 */
class LessonRepository(private val context: Context) {
    private val gson = Gson()
    private val lessonsCache = mutableMapOf<String, Lesson>()
    private val progressCache = mutableMapOf<String, LessonProgress>()

    /**
     * Load all lessons from assets
     */
    suspend fun getAllLessons(): Result<List<Lesson>> = withContext(Dispatchers.IO) {
        try {
            if (lessonsCache.isEmpty()) {
                loadLessonsFromAssets()
            }
            Result.success(lessonsCache.values.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get lesson by ID
     */
    suspend fun getLesson(lessonId: String): Result<Lesson?> = withContext(Dispatchers.IO) {
        try {
            if (lessonsCache.isEmpty()) {
                loadLessonsFromAssets()
            }
            Result.success(lessonsCache[lessonId])
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get lessons by subject
     */
    suspend fun getLessonsBySubject(subject: Subject): Result<List<Lesson>> = withContext(Dispatchers.IO) {
        try {
            val allLessons = getAllLessons().getOrThrow()
            Result.success(allLessons.filter { it.subject == subject })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Save user progress
     */
    suspend fun saveProgress(progress: LessonProgress): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            progressCache[progress.lessonId] = progress
            // In a real app, this would save to Room database or DataStore
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get user progress for a lesson
     */
    suspend fun getProgress(lessonId: String): Result<LessonProgress?> = withContext(Dispatchers.IO) {
        try {
            Result.success(progressCache[lessonId])
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all progress for user
     */
    suspend fun getAllProgress(userId: String): Result<List<LessonProgress>> = withContext(Dispatchers.IO) {
        try {
            val userProgress = progressCache.values.filter { it.userId == userId }
            Result.success(userProgress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if ARCore is supported on this device
     */
    fun isARCoreSupported(): Boolean {
        return try {
            Class.forName("com.google.ar.core.AugmentedImage")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    /**
     * Check if device has sufficient hardware for AR
     */
    fun hasSufficientHardware(): Boolean {
        // Basic hardware check - can be extended
        val availableProcessors = Runtime.getRuntime().availableProcessors()
        val maxMemory = Runtime.getRuntime().maxMemory()
        
        return availableProcessors >= 2 && maxMemory > 32 * 1024 * 1024 // 32MB minimum
    }

    private suspend fun loadLessonsFromAssets() {
        try {
            // Load sample lessons - in a real app, these would be loaded from JSON files
            val sampleLessons = createSampleLessons()
            sampleLessons.forEach { lesson ->
                lessonsCache[lesson.id] = lesson
            }
        } catch (e: Exception) {
            throw Exception("Failed to load lessons from assets: ${e.message}")
        }
    }

    private fun createSampleLessons(): List<Lesson> {
        return listOf(
            // Physics Lesson
            Lesson(
                id = "physics_001",
                subject = Subject.PHYSICS,
                title = "Newton's Laws of Motion",
                description = "Learn about the fundamental laws governing motion through interactive 3D models.",
                difficulty = Difficulty.BEGINNER,
                estimatedDuration = 30,
                markerId = "marker_physics_001",
                modelPath = "models/newtons_laws.glb",
                labSteps = listOf(
                    LabStep(
                        stepNumber = 1,
                        title = "First Law - Inertia",
                        instruction = "Observe how an object at rest stays at rest unless acted upon by a force.",
                        modelHighlighting = ModelHighlighting("box", "#FF0000"),
                        requiresInteraction = true,
                        interactionType = InteractionType.TAP
                    ),
                    LabStep(
                        stepNumber = 2,
                        title = "Second Law - F=ma",
                        instruction = "See how force equals mass times acceleration in this interactive demo.",
                        modelHighlighting = ModelHighlighting("force_arrow", "#00FF00"),
                        requiresInteraction = true,
                        interactionType = InteractionType.DRAG
                    ),
                    LabStep(
                        stepNumber = 3,
                        title = "Third Law - Action-Reaction",
                        instruction = "For every action, there is an equal and opposite reaction.",
                        modelHighlighting = ModelHighlighting("rocket", "#0000FF"),
                        requiresInteraction = false
                    )
                ),
                quiz = Quiz(
                    id = "quiz_physics_001",
                    title = "Newton's Laws Quiz",
                    questions = listOf(
                        QuizQuestion(
                            id = "q1",
                            question = "What does Newton's First Law describe?",
                            questionType = QuestionType.MULTIPLE_CHOICE,
                            options = listOf("Gravity", "Inertia", "Momentum", "Energy"),
                            correctAnswer = "Inertia",
                            explanation = "The First Law states that objects at rest stay at rest and objects in motion stay in motion unless acted upon by an external force."
                        ),
                        QuizQuestion(
                            id = "q2",
                            question = "F = ma represents which of Newton's Laws?",
                            questionType = QuestionType.MULTIPLE_CHOICE,
                            options = listOf("First Law", "Second Law", "Third Law", "Fourth Law"),
                            correctAnswer = "Second Law",
                            explanation = "F = ma is the mathematical representation of Newton's Second Law of Motion."
                        )
                    ),
                    passingScore = 70
                ),
                tags = listOf("mechanics", "fundamentals", "forces")
            ),
            
            // Biology Lesson
            Lesson(
                id = "biology_001",
                subject = Subject.BIOLOGY,
                title = "Cell Structure and Function",
                description = "Explore the components of a cell and their functions in 3D.",
                difficulty = Difficulty.INTERMEDIATE,
                estimatedDuration = 45,
                markerId = "marker_biology_001",
                modelPath = "models/cell_structure.glb",
                labSteps = listOf(
                    LabStep(
                        stepNumber = 1,
                        title = "Cell Membrane",
                        instruction = "The cell membrane controls what enters and exits the cell.",
                        modelHighlighting = ModelHighlighting("cell_membrane", "#FFD700"),
                        requiresInteraction = true,
                        interactionType = InteractionType.ROTATE
                    ),
                    LabStep(
                        stepNumber = 2,
                        title = "Nucleus",
                        instruction = "The nucleus contains the cell's DNA and controls cell activities.",
                        modelHighlighting = ModelHighlighting("nucleus", "#FF6B6B"),
                        requiresInteraction = true,
                        interactionType = InteractionType.TAP
                    ),
                    LabStep(
                        stepNumber = 3,
                        title = "Mitochondria",
                        instruction = "Mitochondria are the powerhouses of the cell, producing energy.",
                        modelHighlighting = ModelHighlighting("mitochondria", "#4ECDC4"),
                        requiresInteraction = false
                    )
                ),
                quiz = Quiz(
                    id = "quiz_biology_001",
                    title = "Cell Structure Quiz",
                    questions = listOf(
                        QuizQuestion(
                            id = "q1",
                            question = "What is the primary function of the nucleus?",
                            questionType = QuestionType.MULTIPLE_CHOICE,
                            options = listOf("Energy production", "Protein synthesis", "DNA storage", "Waste removal"),
                            correctAnswer = "DNA storage",
                            explanation = "The nucleus contains the cell's DNA and controls gene expression."
                        )
                    ),
                    passingScore = 70
                ),
                tags = listOf("cells", "biology", "organelles")
            ),
            
            // Chemistry Lesson
            Lesson(
                id = "chemistry_001",
                subject = Subject.CHEMISTRY,
                title = "Atomic Structure",
                description = "Understand the structure of atoms with interactive 3D models.",
                difficulty = Difficulty.BEGINNER,
                estimatedDuration = 25,
                markerId = "marker_chemistry_001",
                modelPath = "models/atom_structure.glb",
                labSteps = listOf(
                    LabStep(
                        stepNumber = 1,
                        title = "Protons and Neutrons",
                        instruction = "These particles make up the nucleus at the center of the atom.",
                        modelHighlighting = ModelHighlighting("nucleus", "#FF1744"),
                        requiresInteraction = true,
                        interactionType = InteractionType.SCALE
                    ),
                    LabStep(
                        stepNumber = 2,
                        title = "Electrons",
                        instruction = "Electrons orbit around the nucleus in specific energy levels.",
                        modelHighlighting = ModelHighlighting("electron_orbit", "#2196F3"),
                        requiresInteraction = true,
                        interactionType = InteractionType.ROTATE
                    )
                ),
                quiz = Quiz(
                    id = "quiz_chemistry_001",
                    title = "Atomic Structure Quiz",
                    questions = listOf(
                        QuizQuestion(
                            id = "q1",
                            question = "Where are electrons located in an atom?",
                            questionType = QuestionType.MULTIPLE_CHOICE,
                            options = listOf("In the nucleus", "Orbiting the nucleus", "Between atoms", "In electrons"),
                            correctAnswer = "Orbiting the nucleus",
                            explanation = "Electrons orbit around the nucleus in specific energy levels called electron shells."
                        )
                    ),
                    passingScore = 70
                ),
                tags = listOf("atoms", "chemistry", "particles")
            )
        )
    }
}