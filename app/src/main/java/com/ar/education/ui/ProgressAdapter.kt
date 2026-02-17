package com.ar.education.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ar.education.data.Lesson
import com.ar.education.databinding.ItemLessonBinding

class ProgressAdapter(private val onLessonClicked: (Lesson) -> Unit) :
    ListAdapter<Lesson, ProgressAdapter.ProgressViewHolder>(LessonDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressViewHolder {
        val binding = ItemLessonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProgressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) {
        val lesson = getItem(position)
        holder.bind(lesson)
        holder.itemView.setOnClickListener { onLessonClicked(lesson) }
    }

    class ProgressViewHolder(private val binding: ItemLessonBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(lesson: Lesson) {
            binding.tvLessonTitle.text = lesson.title
            // You might want to show progress-specific data here
        }
    }
}

class LessonDiffCallback : DiffUtil.ItemCallback<Lesson>() {
    override fun areItemsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
        return oldItem == newItem
    }
}
