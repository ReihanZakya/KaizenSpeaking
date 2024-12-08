package com.example.kaizenspeaking.ui.history

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
import android.widget.ImageView
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kaizenspeaking.MainActivity
import com.example.kaizenspeaking.R
import com.example.kaizenspeaking.databinding.FragmentHistoryBinding
import com.example.kaizenspeaking.ui.auth.SignInActivity
import com.example.kaizenspeaking.ui.history.data.Authenticator
import com.example.kaizenspeaking.ui.history.data.remote.Repository
import com.example.kaizenspeaking.ui.history.data.remote.retrofit.ApiConfig
import com.example.kaizenspeaking.utils.UserSession
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet


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
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        lineChart = binding.lineChart

        // Observe LiveData for chart entries
        observeChartData()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Toolbar setup
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        setupRecyclerView()
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


        if (!UserSession.isLoggedIn(requireContext())) {
            showLoginDialog()
        } else {
            loadHistoryData()
        }


        binding.switchToSimple.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                historyViewModel.entriesA.observe(viewLifecycleOwner, { entriesA ->
                    historyViewModel.entriesB.observe(viewLifecycleOwner, { entriesB ->
                        historyViewModel.entriesC.observe(viewLifecycleOwner, { entriesC ->
                            historyViewModel.entriesD.observe(viewLifecycleOwner, { entriesD ->
                                val averageEntries =
                                    calculateAverageEntries(entriesA, entriesB, entriesC, entriesD)
                                val averageDataSet =
                                    LineDataSet(averageEntries, getString(R.string.simple_value)).apply {
                                        color = resources.getColor(android.R.color.holo_red_dark)
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

        // Set the click listener for the adapter
        trainingAdapter.setOnItemClickListener { session ->
            val bundle = Bundle().apply {
                putParcelable("sessionData", session)
            }
            findNavController().navigate(
                R.id.trainingDetailFragment,
                bundle
            )
        }
    }

    private fun observeChartData() {
        historyViewModel.entriesA.observe(viewLifecycleOwner, { entriesA ->
            val dataSetA = LineDataSet(entriesA, "Kejelasan").apply {
                color = resources.getColor(android.R.color.holo_blue_dark)
            }

            historyViewModel.entriesB.observe(viewLifecycleOwner, { entriesB ->
                val dataSetB = LineDataSet(entriesB, "Diksi").apply {
                    color = resources.getColor(android.R.color.holo_green_dark)
                }

                historyViewModel.entriesC.observe(viewLifecycleOwner, { entriesC ->
                    val dataSetC = LineDataSet(entriesC, "Kelancaran").apply {
                        color = resources.getColor(android.R.color.holo_red_dark)
                    }

                    historyViewModel.entriesD.observe(viewLifecycleOwner, { entriesD ->
                        val dataSetD = LineDataSet(entriesD, "Emosi").apply {
                            color = resources.getColor(android.R.color.holo_orange_dark)
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

        // lineChart.axisLeft.axisMinimum = 0f
        // lineChart.axisLeft.axisMaximum = 100f
        lineChart.axisRight.isEnabled = false
        lineChart.description.text = "Latihan Ke: "
        lineChart.invalidate()
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

    private fun showLoginDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_box_login)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val signInButton: Button = dialog.findViewById(R.id.btnSignIn)
        val googleSignInButton: ImageView = dialog.findViewById(R.id.btnClose)

        signInButton.setOnClickListener {
            startActivity(Intent(requireContext(), SignInActivity::class.java))
            dialog.dismiss()
        }

        googleSignInButton.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(requireContext(), MainActivity::class.java))
        }

        dialog.show()
    }

    private fun loadHistoryData() {
        historyViewModel.getAllHistory(
            Authenticator.getToken(requireContext()) ?: "",
            Authenticator.getUserId(requireContext()) ?: ""
        )
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
