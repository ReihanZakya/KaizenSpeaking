package com.example.kaizenspeaking.ui.analyze

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import com.ekn.gruzer.gaugelibrary.HalfGauge
import com.ekn.gruzer.gaugelibrary.Range
import com.example.kaizenspeaking.databinding.FragmentAnalyzeResultBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kaizenspeaking.ui.analyze.data.response.AnalyzeResponse
import com.example.kaizenspeaking.ui.analyze.data.response.Score


@Suppress("DEPRECATION")
class AnalyzeResultFragment : Fragment() {

    private var _binding: FragmentAnalyzeResultBinding? = null
    private val binding get() = _binding!!

    private lateinit var barChart: BarChart
    private lateinit var halfGauge: HalfGauge
    private lateinit var scrollView: ScrollView
    private lateinit var cardViewAnalysis: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyzeResultBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.hide()

        // Ambil data dari arguments
        val analyzeResponse = arguments?.getParcelable<AnalyzeResponse>("result")
        Log.d("AnalyzeResultFragment", "Received AnalyzeResponse: $analyzeResponse")
        if (analyzeResponse != null) {
            setupBarChart(analyzeResponse.score)
            setupGaugeChart(analyzeResponse.score)
            setupAnalyzeWords(analyzeResponse.words)
            Log.d("AnalyzeResultFragment", "Hasil Analisis: ${analyzeResponse.score}")
        } else {
            Toast.makeText(requireContext(), "Data tidak ditemukan", Toast.LENGTH_SHORT).show()
            Log.e("AnalyzeResultFragment", "Data tidak ditemukan")
        }

        // Auto-scroll to "Hasil Analisis" section after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            scrollView.smoothScrollTo(0, cardViewAnalysis.top)
        }, 3000)
    }

    private fun setupBarChart(score: Score?) {
        if (score == null) return

        barChart = binding.barchart
        halfGauge = binding.gaugeChart
        scrollView = binding.scrollView
        cardViewAnalysis = binding.cardViewAnalysis

        barChart.axisRight.setDrawLabels(false)
        barChart.axisLeft.axisMinimum = 0f // Set minimum value for Y axis
        barChart.axisLeft.axisMaximum = 100f // Set maximum value for Y axis

        // Data for the chart (representing 4 categories: Kejelasan, Diksi, Kelancaran, Emosi)
        val entriesKejelasan = ArrayList<BarEntry>()
        val entriesDiksi = ArrayList<BarEntry>()
        val entriesKelancaran = ArrayList<BarEntry>()
        val entriesEmosi = ArrayList<BarEntry>()

        // Example data for each category
        entriesKejelasan.add(BarEntry(0f, score.kejelasan?.toFloatOrNull() ?: 0f))
        entriesDiksi.add(BarEntry(1f, score.diksi?.toFloatOrNull() ?: 0f))
        entriesKelancaran.add(BarEntry(2f, score.kelancaran?.toFloatOrNull() ?: 0f))
        entriesEmosi.add(BarEntry(3f, score.emosi?.toFloatOrNull() ?: 0f))

        // Create BarDataSets for each category
        val barDataSetKejelasan = BarDataSet(entriesKejelasan, "Kejelasan")
        val barDataSetDiksi = BarDataSet(entriesDiksi, "Diksi")
        val barDataSetKelancaran = BarDataSet(entriesKelancaran, "Kelancaran")
        val barDataSetEmosi = BarDataSet(entriesEmosi, "Emosi")

        // Set colors for each category
        barDataSetKejelasan.color = resources.getColor(android.R.color.holo_blue_light)
        barDataSetDiksi.color = resources.getColor(android.R.color.holo_green_light)
        barDataSetKelancaran.color = resources.getColor(android.R.color.holo_red_light)
        barDataSetEmosi.color = resources.getColor(android.R.color.holo_orange_light)

        // Create BarData
        val barData = BarData(barDataSetKejelasan, barDataSetDiksi, barDataSetKelancaran, barDataSetEmosi)

        // Set data to the chart
        barChart.data = barData

        // Disable X axis labels
        barChart.xAxis.setDrawLabels(false)

        barChart.description.text = " "

        // Refresh chart to render the new data
        barChart.invalidate()

    }

    private fun setupGaugeChart(score: Score?) {
        if (score == null) return

        // Hitung rata-rata dari skor
        val scores = listOfNotNull(
            score.kejelasan?.toFloatOrNull(),
            score.diksi?.toFloatOrNull(),
            score.kelancaran?.toFloatOrNull(),
            score.emosi?.toFloatOrNull()
        )
        val averageScore = if (scores.isNotEmpty()) scores.average().toFloat() else 0f

        // Konfigurasi HalfGauge
        val range1 = Range().apply {
            color = resources.getColor(android.R.color.holo_red_light)
            from = 0.0
            to = 33.3
        }

        val range2 = Range().apply {
            color = resources.getColor(android.R.color.holo_orange_light)
            from = 33.3
            to = 66.6
        }

        val range3 = Range().apply {
            color = resources.getColor(android.R.color.holo_green_light)
            from = 66.6
            to = 100.0
        }

        // Add color ranges to gauge
        halfGauge.addRange(range1)
        halfGauge.addRange(range2)
        halfGauge.addRange(range3)

        // Set min, max, and current value for the HalfGauge
        halfGauge.minValue = 0.0
        halfGauge.maxValue = 100.0
        halfGauge.value = averageScore.toDouble()  // example value


    }

    private fun setupAnalyzeWords(words: List<String>) {
        // Gabungkan kata-kata menjadi satu string dengan pemisah koma
        val wordsText = words.joinToString(", ")

        // Tampilkan di TextView
        binding.tvAnalyzeResult.text = wordsText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        (activity as AppCompatActivity).supportActionBar?.show()
    }
}
