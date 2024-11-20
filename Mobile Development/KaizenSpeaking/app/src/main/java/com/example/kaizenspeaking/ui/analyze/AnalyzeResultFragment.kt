package com.example.kaizenspeaking.ui.analyze

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kaizenspeaking.R
import com.example.kaizenspeaking.databinding.FragmentAnalyzeBinding
import com.example.kaizenspeaking.databinding.FragmentAnalyzeResultBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate


@Suppress("DEPRECATION")
class AnalyzeResultFragment : Fragment() {

    private var _binding: FragmentAnalyzeResultBinding? = null

    private val binding get() = _binding!!

    private lateinit var barChart: BarChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyzeResultBinding.inflate(inflater,container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize BarChart
        barChart = binding.barchart

        // Data for the chart
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, 4f)) // Data point 1
        entries.add(BarEntry(1f, 2f)) // Data point 2
        entries.add(BarEntry(2f, 6f)) // Data point 3
        entries.add(BarEntry(3f, 3f)) // Data point 4

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}