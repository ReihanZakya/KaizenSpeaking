package com.example.kaizenspeaking.ui.history

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
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

                // Set click listener for title
                titleTextView.setOnClickListener {
                    onItemClickListener?.invoke(session)
                }

                val originalFormat =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
                val desiredFormat = SimpleDateFormat(
                    "dd MMM yyyy HH:mm 'WIB'",
                    Locale.getDefault()
                ) // Tambahkan literal WIB

                try {
                    val date = originalFormat.parse(session.date) // Parsing string date
                    if (date != null) {
                        // Gunakan Calendar untuk menyesuaikan jam
                        val calendar = Calendar.getInstance().apply {
                            time = date
                            val adjustedHour =
                                (get(Calendar.HOUR_OF_DAY) + 7) % 24 // Penyesuaian jam
                            set(Calendar.HOUR_OF_DAY, adjustedHour) // Set jam yang sudah diubah
                        }

                        // Format tanggal dengan desiredFormat
                        val formattedDate = desiredFormat.format(calendar.time)
                        dateTextView.text = formattedDate
                    } else {
                        // Jika parsing gagal, tampilkan string original
                        dateTextView.text = session.date
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    dateTextView.text = session.date // Jika terjadi error, tampilkan original date
                }


                val dur = session.duration

                val formattedDuration = try {
                    // Periksa apakah ada ":" dalam string
                    val parts = dur.split(":")
                    val totalSeconds = when (parts.size) {
                        2 -> {
                            val minutes = parts[0].toIntOrNull() ?: 0
                            val seconds = parts[1].toIntOrNull() ?: 0
                            minutes * 60 + seconds
                        }

                        1 -> {
                            parts[0].toIntOrNull() ?: 0
                        }

                        else -> 0
                    }

                    // Konversi total detik ke format mm:ss
                    val minutes = totalSeconds / 60
                    val seconds = totalSeconds % 60
                    String.format("%02d:%02d", minutes, seconds)
                } catch (e: Exception) {
                    "00:00" // Jika terjadi kesalahan, gunakan default
                }

                durationTextView.text = formattedDuration



                playButton.setOnClickListener {
                    playAudio(session.audioUrl, position, formattedDuration)
                }
                stopButton.setOnClickListener {
                    stopAudio(formattedDuration)
                }
                updateSeekBar()
            }
        }

        private fun playAudio(audioUrl: String, position: Int, dur: String) {
            if (currentPlayingPosition == position && mediaPlayer?.isPlaying == true) {
                stopAudio(dur) // Stop if already playing
                return
            }

            // Stop current playing audio if any
            mediaPlayer?.release() // Release any existing player
            mediaPlayer = null

            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                setOnPreparedListener {
                    start() // Start playing after preparation is complete
                    updateUIForPlaying()
                }
                setOnCompletionListener {
                    resetUI(dur) // Reset UI once the audio finishes
                }
                prepareAsync() // Prepare asynchronously to avoid blocking the UI thread
            }
            currentPlayingPosition = position
        }

        private fun stopAudio(dur: String) {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
            resetUI(dur)
        }

        private fun updateSeekBar() {
            mediaPlayer?.let { player ->
                // Check if media player is initialized and playing
                if (player.isPlaying) {
                    val runnable = object : Runnable {
                        override fun run() {
                            try {
                                binding.seekBar.progress = player.currentPosition
                                binding.durationTextView.text =
                                    formatDuration(player.currentPosition.toLong())
                                handler.postDelayed(this, 1000)
                            } catch (e: IllegalStateException) {
                                // Handle potential IllegalStateException if MediaPlayer is in invalid state
                                e.printStackTrace()
                            }
                        }
                    }
                    handler.postDelayed(runnable, 0)
                }
            }
        }

        private fun resetUI(dur: String) {
            binding.apply {
                playButton.visibility = View.VISIBLE
                stopButton.visibility = View.GONE
                seekBar.progress = 0
                durationTextView.text = dur
            }
        }

        private fun updateUIForPlaying() {
            binding.apply {
                playButton.visibility = View.GONE
                stopButton.visibility = View.VISIBLE
                seekBar.max = mediaPlayer?.duration ?: 0
                updateSeekBar()
            }
        }

        private fun formatDuration(durationMillis: Long): String {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
    }
}
