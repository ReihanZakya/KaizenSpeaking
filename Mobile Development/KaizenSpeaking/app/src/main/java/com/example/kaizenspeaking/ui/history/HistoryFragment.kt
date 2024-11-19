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
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

@Suppress("DEPRECATION")
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels()
    private val trainingAdapter = TrainingSessionAdapter()
    private lateinit var barChart: BarChart


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

        barChart = binding.barchart
        barChart.getAxisRight().setDrawLabels(false)

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