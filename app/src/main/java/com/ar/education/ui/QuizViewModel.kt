package com.ar.education.ui

import androidx.lifecycle.*
import com.ar.education.data.Quiz
import com.ar.education.data.QuizQuestion
import com.ar.education.progress.ProgressRepository
import kotlinx.coroutines.launch

/**
 * Data class for quiz result
 */
data class QuizResult(
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val passed: Boolean
)

/**
 * ViewModel for Quiz Activity
 */
class QuizViewModel(
    private val lessonId: String,
    private val progressRepository: ProgressRepository
) : AndroidViewModel() {
    
    private val _quiz = MutableLiveData<Quiz?>()
    val quiz: LiveData<Quiz?> = _quiz
    
    private val _currentQuestion = MutableLiveData<QuizQuestion?>()
    val currentQuestion: LiveData<QuizQuestion?> = _currentQuestion
    
    private val _quizProgress = MutableLiveData<String>()
    val quizProgress: LiveData<String> = _quizProgress
    
    private val _isQuizComplete = MutableLiveData(false)
    val isQuizComplete: LiveData<Boolean> = _isQuizComplete
    
    private val _quizResult = MutableLiveData<QuizResult?>()
    val quizResult: LiveData<QuizResult?> = _quizResult
    
    private var questionIndex = 0
    private var correctAnswers = 0
    private val userAnswers = mutableListOf<String>()
    
    fun loadQuiz(quiz: Quiz) {
        _quiz.value = quiz
        questionIndex = 0
        correctAnswers = 0
        userAnswers.clear()
        _isQuizComplete.value = false
        
        if (quiz.questions.isNotEmpty()) {
            _currentQuestion.value = quiz.questions[0]
        }
    }
    
    fun nextQuestion() {
        _quiz.value?.let { quiz ->
            if (questionIndex < quiz.questions.size - 1) {
                questionIndex++
                _currentQuestion.value = quiz.questions[questionIndex]
            }
        }
    }
    
    fun submitAnswer(answer: String) {
        _quiz.value?.let { quiz ->
            if (questionIndex < quiz.questions.size) {
                userAnswers.add(answer)
                
                // Check if answer is correct
                val currentQuestion = quiz.questions[questionIndex]
                if (answer == currentQuestion.correctAnswer) {
                    correctAnswers++
                }
            }
        }
    }
    
    fun submitQuiz() {
        _quiz.value?.let { quiz ->
            val totalQuestions = quiz.questions.size
            val score = if (totalQuestions > 0) {
                (correctAnswers * 100) / totalQuestions
            } else 0
            
            val result = QuizResult(
                score = score,
                totalQuestions = totalQuestions,
                correctAnswers = correctAnswers,
                passed = score >= quiz.passingScore
            )
            
            _quizResult.value = result
            _isQuizComplete.value = true
            
            // Save result to progress
            saveQuizResult(score)
        }
    }
    
    private fun saveQuizResult(score: Int) {
        viewModelScope.launch {
            progressRepository.saveQuizResult(lessonId, score, getCurrentUserId())
        }
    }
    
    fun resetQuiz() {
        loadQuiz(_quiz.value ?: return)
    }
    
    fun updateProgress(currentQuestionNum: Int) {
        _quiz.value?.let { quiz ->
            _quizProgress.value = "Question $currentQuestionNum of ${quiz.questions.size}"
        }
    }
    
    private fun getCurrentUserId(): String {
        // In a real app, this would get from authentication
        return "default_user"
    }
}

/**
 * Factory for QuizViewModel
 */
class QuizViewModelFactory(
    private val lessonId: String,
    private val progressRepository: ProgressRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuizViewModel(lessonId, progressRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}