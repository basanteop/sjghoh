package com.ar.education.ar

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ar.education.R
import com.ar.education.data.*
import com.ar.education.databinding.ActivityArViewerBinding
import com.ar.education.progress.ProgressRepository
import com.ar.education.quiz.QuizActivity
import com.google.ar.core.*
import com.google.ar.core.exceptions.NotYetAvailableException
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.coroutines.*

/**
 * AR Viewer Activity for displaying 3D models with guided lab steps
 */
class ARViewerActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityArViewerBinding
    private lateinit var arFragment: ArFragment
    private lateinit var viewModel: ARViewerViewModel
    private var currentLesson: Lesson? = null
    private var currentStepIndex = 0
    private var modelRenderable: ModelRenderable? = null
    private var anchorNode: AnchorNode? = null
    private var transformableNode: TransformableNode? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityArViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViews()
        setupViewModel()
        setupAR()
        loadLessonData()
    }
    
    private fun setupViews() {
        arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as ArFragment
        
        binding.apply {
            btnPrevious.setOnClickListener { previousStep() }
            btnNext.setOnClickListener { nextStep() }
            btnTakeQuiz.setOnClickListener { startQuiz() }
            btnHome.setOnClickListener { finish() }
            btnBookmark.setOnClickListener { toggleBookmark() }
        }
    }
    
    private fun setupViewModel() {
        val lessonId = intent.getStringExtra(EXTRA_LESSON_ID) ?: return
        val progressRepository = ProgressRepository(this)
        viewModel = ViewModelProvider(this, ARViewerViewModelFactory(lessonId, progressRepository)) {
            ARViewerViewModel(lessonId, progressRepository)
        }.get(ARViewerViewModel::class.java)
        
        viewModel.currentLesson.observe(this) { lesson ->
            currentLesson = lesson
            updateUIForCurrentStep()
            loadModel()
        }
        
        viewModel.currentStep.observe(this) { stepIndex ->
            currentStepIndex = stepIndex
            updateUIForCurrentStep()
        }
        
        viewModel.progress.observe(this) { progress ->
            binding.btnBookmark.isSelected = progress?.bookmarked ?: false
        }
    }
    
    private fun setupAR() {
        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            if (anchorNode == null) {
                // Create anchor
                val anchor = hitResult.createAnchor()
                anchorNode = AnchorNode(anchor)
                anchorNode!!.setParent(arFragment.arSceneView.scene)
                
                // Load and place model
                loadModel()
            }
        }
    }
    
    private fun loadLessonData() {
        val lessonId = intent.getStringExtra(EXTRA_LESSON_ID) ?: return
        viewModel.loadLesson()
    }
    
    private fun loadModel() {
        val lesson = currentLesson ?: return
        
        // Load 3D model from assets
        ModelRenderable.builder()
            .setSource(this, Uri.parse(lesson.modelPath))
            .build()
            .thenAccept { renderable ->
                modelRenderable = renderable
                if (anchorNode != null) {
                    placeModel()
                }
            }
            .exceptionally { throwable ->
                Toast.makeText(this, "Failed to load model: ${throwable.message}", Toast.LENGTH_SHORT).show()
                null
            }
    }
    
    private fun placeModel() {
        val renderable = modelRenderable ?: return
        val node = TransformableNode(arFragment.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        node.localPosition = Vector3(0f, 0f, 0f)
        node.select()
        transformableNode = node
        
        // Apply model highlighting if needed
        val currentStep = currentLesson?.labSteps?.get(currentStepIndex)
        currentStep?.modelHighlighting?.let { highlighting ->
            applyModelHighlighting(highlighting)
        }
    }
    
    private fun applyModelHighlighting(highlighting: ModelHighlighting) {
        // In a real implementation, this would highlight specific parts of the 3D model
        // For now, we'll just show a toast message
        val step = currentLesson?.labSteps?.get(currentStepIndex)
        Toast.makeText(this, step?.title ?: "", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateUIForCurrentStep() {
        val lesson = currentLesson ?: return
        
        binding.apply {
            tvLessonTitle.text = lesson.title
            tvStepInfo.text = "Step ${currentStepIndex + 1} of ${lesson.labSteps.size}"
            
            if (currentStepIndex < lesson.labSteps.size) {
                val step = lesson.labSteps[currentStepIndex]
                tvStepTitle.text = step.title
                tvStepInstruction.text = step.instruction
                tvExpectedOutcome.text = step.expectedOutcome ?: ""
            }
            
            // Update button states
            btnPrevious.isEnabled = currentStepIndex > 0
            btnNext.isEnabled = currentStepIndex < lesson.labSteps.size - 1
            
            // Show/hide take quiz button
            btnTakeQuiz.visibility = if (currentStepIndex == lesson.labSteps.size - 1) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
    
    private fun previousStep() {
        if (currentStepIndex > 0) {
            viewModel.previousStep()
        }
    }
    
    private fun nextStep() {
        val lesson = currentLesson ?: return
        if (currentStepIndex < lesson.labSteps.size - 1) {
            // Mark current step as completed
            viewModel.markStepCompleted(currentStepIndex + 1)
            viewModel.nextStep()
        }
    }
    
    private fun startQuiz() {
        val lesson = currentLesson ?: return
        val intent = Intent(this, QuizActivity::class.java)
        intent.putExtra(QuizActivity.EXTRA_LESSON_ID, lesson.id)
        intent.putExtra(QuizActivity.EXTRA_QUIZ_DATA, lesson.quiz)
        startActivity(intent)
    }
    
    private fun toggleBookmark() {
        viewModel.toggleBookmark()
    }
    
    override fun onResume() {
        super.onResume()
        arFragment.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        arFragment.onPause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        arFragment.onDestroy()
    }
    
    companion object {
        const val EXTRA_LESSON_ID = "extra_lesson_id"
    }
}