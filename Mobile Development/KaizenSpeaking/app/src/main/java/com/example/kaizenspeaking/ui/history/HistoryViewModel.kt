package com.example.kaizenspeaking.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaizenspeaking.ui.history.data.TrainingSession
import com.example.kaizenspeaking.ui.history.data.remote.Repository
import com.example.kaizenspeaking.ui.history.data.remote.response.DataItem
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.launch
import com.example.kaizenspeaking.ui.history.data.Result

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
            }
            _isLoading.value = false
        }
    }

    private fun processChartData(dataItems: List<DataItem>) {
        val entriesA = ArrayList<Entry>()
        val entriesB = ArrayList<Entry>()
        val entriesC = ArrayList<Entry>()
        val entriesD = ArrayList<Entry>()

        dataItems.forEachIndexed { index, dataItem ->
            val position = (index + 1).toFloat()
            entriesA.add(Entry(position, dataItem.score.kelancaran.toFloatOrNull() ?: 0f))
            entriesB.add(Entry(position, dataItem.score.kejelasan.toFloatOrNull() ?: 0f))
            entriesC.add(Entry(position, dataItem.score.diksi.toFloatOrNull() ?: 0f))
            entriesD.add(Entry(position, dataItem.score.emosi.toFloatOrNull() ?: 0f))
        }

        _entriesA.value = entriesA
        _entriesB.value = entriesB
        _entriesC.value = entriesC
        _entriesD.value = entriesD

        _numberOfExercise.value = "Banyak Latihan: ${dataItems.size}"
    }
}
