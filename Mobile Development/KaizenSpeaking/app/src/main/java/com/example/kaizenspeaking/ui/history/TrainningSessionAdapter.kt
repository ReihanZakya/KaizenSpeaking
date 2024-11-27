package com.example.kaizenspeaking.ui.history

import android.icu.text.SimpleDateFormat
import android.media.MediaPlayer
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kaizenspeaking.databinding.ItemTrainingSessionBinding
import com.example.kaizenspeaking.ui.history.data.TrainingSession
import java.util.Locale
import java.util.concurrent.TimeUnit

class TrainingSessionAdapter : RecyclerView.Adapter<TrainingSessionAdapter.TrainingViewHolder>() {

    private var sessions = listOf<TrainingSession>()
    private val handler = Handler()
    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingPosition: Int = -1
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
        holder.bind(sessions[position], position)
    }

    override fun getItemCount() = sessions.size

    inner class TrainingViewHolder(
        private val binding: ItemTrainingSessionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(session: TrainingSession, position: Int) {
            binding.apply {
                titleTextView.text = session.title

                val originalFormat =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
                val desiredFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                try {
                    val date = originalFormat.parse(session.date)
                    val formattedDate = desiredFormat.format(date)
                    dateTextView.text = formattedDate
                } catch (e: Exception) {
                    e.printStackTrace()
                    dateTextView.text = session.date
                }

                playButton.setOnClickListener {
                    playAudio(session.audioUrl, position)
                }
                pauseButton.setOnClickListener {
                    pauseAudio()
                }
                updateSeekBar()
            }
        }

        private fun playAudio(audioUrl: String, position: Int) {
            if (currentPlayingPosition == position && mediaPlayer?.isPlaying == true) {
                pauseAudio()
                return
            }

            // Stop current playing audio if any
            mediaPlayer?.release()
            mediaPlayer = null

            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                prepare()
                start()
                setOnCompletionListener {
                    resetUI()
                }
            }
            currentPlayingPosition = position
            updateUIForPlaying()
        }

        private fun pauseAudio() {
            mediaPlayer?.pause()
            updateUIForPaused()
        }

        private fun updateSeekBar() {
            mediaPlayer?.let { player ->
                val runnable = object : Runnable {
                    override fun run() {
                        binding.seekBar.progress = player.currentPosition
                        binding.durationTextView.text =
                            formatDuration(player.currentPosition.toLong())
                        handler.postDelayed(this, 1000)
                    }
                }
                handler.postDelayed(runnable, 0)
            }
        }

        private fun resetUI() {
            binding.apply {
                playButton.visibility = View.VISIBLE
                pauseButton.visibility = View.GONE
                seekBar.progress = 0
                durationTextView.text = formatDuration(0)
            }
        }

        private fun updateUIForPlaying() {
            binding.apply {
                playButton.visibility = View.GONE
                pauseButton.visibility = View.VISIBLE
                seekBar.max = mediaPlayer?.duration ?: 0
                updateSeekBar()
            }
        }

        private fun updateUIForPaused() {
            binding.apply {
                playButton.visibility = View.VISIBLE
                pauseButton.visibility = View.GONE
            }
        }

        private fun formatDuration(durationMillis: Long): String {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
    }
}
