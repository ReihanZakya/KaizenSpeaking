package com.example.kaizenspeaking.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kaizenspeaking.R
import com.example.kaizenspeaking.databinding.FragmentHistoryBinding
import com.example.kaizenspeaking.ui.history.data.TrainingSession
import com.example.kaizenspeaking.ui.history.data.remote.Repository
import com.example.kaizenspeaking.ui.history.data.remote.retrofit.ApiConfig
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.example.kaizenspeaking.ui.history.data.Authenticator.TOKEN
import com.example.kaizenspeaking.ui.history.data.Authenticator.USER_ID

@Suppress("DEPRECATION")
class HistoryFragment : Fragment() {
    private val repository: Repository by lazy {
        Repository.getInstance(ApiConfig.getApiService())
    }
    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory(repository)
    }

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val trainingAdapter = TrainingSessionAdapter()
    private lateinit var lineChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        lineChart = binding.lineChart

        // Observe LiveData for chart entries
        observeChartData()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        // Observer untuk isLoading
        historyViewModel.isLoading.observe(viewLifecycleOwner, { isLoading ->
            if (isLoading) {
                binding.loadingLayout.visibility = View.VISIBLE
                binding.contentLayout.visibility = View.GONE
            } else {
                binding.loadingLayout.visibility = View.GONE
                binding.contentLayout.visibility = View.VISIBLE
            }
        })
        historyViewModel.getAllHistory(TOKEN, USER_ID)
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trainingAdapter
        }

        historyViewModel.numberOfExercise.observe(viewLifecycleOwner, { newText ->
            binding.numberOfExerciseText.text = newText
        })

        historyViewModel.trainingSessions.observe(viewLifecycleOwner) { sessions ->
            trainingAdapter.submitList(sessions)
        }
    }

    private fun observeChartData() {
        historyViewModel.entriesA.observe(viewLifecycleOwner, { entriesA ->
            val dataSetA = LineDataSet(entriesA, "Kelancaran").apply {
                color = resources.getColor(android.R.color.holo_blue_light)
            }

            historyViewModel.entriesB.observe(viewLifecycleOwner, { entriesB ->
                val dataSetB = LineDataSet(entriesB, "Kejelasan").apply {
                    color = resources.getColor(android.R.color.holo_green_light)
                }

                historyViewModel.entriesC.observe(viewLifecycleOwner, { entriesC ->
                    val dataSetC = LineDataSet(entriesC, "Diksi").apply {
                        color = resources.getColor(android.R.color.holo_red_light)
                    }

                    historyViewModel.entriesD.observe(viewLifecycleOwner, { entriesD ->
                        val dataSetD = LineDataSet(entriesD, "Emosi").apply {
                            color = resources.getColor(android.R.color.holo_orange_light)
                        }

                        val lineData = LineData(dataSetA, dataSetB, dataSetC, dataSetD)
                        setupChart(lineData)
                    })
                })
            })
        })
    }

    private fun setupChart(lineData: LineData) {
        lineChart.data = lineData

        val xAxis = lineChart.xAxis
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f

        lineChart.axisLeft.axisMinimum = 0f
        lineChart.axisRight.isEnabled = false
        lineChart.description.text = "Latihan Ke: "
        lineChart.invalidate()
    }

    private fun setupClickListeners() {
        trainingAdapter.setOnItemClickListener { session ->
            val bundle = Bundle().apply {
                putString("sessionId", session.id)
                putString("sessionTitle", session.title)
            }
            findNavController().navigate(
                R.id.trainingDetailFragment,
                bundle
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
