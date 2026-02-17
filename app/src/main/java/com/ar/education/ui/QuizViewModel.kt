package com.ar.education.quiz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ar.education.data.Quiz
import com.ar.education.progress.ProgressRepository
import kotlinx.coroutines.launch

class QuizViewModel(application: Application, private val lessonId: String, private val quizData: Quiz) : AndroidViewModel(application) {

    private val progressRepository = ProgressRepository(application)

    private val _quiz = MutableLiveData<Quiz>()
    val quiz: LiveData<Quiz> = _quiz

    private val _score = MutableLiveData<Int>()
    val score: LiveData<Int> = _score

    private val _isQuizFinished = MutableLiveData<Boolean>()
    val isQuizFinished: LiveData<Boolean> = _isQuizFinished

    init {
        _quiz.value = quizData
    }

    fun submitQuiz(userAnswers: Map<Int, Int>) {
        var correctAnswers = 0
        for ((questionIndex, selectedOption) in userAnswers) {
            if (quizData.questions[questionIndex].correctAnswerIndex == selectedOption) {
                correctAnswers++
            }
        }
        _score.value = correctAnswers
        viewModelScope.launch {
            progressRepository.markQuizAsCompleted(lessonId, correctAnswers, quizData.questions.size)
            _isQuizFinished.value = true
        }
    }
}

class QuizViewModelFactory(private val application: Application, private val lessonId: String, private val quiz: Quiz) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuizViewModel(application, lessonId, quiz) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
