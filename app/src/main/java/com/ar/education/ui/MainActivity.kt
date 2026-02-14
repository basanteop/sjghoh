package com.ar.education.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ar.education.R
import com.ar.education.ar.ARViewerActivity
import com.ar.education.data.Lesson
import com.ar.education.data.Subject
import com.ar.education.databinding.ActivityMainBinding
import com.ar.education.progress.ProgressRepository
import com.google.ar.core.ArCoreApk

/**
 * Main Activity - Entry point of the AR Education app
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var lessonsAdapter: LessonsAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViews()
        setupViewModel()
        checkARCoreAvailability()
        loadLessons()
    }
    
    private fun setupViews() {
        binding.apply {
            // Setup navigation buttons
            btnPhysics.setOnClickListener { filterBySubject(Subject.PHYSICS) }
            btnBiology.setOnClickListener { filterBySubject(Subject.BIOLOGY) }
            btnChemistry.setOnClickListener { filterBySubject(Subject.CHEMISTRY) }
            btnAllSubjects.setOnClickListener { filterBySubject(null) }
            
            btnProgress.setOnClickListener {
                startActivity(Intent(this@MainActivity, ProgressActivity::class.java))
            }
            
            // Setup RecyclerView for lessons
            lessonsAdapter = LessonsAdapter { lesson ->
                startARLesson(lesson)
            }
            
            recyclerViewLessons.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = lessonsAdapter
            }
        }
    }
    
    private fun setupViewModel() {
        val progressRepository = ProgressRepository(this)
        viewModel = ViewModelProvider(this, MainViewModelFactory(progressRepository)) {
            MainViewModel(progressRepository)
        }.get(MainViewModel::class.java)
        
        viewModel.lessons.observe(this) { lessons ->
            lessonsAdapter.submitList(lessons)
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            // Show/hide loading indicator
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }
        
        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }
    
    private fun checkARCoreAvailability() {
        when (ArCoreApk.getInstance().requestInstall(this, true)) {
            ArCoreApk.InstallStatus.INSTALLED -> {
                // ARCore is installed
                binding.tvArStatus.text = "AR Ready"
                binding.tvArStatus.setTextColor(getColor(android.R.color.holo_green_dark))
            }
            ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                // ARCore installation is required
                binding.tvArStatus.text = "AR Install Required"
                binding.tvArStatus.setTextColor(getColor(android.R.color.holo_orange_dark))
            }
            else -> {
                // ARCore not supported
                binding.tvArStatus.text = "AR Not Supported"
                binding.tvArStatus.setTextColor(getColor(android.R.color.holo_red_dark))
            }
        }
    }
    
    private fun loadLessons() {
        viewModel.loadAllLessons()
    }
    
    private fun filterBySubject(subject: Subject?) {
        viewModel.filterBySubject(subject)
    }
    
    private fun startARLesson(lesson: Lesson) {
        if (isARCoreSupported()) {
            val intent = Intent(this, ARViewerActivity::class.java)
            intent.putExtra(ARViewerActivity.EXTRA_LESSON_ID, lesson.id)
            startActivity(intent)
        } else {
            Toast.makeText(this, "AR not supported on this device", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun isARCoreSupported(): Boolean {
        return try {
            packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_AR)
        } catch (e: Exception) {
            false
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh progress and lessons when returning to main activity
        viewModel.refreshData()
    }
}

/**
 * Adapter for displaying lessons in RecyclerView
 */
class LessonsAdapter(
    private val onLessonClick: (Lesson) -> Unit
) : RecyclerView.Adapter<LessonsAdapter.LessonViewHolder>() {
    
    private var lessons = mutableListOf<Lesson>()
    
    fun submitList(newLessons: List<Lesson>) {
        lessons.clear()
        lessons.addAll(newLessons)
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): LessonViewHolder {
        val binding = com.ar.education.databinding.ItemLessonBinding.inflate(
            android.view.LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LessonViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        holder.bind(lessons[position], onLessonClick)
    }
    
    override fun getItemCount() = lessons.size
    
    class LessonViewHolder(
        private val binding: com.ar.education.databinding.ItemLessonBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(lesson: Lesson, onClick: (Lesson) -> Unit) {
            binding.apply {
                tvLessonTitle.text = lesson.title
                tvSubject.text = lesson.subject.displayName
                tvDifficulty.text = lesson.difficulty.displayName
                tvDuration.text = "${lesson.estimatedDuration} min"
                tvDescription.text = lesson.description
                
                // Set subject color
                val colorRes = when (lesson.subject) {
                    Subject.PHYSICS -> android.R.color.holo_blue_dark
                    Subject.BIOLOGY -> android.R.color.holo_green_dark
                    Subject.CHEMISTRY -> android.R.color.holo_orange_dark
                }
                tvSubject.setTextColor(itemView.context.getColor(colorRes))
                
                root.setOnClickListener { onClick(lesson) }
            }
        }
    }
}