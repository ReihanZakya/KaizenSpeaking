package com.example.kaizenspeaking.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kaizenspeaking.databinding.ItemTrainingSessionBinding
import com.example.kaizenspeaking.ui.history.data.TrainingSession

class TrainingSessionAdapter : RecyclerView.Adapter<TrainingSessionAdapter.TrainingViewHolder>() {

    private var sessions = listOf<TrainingSession>()
    private var onItemClickListener: ((TrainingSession) -> Unit)? = null

    fun setOnItemClickListener(listener: (TrainingSession) -> Unit) {
        onItemClickListener = listener
    }

    fun submitList(newSessions: List<TrainingSession>) {
        sessions = newSessions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainingViewHolder {
        val binding = ItemTrainingSessionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TrainingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrainingViewHolder, position: Int) {
        holder.bind(sessions[position])
    }

    override fun getItemCount() = sessions.size

    inner class TrainingViewHolder(
        private val binding: ItemTrainingSessionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(sessions[position])
                }
            }
        }

        fun bind(session: TrainingSession) {
            binding.apply {
                titleTextView.text = session.title
                dateTextView.text = session.date
            }
        }
    }
}