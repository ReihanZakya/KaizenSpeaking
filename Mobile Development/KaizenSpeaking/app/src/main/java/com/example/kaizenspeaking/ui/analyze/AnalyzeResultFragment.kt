package com.example.kaizenspeaking.ui.analyze

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import com.ekn.gruzer.gaugelibrary.HalfGauge
import com.ekn.gruzer.gaugelibrary.Range
import com.example.kaizenspeaking.databinding.FragmentAnalyzeResultBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.kaizenspeaking.MainActivity
import com.example.kaizenspeaking.R
import com.example.kaizenspeaking.ui.analyze.data.response.Score
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener


@Suppress("DEPRECATION")
class AnalyzeResultFragment : Fragment() {

    private var _binding: FragmentAnalyzeResultBinding? = null
    private val binding get() = _binding!!

    private lateinit var barChart: BarChart
    private lateinit var halfGauge: HalfGauge
    private lateinit var scrollView: ScrollView
    private lateinit var cardViewAnalysis: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyzeResultBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Delay hiding the Action Bar after the fragment's view is fully created
        Handler(Looper.getMainLooper()).post {
            (activity as AppCompatActivity).supportActionBar?.hide()
        }

        // Hide Bottom Navigation after fragment view is created
        Handler(Looper.getMainLooper()).post {
            hideBottomNavigation()
        }

        // Toolbar setup
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            // Start MainActivity when toolbar is pressed
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)

//            // Show Bottom Navigation again
//            showBottomNavigation()

            // Optionally, finish current activity if you don't want to keep it in the back stack
            requireActivity().finish()
        }

        // Handle back button press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Start MainActivity when back button is pressed
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)

