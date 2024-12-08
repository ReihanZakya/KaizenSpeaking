package com.example.kaizenspeaking.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaizenspeaking.ui.history.data.Result
import com.example.kaizenspeaking.ui.history.data.TrainingSession
import com.example.kaizenspeaking.ui.history.data.remote.Repository
import com.example.kaizenspeaking.ui.history.data.remote.response.DataItem
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: Repository) : ViewModel() {

    // Data untuk chart
    private val _entriesA = MutableLiveData<List<Entry>>()
    val entriesA: LiveData<List<Entry>> get() = _entriesA

    private val _entriesB = MutableLiveData<List<Entry>>()
    val entriesB: LiveData<List<Entry>> get() = _entriesB

    private val _entriesC = MutableLiveData<List<Entry>>()
    val entriesC: LiveData<List<Entry>> get() = _entriesC

    private val _entriesD = MutableLiveData<List<Entry>>()
    val entriesD: LiveData<List<Entry>> get() = _entriesD

    private val _history = MutableLiveData<Result<List<DataItem>>>()
    val history: LiveData<Result<List<DataItem>>> = _history

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData untuk mengubah jumlah latihan
    private val _numberOfExercise = MutableLiveData<String>()
    val numberOfExercise: LiveData<String> get() = _numberOfExercise

    private val _trainingSessions = MutableLiveData<List<TrainingSession>>()
    val trainingSessions: LiveData<List<TrainingSession>> get() = _trainingSessions

    init {
        _numberOfExercise.value = "Banyak Latihan: 0"  // Nilai default
    }

    fun getAllHistory(token: String, userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getAllHistory(token, userId)
            _history.value = result
            if (result is Result.Success) {
                processChartData(result.data)
                val sessions = result.data.map { dataItem ->
                    TrainingSession(
                        id = dataItem.id,
                        title = dataItem.topic,
                        date = dataItem.createdAt,
                        audioUrl = dataItem.audioFileUrl,
                        duration = dataItem.duration,
                        kejelasan = dataItem.score.kejelasan,
                        diksi = dataItem.score.diksi,
                        kelancaran = dataItem.score.kelancaran,
                        emosi = dataItem.score.emosi,
                        analize = dataItem.analize
                    )
                }
                _trainingSessions.value = sessions
            }
            _isLoading.value = false
        }
    }

    private fun processChartData(dataItems: List<DataItem>) {
        // Urutkan data berdasarkan tanggal secara ascending
        val sortedDataItems = dataItems.sortedBy { it.createdAt }

        val entriesA = ArrayList<Entry>()
        val entriesB = ArrayList<Entry>()
        val entriesC = ArrayList<Entry>()
        val entriesD = ArrayList<Entry>()

        sortedDataItems.forEachIndexed { index, sortedDataItem ->
            val position = (index + 1).toFloat()

            fun safeScore(score: String?): Float {
                return score?.toFloatOrNull() ?: 0f
            }

            entriesA.add(Entry(position, safeScore(sortedDataItem.score.kejelasan)))
            entriesB.add(Entry(position, safeScore(sortedDataItem.score.diksi)))
            entriesC.add(Entry(position, safeScore(sortedDataItem.score.kelancaran)))
            entriesD.add(Entry(position, safeScore(sortedDataItem.score.emosi)))
        }


        _entriesA.value = entriesA
        _entriesB.value = entriesB
        _entriesC.value = entriesC
        _entriesD.value = entriesD

        _numberOfExercise.value = "Banyak Latihan: ${dataItems.size}"
    }
}
