package com.example.kaizenspeaking.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kaizenspeaking.R
import com.example.kaizenspeaking.databinding.FragmentHistoryBinding
import com.example.kaizenspeaking.ui.history.data.TrainingSession
import com.example.kaizenspeaking.ui.home_signed.HomeSignedViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Suppress("DEPRECATION")
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels()
    private val trainingAdapter = TrainingSessionAdapter()
    private lateinit var lineChart: LineChart
    private lateinit var historyViewModel: HistoryViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        historyViewModel = ViewModelProvider(this)[HistoryViewModel::class.java]
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        lineChart = binding.lineChart

        // Initialize chart data in ViewModel
        historyViewModel.initChartData()

        // Observe LiveData for chart entries
        historyViewModel.entriesA.observe(viewLifecycleOwner, { entriesA ->
            val dataSetA = LineDataSet(entriesA, "Kejelasan")
            dataSetA.color = resources.getColor(android.R.color.holo_blue_light)

            // Similarly set up other datasets (B, C, D)
            historyViewModel.entriesB.observe(viewLifecycleOwner, { entriesB ->
                val dataSetB = LineDataSet(entriesB, "Diksi")
                dataSetB.color = resources.getColor(android.R.color.holo_green_light)

                historyViewModel.entriesC.observe(viewLifecycleOwner, { entriesC ->
                    val dataSetC = LineDataSet(entriesC, "Kelancaran")
                    dataSetC.color = resources.getColor(android.R.color.holo_red_light)

                    historyViewModel.entriesD.observe(viewLifecycleOwner, { entriesD ->
                        val dataSetD = LineDataSet(entriesD, "Emosi")
                        dataSetD.color = resources.getColor(android.R.color.holo_orange_light)

                        // Create LineData object and set it on the chart
                        val lineData = LineData(dataSetA, dataSetB, dataSetC, dataSetD)
                        lineChart.data = lineData

                        // Configure X Axis
                        val xAxis = lineChart.xAxis
                        xAxis.position =
                            com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                        xAxis.granularity = 1f
                        xAxis.labelCount = entriesA.size
                        xAxis.valueFormatter =
                            object : com.github.mikephil.charting.formatter.ValueFormatter() {
                                override fun getFormattedValue(value: Float): String {
                                    return "${value.toInt()}"
                                }
                            }

                        // Set Y Axis
                        val leftYAxis = lineChart.axisLeft
                        val rightYAxis = lineChart.axisRight
                        leftYAxis.axisMinimum = 0f
                        leftYAxis.valueFormatter =
                            object : com.github.mikephil.charting.formatter.ValueFormatter() {
                                override fun getFormattedValue(value: Float): String {
                                    return value.toInt().toString()
                                }
                            }
                        rightYAxis.isEnabled = false

                        lineChart.description.text = "Latihan Ke: "
                        lineChart.setExtraOffsets(0f, 20f, 0f, 0f)

                        // Configure Legend
                        val legend = lineChart.legend
                        legend.textSize = 10f
                        legend.form =
                            com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE
                        legend.formSize = 8f

                        lineChart.invalidate() // Refresh chart
                    })
                })
            })
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()


    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trainingAdapter
        }

        // Example data - replace with your actual data source
        val sampleSessions = listOf(
            TrainingSession("1", "First Training", "2024-03-21"),
            TrainingSession("2", "Second Training", "2024-03-22"),
            TrainingSession("3", "Third Training", "2024-03-23")
        )

        trainingAdapter.submitList(sampleSessions)
    }



    private fun setupClickListeners() {
        trainingAdapter.setOnItemClickListener { session ->
            // Navigasi sederhana menggunakan ID fragment tujuan
            val bundle = Bundle().apply {
                putString("sessionId", session.id)
                putString("sessionTitle", session.title)
            }
            findNavController().navigate(
                R.id.trainingDetailFragment,  // Pastikan ID ini sesuai dengan yang ada di nav graph
                bundle
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}