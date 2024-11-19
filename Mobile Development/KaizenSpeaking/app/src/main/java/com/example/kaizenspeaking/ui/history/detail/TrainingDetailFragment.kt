package com.example.kaizenspeaking.ui.history.detail

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.kaizenspeaking.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.ekn.gruzer.gaugelibrary.Range
import com.ekn.gruzer.gaugelibrary.HalfGauge

class TrainingDetailFragment : Fragment() {

    private lateinit var barChart: BarChart
    private lateinit var halfGauge: HalfGauge
    private lateinit var scrollView: ScrollView
    private lateinit var cardViewAnalisis: View
    private lateinit var titleTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_training_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barChart = view.findViewById(R.id.barchart)
        halfGauge = view.findViewById(R.id.gauge_chart)
        scrollView = view.findViewById(R.id.scrollView)
        cardViewAnalisis = view.findViewById(R.id.cardViewAnalasis)
        titleTextView = view.findViewById(R.id.titleTextView)

        // Konfigurasi BarChart
        barChart.axisRight.setDrawLabels(false)

        // Data for the chart
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, 25f)) // Data point 1
        entries.add(BarEntry(1f, 50f)) // Data point 2
        entries.add(BarEntry(2f, 100f)) // Data point 3
        entries.add(BarEntry(3f, 75f)) // Data point 4

        // Create BarDataSet
        val barDataSet = BarDataSet(entries, "Bar Chart Example")

        // Set different colors for each bar
        val colors = arrayListOf(
            resources.getColor(android.R.color.holo_blue_light),
            resources.getColor(android.R.color.holo_green_light),
            resources.getColor(android.R.color.holo_orange_light),
            resources.getColor(android.R.color.holo_red_light)
        )
        barDataSet.colors = colors

        // Create BarData
        val barData = BarData(barDataSet)

        // Set data to the chart
        barChart.data = barData

        // Refresh chart to render the new data
        barChart.invalidate()

        // Konfigurasi HalfGauge
        val range1 = Range().apply {
            color = Color.parseColor("#ce0000")
            from = 0.0
            to = 33.3
        }

        val range2 = Range().apply {
            color = Color.parseColor("#E3E500")
            from = 33.3
            to = 66.6
        }

        val range3 = Range().apply {
            color = Color.parseColor("#00b20b")
            from = 66.6
            to = 100.0
        }

        // Add color ranges to gauge
        halfGauge.addRange(range1)
        halfGauge.addRange(range2)
        halfGauge.addRange(range3)

        // Set min, max, and current value
        halfGauge.minValue = 0.0
        halfGauge.maxValue = 100.0
        halfGauge.value = 75.0  // example value

        // Mengambil arguments
        arguments?.let { args ->
            val sessionId = args.getString("sessionId")
            val sessionTitle = args.getString("sessionTitle")

            // Update UI
            titleTextView.text = sessionTitle
        }

        // Auto-scroll to "Hasil Analisis" section after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            scrollView.smoothScrollTo(0, cardViewAnalisis.top)
        }, 3000)
    }
}