//                // Show Bottom Navigation again
//                showBottomNavigation()

                // Optionally, finish current activity if you don't want to keep it in the back stack
                requireActivity().finish()
            }
        })

        val score = arguments?.getParcelable<Score>("score")
        val analyzeMessage = arguments?.getString("analyze_message")

        if (score != null && analyzeMessage != null) {
            setupBarChart(score)
            setupGaugeChart(score)

            val htmlMessage = convertToHtml(analyzeMessage)
            binding.tvAnalyzeResult.text = Html.fromHtml(htmlMessage, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Toast.makeText(requireContext(), "Data tidak ditemukan", Toast.LENGTH_SHORT).show()
        }

        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                e?.let { entry ->
                    val (title, description) = when (entry.x.toInt()) {
                        0 -> getString(R.string.matrix_kejelasan) to getString(R.string.description_matrix_kejelasan)
                        1 -> getString(R.string.matrix_diksi) to getString(R.string.description_matrix_diksi)
                        2 -> getString(R.string.matrix_kelancaran) to getString(R.string.description_matrix_kelancaran)
                        3 -> getString(R.string.matrix_emosi) to getString(R.string.description_matrix_emosi)
                        else -> "Informasi" to "Deskripsi tidak tersedia"
                    }

                    // Tampilkan AlertDialog
                    AlertDialog.Builder(requireContext())
                        .setTitle(title) // Set judul dialog
                        .setMessage(
                            Html.fromHtml(
                                description,
                                Html.FROM_HTML_MODE_LEGACY
                            )
                        ) // Render HTML pada deskripsi
                        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() } // Tombol OK
                        .show()
                }
            }

            override fun onNothingSelected() {
                // Tidak melakukan apa-apa jika tidak ada yang dipilih
            }
        })

        // Auto-scroll to "Hasil Analisis" section after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            scrollView.smoothScrollTo(0, cardViewAnalysis.top)
        }, 3000)
    }

    fun convertToHtml(text: String): String {
        val boldText = text.replace(Regex("\\*\\*(.*?)\\*\\*"), "<b>$1</b>")
        val listLines = mutableListOf<String>()
        val finalText = StringBuilder()
        var isInList = false

        boldText.lines().forEach { line ->
            when {
                line.trim().startsWith("-") -> {
                    isInList = true
                    listLines.add("<li>${line.trim().substring(1).trim()}</li>")
                }
                line.isBlank() -> {
                    if (isInList) {
                        finalText.append("<ul>${listLines.joinToString("")}</ul>")
                        listLines.clear()
                        isInList = false
                    }
                    finalText.append("<br>")
                }
                else -> {
                    if (isInList) {
                        finalText.append("<ul>${listLines.joinToString("")}</ul>")
                        listLines.clear()
                        isInList = false
                    }
                    finalText.append(line).append("<br>")
                }
            }
        }
        if (isInList) {
            finalText.append("<ul>${listLines.joinToString("")}</ul>")
        }
        return "<div style=\"text-align: justify;\">$finalText</div>"
    }

    private fun setupBarChart(score: Score?) {
        if (score == null) return

        barChart = binding.barchart
        halfGauge = binding.gaugeChart
        scrollView = binding.scrollView
        cardViewAnalysis = binding.cardViewAnalysis

        barChart.axisRight.setDrawLabels(false)
        barChart.axisLeft.axisMinimum = 0f // Set minimum value for Y axis
        barChart.axisLeft.axisMaximum = 100f // Set maximum value for Y axis

        // Data for the chart (representing 4 categories: Kejelasan, Diksi, Kelancaran, Emosi)
        val entriesKejelasan = ArrayList<BarEntry>()
        val entriesDiksi = ArrayList<BarEntry>()
        val entriesKelancaran = ArrayList<BarEntry>()
        val entriesEmosi = ArrayList<BarEntry>()

        // Example data for each category
        entriesKejelasan.add(BarEntry(0f, score.kejelasan?.toFloatOrNull() ?: 0f))
        entriesDiksi.add(BarEntry(1f, score.diksi?.toFloatOrNull() ?: 0f))
        entriesKelancaran.add(BarEntry(2f, score.kelancaran?.toFloatOrNull() ?: 0f))
        entriesEmosi.add(BarEntry(3f, score.emosi?.toFloatOrNull() ?: 0f))

        // Create BarDataSets for each category
        val barDataSetKejelasan = BarDataSet(entriesKejelasan, "Kejelasan")
        val barDataSetDiksi = BarDataSet(entriesDiksi, "Diksi")
        val barDataSetKelancaran = BarDataSet(entriesKelancaran, "Kelancaran")
        val barDataSetEmosi = BarDataSet(entriesEmosi, "Emosi")

        // Set colors for each category
        barDataSetKejelasan.color = resources.getColor(android.R.color.holo_blue_light)
        barDataSetDiksi.color = resources.getColor(android.R.color.holo_green_light)
        barDataSetKelancaran.color = resources.getColor(android.R.color.holo_red_light)
        barDataSetEmosi.color = resources.getColor(android.R.color.holo_orange_light)

        // Create BarData
        val barData = BarData(barDataSetKejelasan, barDataSetDiksi, barDataSetKelancaran, barDataSetEmosi)

        // Set data to the chart
        barChart.data = barData

        // Disable X axis labels
        barChart.xAxis.setDrawLabels(false)

        barChart.description.text = " "

        // Refresh chart to render the new data
        barChart.invalidate()

    }

    private fun setupGaugeChart(score: Score?) {
        if (score == null) return

        // Hitung rata-rata dari skor
        val scores = listOfNotNull(
            score.kejelasan?.toFloatOrNull() ?: 0f,
            score.diksi?.toFloatOrNull() ?: 0f,
            score.kelancaran?.toFloatOrNull() ?: 0f,
            score.emosi?.toFloatOrNull() ?: 0f
        )
        val averageScore = if (scores.isNotEmpty()) scores.average().toFloat() else 0f

        // Konfigurasi HalfGauge
        val range1 = Range().apply {
            color = resources.getColor(android.R.color.holo_red_light)
            from = 0.0
            to = 33.3
        }

        val range2 = Range().apply {
            color = resources.getColor(android.R.color.holo_orange_light)
            from = 33.3
            to = 66.6
        }

        val range3 = Range().apply {
            color = resources.getColor(android.R.color.holo_green_light)
            from = 66.6
            to = 100.0
        }

        // Add color ranges to gauge
        halfGauge.addRange(range1)
        halfGauge.addRange(range2)
        halfGauge.addRange(range3)

        // Set min, max, and current value for the HalfGauge
        halfGauge.minValue = 0.0
        halfGauge.maxValue = 100.0
        halfGauge.value = averageScore.toDouble()  // example value


    }

    private fun setupAnalyzeWords(words: List<String>) {
        // Gabungkan kata-kata menjadi satu string dengan pemisah koma
        val wordsText = words.joinToString(", ")

        // Tampilkan di TextView
        binding.tvAnalyzeResult.text = wordsText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        showBottomNavigation()
    }

    private fun hideBottomNavigation() {
        val bottomNav = requireActivity().findViewById<View>(R.id.nav_view)
        bottomNav?.visibility = View.GONE
    }
    private fun showBottomNavigation() {
        val bottomNav = requireActivity().findViewById<View>(R.id.nav_view)
        bottomNav?.visibility = View.VISIBLE
    }
}
