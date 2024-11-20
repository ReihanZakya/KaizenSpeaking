package com.example.kaizenspeaking.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kaizenspeaking.R
import com.example.kaizenspeaking.databinding.FragmentHistoryBinding
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
    private lateinit var barChart: BarChart
    private lateinit var lineChart: LineChart


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        lineChart = binding.lineChart

        // Data for each Exercise
        val exerciseLabels = arrayOf("Latihan 1", "Latihan 2", "Latihan 3")
        val dataA = arrayOf(65f, 70f, 80f, 85f, 92f, 94f)
        val dataB = arrayOf(50f, 52f, 60f, 75f, 85f, 95f)
        val dataC = arrayOf(70f, 72f, 75f, 79f, 82f, 85f)
        val dataD = arrayOf(40f, 65f, 75f, 80f, 83f, 90f)

        // Create entries for each line
        val entriesA = ArrayList<Entry>()
        val entriesB = ArrayList<Entry>()
        val entriesC = ArrayList<Entry>()
        val entriesD = ArrayList<Entry>()

        // Populate entries for each data series
        for (i in exerciseLabels.indices) {
            entriesA.add(Entry(i.toFloat(), dataA[i]))
            entriesB.add(Entry(i.toFloat(), dataB[i]))
            entriesC.add(Entry(i.toFloat(), dataC[i]))
            entriesD.add(Entry(i.toFloat(), dataD[i]))
        }

        // Create datasets for each category (A, B, C, D)
        val dataSetA = LineDataSet(entriesA, "Kejelasan")
        val dataSetB = LineDataSet(entriesB, "Diksi")
        val dataSetC = LineDataSet(entriesC, "Kelancaran")
        val dataSetD = LineDataSet(entriesD, "Emosi")

        // Set colors for each line
        dataSetA.color = resources.getColor(android.R.color.holo_blue_light)
        dataSetB.color = resources.getColor(android.R.color.holo_green_light)
        dataSetC.color = resources.getColor(android.R.color.holo_red_light)
        dataSetD.color = resources.getColor(android.R.color.holo_orange_light)

        val lineData = LineData(dataSetA, dataSetB, dataSetC, dataSetD)
        lineChart.data = lineData

        // Refresh the chart
        lineChart.invalidate()

    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trainingAdapter
        }
    }

    private fun setupObservers() {
        viewModel.trainingSessions.observe(viewLifecycleOwner) { sessions ->
            trainingAdapter.submitList(sessions)
        }

        // Untuk sementara, kita bisa menampilkan total di TextView yang ada di layout
        viewModel.totalTrainings.observe(viewLifecycleOwner) { total ->
            // Cari TextView yang sesuai di layout Anda
            binding.root.findViewById<TextView>(R.id.totalTrainingTextView)?.text =
                "Total Latihan: $total"
        }
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