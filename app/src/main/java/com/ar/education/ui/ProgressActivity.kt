package com.ar.education.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ar.education.R
import com.ar.education.ar.ARViewerActivity
import com.ar.education.data.Subject
import com.ar.education.databinding.ActivityProgressBinding
import com.ar.education.progress.ProgressRepository
import com.ar.education.progress.ProgressViewModel

/**
 * Activity for displaying user progress
 */
class ProgressActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityProgressBinding
    private lateinit var viewModel: ProgressViewModel
    private lateinit var adapter: ProgressAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewModel()
        setupViews()
        loadProgress()
    }
    
    private fun setupViewModel() {
        val progressRepository = ProgressRepository(this)
        viewModel = ViewModelProvider(this, ProgressViewModelFactory(progressRepository)) {
            ProgressViewModel(progressRepository)
        }.get(ProgressViewModel::class.java)
        
        viewModel.progressList.observe(this) { progressList ->
            adapter.submitList(progressList)
        }
        
        viewModel.completedCount.observe(this) { count ->
            binding.tvCompletedLessons.text = "Completed: $count lessons"
        }
        
        viewModel.averageScore.observe(this) { score ->
            binding.tvAverageScore.text = if (score != null) {
                "Average Score: ${score.toInt()}%"
            } else {
                "Average Score: N/A"
            }
        }
        
        viewModel.bookmarkedCount.observe(this) { count ->
            binding.tvBookmarked.text = "Bookmarked: $count"
        }
    }
    
    private fun setupViews() {
        adapter = ProgressAdapter { lessonId ->
            val intent = Intent(this, ARViewerActivity::class.java)
            intent.putExtra(ARViewerActivity.EXTRA_LESSON_ID, lessonId)
            startActivity(intent)
        }
        
        binding.recyclerViewProgress.apply {
            layoutManager = LinearLayoutManager(this@ProgressActivity)
            adapter = this@ProgressActivity.adapter
        }
        
        binding.btnBack.setOnClickListener { finish() }
    }
    
    private fun loadProgress() {
        viewModel.loadProgress()
    }
}

/**
 * Adapter for displaying progress items
 */
class ProgressAdapter(
    private val onLessonClick: (String) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<ProgressAdapter.ProgressViewHolder>() {
    
    private var progressItems = mutableListOf<ProgressItem>()
    
    data class ProgressItem(
        val lessonId: String,
        val lessonTitle: String,
        val subject: Subject,
        val completedSteps: Int,
        val totalSteps: Int,
        val quizScore: Int,
        val isCompleted: Boolean,
        val lastAccessed: Long
    )
    
    fun submitList(items: List<ProgressItem>) {
        progressItems.clear()
        progressItems.addAll(items)
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ProgressViewHolder {
        val binding = com.ar.education.databinding.ItemProgressBinding.inflate(
            android.view.LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProgressViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) {
        holder.bind(progressItems[position], onLessonClick)
    }
    
    override fun getItemCount() = progressItems.size
    
    class ProgressViewHolder(
        private val binding: com.ar.education.databinding.ItemProgressBinding
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: ProgressItem, onClick: (String) -> Unit) {
            binding.apply {
                tvLessonTitle.text = item.lessonTitle
                tvSubject.text = item.subject.displayName
                tvProgress.text = "${item.completedSteps}/${item.totalSteps} steps"
                tvQuizScore.text = if (item.quizScore > 0) "Quiz: ${item.quizScore}%" else "Quiz: Not taken"
                tvStatus.text = if (item.isCompleted) "Completed" else "In Progress"
                
                root.setOnClickListener { onClick(item.lessonId) }
            }
        }
    }
}