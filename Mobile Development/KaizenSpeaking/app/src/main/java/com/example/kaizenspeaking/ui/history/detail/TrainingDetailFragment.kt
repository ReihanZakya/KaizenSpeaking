package com.example.kaizenspeaking.ui.history.detail

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toolbar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ekn.gruzer.gaugelibrary.HalfGauge
import com.ekn.gruzer.gaugelibrary.Range
import com.example.kaizenspeaking.R
import com.example.kaizenspeaking.ui.history.data.TrainingSession
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.util.Locale

class TrainingDetailFragment : Fragment() {

    private lateinit var barChart: BarChart
    private lateinit var halfGauge: HalfGauge
    private lateinit var scrollView: ScrollView
    private lateinit var cardViewAnalisis: View
    private lateinit var titleTextView: TextView
    private lateinit var analizeTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var durationTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
        return inflater.inflate(R.layout.fragment_training_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.hide()
        hideBottomNavigation()

        // Toolbar setup
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val trainingSession: TrainingSession? = arguments?.getParcelable("sessionData")

        // Inisialisasi semua properti lateinit
        barChart = view.findViewById(R.id.barchart)
        halfGauge = view.findViewById(R.id.gauge_chart)
        scrollView = view.findViewById(R.id.scrollView)
        cardViewAnalisis = view.findViewById(R.id.cardViewAnalasis)
        titleTextView = view.findViewById(R.id.titleTextView)
        analizeTextView = view.findViewById(R.id.analizeTextView)
        dateTextView = view.findViewById(R.id.dateTextView)
        durationTextView = view.findViewById(R.id.durationTextView)

        // Konfigurasi BarChart
        barChart.axisRight.setDrawLabels(false)
        barChart.axisLeft.axisMinimum = 0f // Set minimum value for Y axis
        barChart.axisLeft.axisMaximum = 100f // Set maximum value for Y axis

        // Declare the variables outside the let block
        var kejelasan: Float = 0f
        var diksi: Float = 0f
        var kelancaran: Float = 0f
        var emosi: Float = 0f

        // Assign values inside the let block
        trainingSession?.let { session ->
            kejelasan = session.kejelasan?.toFloatOrNull() ?: 0f
            diksi = session.diksi?.toFloatOrNull() ?: 0f
            kelancaran = session.kelancaran?.toFloatOrNull() ?: 0f
            emosi = session.emosi?.toFloatOrNull() ?: 0f

            val rawAnalize = session.analize
            val formattedHtml = convertToHtml(rawAnalize) // Konversi ke HTML
            analizeTextView.text =
                Html.fromHtml(formattedHtml, Html.FROM_HTML_MODE_LEGACY) // Render HTML

            val formattedTitle = "Topik Pembicaraan: " + session.title
            titleTextView = view.findViewById(R.id.titleTextView)
            titleTextView.text = formattedTitle

            val originalFormat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
            val desiredFormat = SimpleDateFormat(
                "dd MMM yyyy HH:mm 'WIB'",
                Locale.getDefault()
            ) // Tambahkan literal WIB

            var dateExpected = session.date
            try {
                val date = originalFormat.parse(session.date) // Parsing string date
                if (date != null) {
                    // Gunakan Calendar untuk menyesuaikan jam
                    val calendar = Calendar.getInstance().apply {
                        time = date
                        val adjustedHour = (get(Calendar.HOUR_OF_DAY) + 7) % 24 // Penyesuaian jam
                        set(Calendar.HOUR_OF_DAY, adjustedHour) // Set jam yang sudah diubah
                    }

                    // Format tanggal dengan desiredFormat
                    val formattedDate = desiredFormat.format(calendar.time)
                    dateExpected = formattedDate
                } else {
                    // Jika parsing gagal, tampilkan string original
                    dateExpected = session.date
                }
            } catch (e: Exception) {
                e.printStackTrace()
                dateExpected = session.date // Jika terjadi error, tampilkan original date
            }


            val formattedDate = "Direkam Pada: " + dateExpected
            dateTextView = view.findViewById(R.id.dateTextView)
            dateTextView.text = formattedDate

            durationTextView = view.findViewById(R.id.durationTextView)

            session.duration?.let { duration ->
                // Pisahkan menit dan detik menggunakan split(":")
                val parts = duration.split(":")
                val minutes = parts.getOrNull(0)?.toIntOrNull() ?: 0
                val seconds = parts.getOrNull(1)?.toIntOrNull() ?: 0

                // Format teks berdasarkan nilai menit dan detik
                val formattedDuration = if (minutes > 0) {
                    "Durasi Pembicaraan: $minutes Menit $seconds Detik"
                } else {
                    "Durasi Pembicaraan: $seconds Detik"
                }
                // Tampilkan di TextView
                durationTextView.text = formattedDuration
            }
        }

        // Data for the chart (representing 4 categories: Kejelasan, Diksi, Kelancaran, Emosi)
        val entriesKejelasan = ArrayList<BarEntry>()
        val entriesDiksi = ArrayList<BarEntry>()
        val entriesKelancaran = ArrayList<BarEntry>()
        val entriesEmosi = ArrayList<BarEntry>()

        // Example data for each category
        entriesKejelasan.add(BarEntry(0f, kejelasan))
        entriesDiksi.add(BarEntry(1f, diksi))
        entriesKelancaran.add(BarEntry(2f, kelancaran))
        entriesEmosi.add(BarEntry(3f, emosi))

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
        val barData =
            BarData(barDataSetKejelasan, barDataSetDiksi, barDataSetKelancaran, barDataSetEmosi)

        // Set data to the chart
        barChart.data = barData

        // Disable X axis labels
        barChart.xAxis.setDrawLabels(false)

        barChart.description.text = " "

        // Refresh chart to render the new data
        barChart.invalidate()

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

        // Set min, max, and current value
        // Calculate average
        val average: Float = (kejelasan + diksi + kelancaran + emosi) / 4

        halfGauge.minValue = 0.0
        halfGauge.maxValue = 100.0
        halfGauge.value = average.toDouble()

        // Auto-scroll to "Hasil Analisis" section after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            scrollView.smoothScrollTo(0, cardViewAnalisis.top)
        }, 3000)

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
    }

    private fun convertToHtml(text: String): String {
        // Ganti "**{text}**" dengan "<b>{text}</b>"
        val boldText = text.replace(Regex("\\*\\*(.*?)\\*\\*"), "<b>$1</b>")

        // Simpan elemen daftar sementara untuk diolah ulang
        val listLines = mutableListOf<String>()
        val finalText = StringBuilder()
        var isInList = false

        // Proses baris satu per satu
        boldText.lines().forEach { line ->
            when {
                // Jika baris merupakan elemen daftar
                line.trim().startsWith("-") -> {
                    isInList = true
                    listLines.add("<li>${line.trim().substring(1).trim()}</li>")
                }
                // Jika baris kosong, selesaikan daftar jika ada
                line.isBlank() -> {
                    if (isInList) {
                        finalText.append("<ul>${listLines.joinToString("")}</ul>")
                        listLines.clear()
                        isInList = false
                    }
                    finalText.append("<br>")
                }
                // Baris biasa di luar daftar
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

        // Tambahkan elemen daftar yang tersisa
        if (isInList) {
            finalText.append("<ul>${listLines.joinToString("")}</ul>")
        }

        // Bungkus seluruh teks dalam div dengan gaya justify
        val justifiedHtml = "<div style=\"text-align: justify;\">$finalText</div>"

        // Log hasil akhir HTML
        Log.d("HTMLConverter", "Converted HTML: $justifiedHtml")
        return justifiedHtml
    }

    override fun onDestroyView() {
        super.onDestroyView()
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
