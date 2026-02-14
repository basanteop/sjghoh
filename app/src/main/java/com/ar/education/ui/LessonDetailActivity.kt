package com.ar.education.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ar.education.R
import com.ar.education.ar.ARViewerActivity
import com.ar.education.data.Lesson
import com.ar.education.databinding.ActivityLessonDetailBinding
import com.ar.education.progress.ProgressRepository

/**
 * Activity for displaying lesson details
 */
class LessonDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLessonDetailBinding
    private lateinit var viewModel: LessonDetailViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityLessonDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewModel()
        setupViews()
        loadLesson()
    }
    
    private fun setupViewModel() {
        val lessonId = intent.getStringExtra(EXTRA_LESSON_ID) ?: return
        val progressRepository = ProgressRepository(this)
        viewModel = ViewModelProvider(this, LessonDetailViewModelFactory(lessonId, progressRepository)) {
            LessonDetailViewModel(lessonId, progressRepository)
        }.get(LessonDetailViewModel::class.java)
        
        viewModel.lesson.observe(this) { lesson ->
            lesson?.let { displayLesson(it) }
        }
        
        viewModel.progress.observe(this) { progress ->
            // Display progress information
            binding.tvProgress.text = if (progress != null) {
                "Progress: ${progress.completedSteps.size} steps completed"
            } else {
                "Not started"
            }
            
            if (progress?.isCompleted == true) {
                binding.btnStartLesson.text = "Continue"
            }
        }
    }
    
    private fun setupViews() {
        binding.apply {
            btnBack.setOnClickListener { finish() }
            
            btnStartLesson.setOnClickListener {
                viewModel.lesson.value?.let { lesson ->
                    val intent = Intent(this@LessonDetailActivity, ARViewerActivity::class.java)
                    intent.putExtra(ARViewerActivity.EXTRA_LESSON_ID, lesson.id)
                    startActivity(intent)
                }
            }
            
            btnTakeQuiz.setOnClickListener {
                viewModel.lesson.value?.let { lesson ->
                    val intent = Intent(this@LessonDetailActivity, QuizActivity::class.java)
                    intent.putExtra(QuizActivity.EXTRA_LESSON_ID, lesson.id)
                    intent.putExtra(QuizActivity.EXTRA_QUIZ_DATA, lesson.quiz)
                    startActivity(intent)
                }
            }
        }
    }
    
    private fun loadLesson() {
        viewModel.loadLesson()
    }
    
    private fun displayLesson(lesson: Lesson) {
        binding.apply {
            tvLessonTitle.text = lesson.title
            tvSubject.text = lesson.subject.displayName
            tvDifficulty.text = lesson.difficulty.displayName
            tvDuration.text = "${lesson.estimatedDuration} minutes"
            tvDescription.text = lesson.description
            
            tvLabSteps.text = lesson.labSteps.size.toString()
            tvQuizQuestions.text = lesson.quiz.questions.size.toString()
            
            // Display tags
            if (lesson.tags.isNotEmpty()) {
                tvTags.text = lesson.tags.joinToString(", ")
            }
        }
    }
    
    companion object {
        const val EXTRA_LESSON_ID = "extra_lesson_id"
    }
}