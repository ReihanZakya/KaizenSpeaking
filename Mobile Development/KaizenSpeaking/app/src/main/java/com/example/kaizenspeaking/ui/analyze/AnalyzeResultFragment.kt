package com.example.kaizenspeaking.ui.analyze

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import com.ekn.gruzer.gaugelibrary.HalfGauge
import com.ekn.gruzer.gaugelibrary.Range
import com.example.kaizenspeaking.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class AnalyzeResultFragment : Fragment() {

    private lateinit var barChart: BarChart
    private lateinit var halfGauge: HalfGauge
    private lateinit var scrollView: ScrollView
    private lateinit var cardViewAnalysis: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_analyze_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barChart = view.findViewById(R.id.barchart)
        halfGauge = view.findViewById(R.id.gauge_chart)
        scrollView = view.findViewById(R.id.scrollView)
        cardViewAnalysis = view.findViewById(R.id.cardViewAnalysis)

        barChart.axisRight.setDrawLabels(false)
        barChart.axisLeft.axisMinimum = 0f // Set minimum value for Y axis
        barChart.axisLeft.axisMaximum = 100f // Set maximum value for Y axis

        // Data for the chart (representing 4 categories: Kejelasan, Diksi, Kelancaran, Emosi)
        val entriesKejelasan = ArrayList<BarEntry>()
        val entriesDiksi = ArrayList<BarEntry>()
        val entriesKelancaran = ArrayList<BarEntry>()
        val entriesEmosi = ArrayList<BarEntry>()

        // Example data for each category
        entriesKejelasan.add(BarEntry(0f, 94f))
        entriesDiksi.add(BarEntry(1f, 95f))
        entriesKelancaran.add(BarEntry(2f, 85f))
        entriesEmosi.add(BarEntry(3f, 90f))

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
        halfGauge.value = 75.0  // example value

        // Auto-scroll to "Hasil Analisis" section after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            scrollView.smoothScrollTo(0, cardViewAnalysis.top)
        }, 3000)
    }
}
