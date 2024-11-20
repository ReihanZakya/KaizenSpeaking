package com.example.kaizenspeaking.ui.home_signed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kaizenspeaking.databinding.FragmentHomeSignedBinding
import com.example.kaizenspeaking.ui.home.Article
import com.example.kaizenspeaking.ui.home.ArticleAdapter
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class HomeSignedFragment : Fragment() {

    private var _binding: FragmentHomeSignedBinding? = null
    private val binding get() = _binding!!

    private lateinit var lineChart: LineChart
    private lateinit var homeSignedViewModel: HomeSignedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeSignedViewModel = ViewModelProvider(this)[HomeSignedViewModel::class.java]
        _binding = FragmentHomeSignedBinding.inflate(inflater, container, false)

        // Initialize the LineChart
        lineChart = binding.lineChart

        // Initialize chart data in ViewModel
        homeSignedViewModel.initChartData()

        // Observe LiveData for chart entries
        homeSignedViewModel.entriesA.observe(viewLifecycleOwner, { entriesA ->
            val dataSetA = LineDataSet(entriesA, "Kejelasan")
            dataSetA.color = resources.getColor(android.R.color.holo_blue_light)

            // Similarly set up other datasets (B, C, D)
            homeSignedViewModel.entriesB.observe(viewLifecycleOwner, { entriesB ->
                val dataSetB = LineDataSet(entriesB, "Diksi")
                dataSetB.color = resources.getColor(android.R.color.holo_green_light)

                homeSignedViewModel.entriesC.observe(viewLifecycleOwner, { entriesC ->
                    val dataSetC = LineDataSet(entriesC, "Kelancaran")
                    dataSetC.color = resources.getColor(android.R.color.holo_red_light)

                    homeSignedViewModel.entriesD.observe(viewLifecycleOwner, { entriesD ->
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

        // Initialize RecyclerView with ArticleAdapter
        val articles = List(homeSignedViewModel.articleTitles.size) { index ->
            Article(
                title = homeSignedViewModel.articleTitles[index],
                description = homeSignedViewModel.articleDescriptions[index],
                image = homeSignedViewModel.articleImages[index],
                url = homeSignedViewModel.articleUrls[index]
            )
        }

        // Mengamati perubahan pada LiveData numberOfExercise
        homeSignedViewModel.numberOfExercise.observe(viewLifecycleOwner, { newText ->
            // Update TextView dengan nilai baru
            binding.numberOfExerciseText.text = newText
        })

        val adapter = ArticleAdapter(requireContext(), articles)
        binding.ArticlesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.ArticlesRecyclerView.adapter = adapter

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
