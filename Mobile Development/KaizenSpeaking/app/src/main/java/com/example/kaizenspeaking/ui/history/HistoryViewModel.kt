package com.example.kaizenspeaking.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kaizenspeaking.ui.history.data.TrainingSession

class HistoryViewModel : ViewModel() {

    private val _trainingSessions = MutableLiveData<List<TrainingSession>>()
    val trainingSessions: LiveData<List<TrainingSession>> = _trainingSessions

    private val _totalTrainings = MutableLiveData<Int>()
    val totalTrainings: LiveData<Int> = _totalTrainings

    init {
        // In a real app, this would load from a repository
        loadTrainingSessions()
    }

    private fun loadTrainingSessions() {
        // Dummy data for demonstration
        val sessions = listOf(
            TrainingSession("1", "Pidato Senin 1", "30 Oct 2024 10:25:14"),
            TrainingSession("2", "Pidato Senin 2", "1 Nov 2024 15:30:22"),
            TrainingSession("3", "Pidato Senin 3", "2 Nov 2024 00:25:14")
        )
        _trainingSessions.value = sessions
        _totalTrainings.value = sessions.size
    }
}