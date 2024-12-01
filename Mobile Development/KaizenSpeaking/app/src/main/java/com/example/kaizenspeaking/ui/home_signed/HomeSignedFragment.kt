package com.example.kaizenspeaking.ui.home_signed

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kaizenspeaking.R
import com.example.kaizenspeaking.databinding.FragmentHomeSignedBinding
import com.example.kaizenspeaking.ui.auth.SignInActivity
import com.example.kaizenspeaking.ui.history.HistoryViewModel
import com.example.kaizenspeaking.ui.history.HistoryViewModelFactory
import com.example.kaizenspeaking.ui.history.data.Authenticator.TOKEN
import com.example.kaizenspeaking.ui.history.data.Authenticator.USER_ID
import com.example.kaizenspeaking.ui.history.data.remote.Repository
import com.example.kaizenspeaking.ui.history.data.remote.retrofit.ApiConfig
import com.example.kaizenspeaking.ui.home.Article
import com.example.kaizenspeaking.ui.home.ArticleAdapter
import com.example.kaizenspeaking.utils.UserSession
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlin.getValue

class HomeSignedFragment : Fragment() {
    private val repository: Repository by lazy {
        Repository.getInstance(ApiConfig.getApiService())
    }
    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory(repository)
    }

    private var _binding: FragmentHomeSignedBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeSignedViewModel: HomeSignedViewModel
    private lateinit var lineChart: LineChart
    private val articleAdapter by lazy { ArticleAdapter(requireContext(), listOf()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeSignedViewModel = ViewModelProvider(this)[HomeSignedViewModel::class.java]
        _binding = FragmentHomeSignedBinding.inflate(inflater, container, false)

        lineChart = binding.lineChart
        observeChartData()


        binding.accountButton.setOnClickListener {
            if (!UserSession.isLoggedIn(requireContext())) {
                showLoginDialog()
            } else {
                findNavController().navigate(R.id.action_homeSignedFragment_to_profileFragment) } }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        updateProgressChartVisibility()

        // Observer untuk isLoading
        historyViewModel.isLoading.observe(viewLifecycleOwner, { isLoading ->
            if (isLoading) {
                binding.loadingLayout.visibility = View.VISIBLE
                binding.progressChartLayout.visibility = View.INVISIBLE
            } else {
                binding.loadingLayout.visibility = View.GONE
                binding.progressChartLayout.visibility = View.VISIBLE
            }
        })
        historyViewModel.getAllHistory(TOKEN, USER_ID)
        historyViewModel.numberOfExercise.observe(viewLifecycleOwner, { newText ->
            binding.numberOfExerciseText.text = newText
        })

        binding.switchToSimple.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                historyViewModel.entriesA.observe(viewLifecycleOwner, { entriesA ->
                    historyViewModel.entriesB.observe(viewLifecycleOwner, { entriesB ->
                        historyViewModel.entriesC.observe(viewLifecycleOwner, { entriesC ->
                            historyViewModel.entriesD.observe(viewLifecycleOwner, { entriesD ->
                                val averageEntries = calculateAverageEntries(entriesA, entriesB, entriesC, entriesD)
                                val averageDataSet = LineDataSet(averageEntries, "Nilai Rata-Rata").apply {
                                    color = resources.getColor(android.R.color.holo_purple)
                                }
                                val lineData = LineData(averageDataSet)
                                setupChart(lineData)
                            })
                        })
                    })
                })
            } else {
                observeChartData() // Kembali ke grafik awal
            }
        }

    }

    private fun setupRecyclerView() {
        binding.ArticlesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = articleAdapter
        }
        updateArticles()
    }

    private fun updateArticles() {
        val articles = List(homeSignedViewModel.articleTitles.size) { index ->
            Article(
                title = homeSignedViewModel.articleTitles[index],
                description = homeSignedViewModel.articleDescriptions[index],
                image = homeSignedViewModel.articleImages[index],
                url = homeSignedViewModel.articleUrls[index]
            )
        }
        articleAdapter.updateData(articles)
    }

    private fun observeChartData() {
        historyViewModel.entriesA.observe(viewLifecycleOwner, { entriesA ->
            val dataSetA = LineDataSet(entriesA, "Kejelasan").apply {
                color = resources.getColor(android.R.color.holo_blue_light)
            }
            historyViewModel.entriesB.observe(viewLifecycleOwner, { entriesB ->
                val dataSetB = LineDataSet(entriesB, "Diksi").apply {
                    color = resources.getColor(android.R.color.holo_green_light)
                }
                historyViewModel.entriesC.observe(viewLifecycleOwner, { entriesC ->
                    val dataSetC = LineDataSet(entriesC, "Kelancaran").apply {
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

    private fun calculateAverageEntries(
        entriesA: List<com.github.mikephil.charting.data.Entry>,
        entriesB: List<com.github.mikephil.charting.data.Entry>,
        entriesC: List<com.github.mikephil.charting.data.Entry>,
        entriesD: List<com.github.mikephil.charting.data.Entry>
    ): List<com.github.mikephil.charting.data.Entry> {
        val averageEntries = mutableListOf<com.github.mikephil.charting.data.Entry>()

        for (i in entriesA.indices) {
            val avg = (entriesA[i].y + entriesB[i].y + entriesC[i].y + entriesD[i].y) / 4
            averageEntries.add(com.github.mikephil.charting.data.Entry(entriesA[i].x, avg))
        }
        return averageEntries
    }


    private fun setupChart(lineData: LineData) {
        lineChart.data = lineData

        val xAxis = lineChart.xAxis
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f

        lineChart.axisLeft.axisMinimum = 0f
        lineChart.axisLeft.axisMaximum = 100f
        lineChart.axisRight.isEnabled = false
        lineChart.description.text = "Latihan Ke: "
        lineChart.invalidate()
    }

    private fun showLoginDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_box_login)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val signInButton: Button = dialog.findViewById(R.id.btnSignIn)
        val googleSignInButton: Button = dialog.findViewById(R.id.btnClose)

        signInButton.setOnClickListener {
            startActivity(Intent(requireContext(), SignInActivity::class.java))
            dialog.dismiss()
        }

        googleSignInButton.setOnClickListener {
            // TODO: Implement Google Sign-In logic
            // Use Google Sign-In API to handle authentication
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateProgressChartVisibility() {
        if (UserSession.isLoggedIn(requireContext())) {
            binding.progressChartLayout.visibility = View.VISIBLE
        } else {
            binding.progressChartLayout.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
