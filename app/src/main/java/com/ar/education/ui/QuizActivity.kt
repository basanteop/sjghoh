package com.ar.education.quiz

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ar.education.R
import com.ar.education.data.Quiz
import com.ar.education.data.QuizQuestion
import com.ar.education.databinding.ActivityQuizBinding
import com.ar.education.progress.ProgressRepository
import com.ar.education.ui.MainActivity

/**
 * Activity for taking quizzes
 */
class QuizActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityQuizBinding
    private lateinit var viewModel: QuizViewModel
    private var currentQuestionIndex = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewModel()
        setupViews()
        loadQuiz()
    }
    
    private fun setupViewModel() {
        val lessonId = intent.getStringExtra(EXTRA_LESSON_ID) ?: return
        val progressRepository = ProgressRepository(this)
        
        viewModel = ViewModelProvider(this, QuizViewModelFactory(lessonId, progressRepository)) {
            QuizViewModel(lessonId, progressRepository)
        }.get(QuizViewModel::class.java)
        
        viewModel.currentQuestion.observe(this) { question ->
            question?.let { displayQuestion(it) }
        }
        
        viewModel.quizProgress.observe(this) { progress ->
            binding.tvProgress.text = progress
        }
        
        viewModel.isQuizComplete.observe(this) { isComplete ->
            if (isComplete) {
                showResults()
            }
        }
        
        viewModel.quizResult.observe(this) { result ->
            result?.let {
                binding.tvScore.text = "Score: ${it.score}%(${it.correctAnswers}/${it.totalQuestions})"
                binding.tvResultMessage.text = if (it.passed) {
                    "Congratulations! You passed!"
                } else {
                    "Keep practicing! You need 70% to pass."
                }
            }
        }
    }
    
    private fun setupViews() {
        binding.apply {
            btnNext.setOnClickListener { nextQuestion() }
            btnSubmit.setOnClickListener { submitQuiz() }
            btnRetry.setOnClickListener { retryQuiz() }
            btnFinish.setOnClickListener { finishQuiz() }
            btnBack.setOnClickListener { finish() }
        }
    }
    
    private fun loadQuiz() {
        val quiz = intent.getParcelableExtra<Quiz>(EXTRA_QUIZ_DATA)
        quiz?.let {
            viewModel.loadQuiz(it)
        }
    }
    
    private fun displayQuestion(question: QuizQuestion) {
        binding.apply {
            tvQuestionNumber.text = "Question ${currentQuestionIndex + 1}"
            tvQuestion.text = question.question
            
            // Clear previous options
            radioGroupOptions.removeAllViews()
            
            // Add options as radio buttons
            question.options.forEachIndexed { index, option ->
                val radioButton = RadioButton(this@QuizActivity)
                radioButton.id = View.generateViewId()
                radioButton.text = option
                radioButton.tag = option
                radioButton.textSize = 16f
                radioButton.setPadding(16, 24, 16, 24)
                radioGroupOptions.addView(radioButton)
            }
            
            // Show/hide explanation
            question.explanation?.let {
                tvExplanation.visibility = View.VISIBLE
                tvExplanation.text = it
            } ?: run {
                tvExplanation.visibility = View.GONE
            }
            
            // Update progress
            viewModel.updateProgress(currentQuestionIndex + 1)
        }
    }
    
    private fun nextQuestion() {
        val selectedOption = getSelectedOption()
        if (selectedOption == null) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show()
            return
        }
        
        viewModel.submitAnswer(selectedOption)
        currentQuestionIndex++
        
        if (currentQuestionIndex < (viewModel.quiz.value?.questions?.size ?: 0)) {
            viewModel.nextQuestion()
        } else {
            submitQuiz()
        }
    }
    
    private fun getSelectedOption(): String? {
        val selectedId = binding.radioGroupOptions.checkedRadioButtonId
        if (selectedId != -1) {
            val radioButton = binding.radioGroupOptions.findViewById<RadioButton>(selectedId)
            return radioButton?.tag as? String
        }
        return null
    }
    
    private fun submitQuiz() {
        viewModel.submitQuiz()
    }
    
    private fun showResults() {
        binding.apply {
            quizContent.visibility = View.GONE
            resultContent.visibility = View.VISIBLE
        }
    }
    
    private fun retryQuiz() {
        currentQuestionIndex = 0
        viewModel.resetQuiz()
        binding.apply {
            quizContent.visibility = View.VISIBLE
            resultContent.visibility = View.GONE
        }
    }
    
    private fun finishQuiz() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        })
    }
    
    companion object {
        const val EXTRA_LESSON_ID = "extra_lesson_id"
        const val EXTRA_QUIZ_DATA = "extra_quiz_data"
    }
}