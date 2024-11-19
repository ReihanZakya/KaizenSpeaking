package com.example.kaizenspeaking.ui.home_signed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kaizenspeaking.R
import com.example.kaizenspeaking.databinding.FragmentHomeSignedBinding
import com.example.kaizenspeaking.ui.home.Article
import com.example.kaizenspeaking.ui.home.ArticleAdapter
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class HomeSignedFragment : Fragment() {

    private var _binding: FragmentHomeSignedBinding? = null
    private val binding get() = _binding!!

    private lateinit var lineChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val homeSignedViewModel = ViewModelProvider(this)[HomeSignedViewModel::class.java]
        _binding = FragmentHomeSignedBinding.inflate(inflater, container, false)

        // Initialize the LineChart
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

        // Create LineData object and set it on the chart
        val lineData = LineData(dataSetA, dataSetB, dataSetC, dataSetD)
        lineChart.data = lineData

        // Refresh the chart
        lineChart.invalidate()

        // Initialize RecyclerView with ArticleAdapter
        val articles = List(homeSignedViewModel.articleTitles.size) { index ->
            Article(
                title = homeSignedViewModel.articleTitles[index],
                description = homeSignedViewModel.articleDescriptions[index],
                image = homeSignedViewModel.articleImages[index],
                url = homeSignedViewModel.articleUrls[index]
            )
        }

        val adapter = ArticleAdapter(requireContext(), articles)
        binding.ArticlesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.ArticlesRecyclerView.adapter = adapter

        // Set onClickListener for accountName
        binding.accountName.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.navigation_home_signed) // Navigasi ke HomeSignedFragment
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
