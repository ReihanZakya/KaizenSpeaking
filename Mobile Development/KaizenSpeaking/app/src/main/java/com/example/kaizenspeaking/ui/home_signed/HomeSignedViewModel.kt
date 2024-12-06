package com.example.kaizenspeaking.ui.home_signed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.kaizenspeaking.R
import com.github.mikephil.charting.data.Entry

class HomeSignedViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext

    // Data untuk chart
    private val _entriesA = MutableLiveData<List<Entry>>()
    val entriesA: LiveData<List<Entry>> get() = _entriesA

    private val _entriesB = MutableLiveData<List<Entry>>()
    val entriesB: LiveData<List<Entry>> get() = _entriesB

    private val _entriesC = MutableLiveData<List<Entry>>()
    val entriesC: LiveData<List<Entry>> get() = _entriesC

    private val _entriesD = MutableLiveData<List<Entry>>()
    val entriesD: LiveData<List<Entry>> get() = _entriesD

    // Method to initialize data
    fun initChartData() {
        val dataA = arrayOf(65f, 70f, 80f, 85f, 92f, 94f)
        val dataB = arrayOf(50f, 52f, 60f, 75f, 85f, 95f)
        val dataC = arrayOf(70f, 72f, 75f, 79f, 82f, 85f)
        val dataD = arrayOf(40f, 65f, 75f, 80f, 83f, 90f)

        val entriesA = ArrayList<Entry>()
        val entriesB = ArrayList<Entry>()
        val entriesC = ArrayList<Entry>()
        val entriesD = ArrayList<Entry>()

        for (i in dataA.indices) {
            entriesA.add(Entry((i + 1).toFloat(), dataA[i]))
            entriesB.add(Entry((i + 1).toFloat(), dataB[i]))
            entriesC.add(Entry((i + 1).toFloat(), dataC[i]))
            entriesD.add(Entry((i + 1).toFloat(), dataD[i]))
        }

        _entriesA.value = entriesA
        _entriesB.value = entriesB
        _entriesC.value = entriesC
        _entriesD.value = entriesD

        // Update the number of exercises based on the length of dataA
        _numberOfExercise.value = "Banyak Latihan: ${dataA.size}"
    }

    // LiveData untuk mengubah jumlah latihan
    private val _numberOfExercise = MutableLiveData<String>()
    val numberOfExercise: LiveData<String> get() = _numberOfExercise

    // Initialize number of exercises based on the number of articles
    init {
        // Inisialisasi nilai jumlah latihan
        _numberOfExercise.value = "Banyak Latihan: 0"  // Nilai default
    }

    val articleTitles: Array<String> =
        context.resources.getStringArray(R.array.tempdata_article_title)
    val articleDescriptions: Array<String> =
        context.resources.getStringArray(R.array.tempdata_article_description)
    val articleUrls: Array<String> = context.resources.getStringArray(R.array.tempdata_article_url)
    val articleImages: IntArray =
        context.resources.obtainTypedArray(R.array.tempdata_article_image).let {
            IntArray(it.length()) { index -> it.getResourceId(index, -1) }
        }
}
