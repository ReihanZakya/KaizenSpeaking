package com.example.kaizenspeaking.ui.analyze

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.fragment.findNavController
import com.example.kaizenspeaking.R
import com.example.kaizenspeaking.databinding.FragmentAnalyzeBinding
import com.example.kaizenspeaking.helper.SharedPreferencesHelper
import com.example.kaizenspeaking.ui.analyze.Service.UploadForegroundService
import com.example.kaizenspeaking.ui.analyze.data.response.Score
import com.example.kaizenspeaking.ui.instructions.OnboardingActivity
import com.example.kaizenspeaking.utils.UserSession
import java.io.File

class AnalyzeFragment : Fragment() {

    private var _binding: FragmentAnalyzeBinding? = null

    private val binding get() = _binding!!

    private var state = 0
    private val handler = Handler(Looper.getMainLooper())
    private var hasShownOnboarding = false

    companion object {
        private const val PREFS_NAME = "OnboardingPrefs"
        private const val KEY_HAS_SHOWN_ONBOARDING = "hasShownOnboarding"
        private const val ONBOARDING_DELAY = 5000L // 5 seconds
    }

    //audio
    private var mediaRecorder: MediaRecorder? = null
    private var tempFile: File? = null

    private val REQUEST_CODE_RECORD_AUDIO = 1

    // Time
    private var isRunning = false
    private var elapsedTime: Long = 0L
    private var startTime: Long = 0L
    private lateinit var handlerTimer: Handler
    private lateinit var runnable: Runnable

    private var receiver: BroadcastReceiver? = null

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(requireContext(), "Notifikasi diizinkan", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Izin notifikasi ditolak", Toast.LENGTH_SHORT)
                    .show()
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
        _binding = FragmentAnalyzeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Toolbar setup
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Cek dan minta izin POST_NOTIFICATIONS jika diperlukan
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        checkAndShowOnboarding()
        checkAudioPermission()

        binding.btnViewIntructions.setOnClickListener {
            startOnboardingManually()
        }

//        button
        binding.btnMultiFunction.text = getString(R.string.start_record)
        binding.btnMultiFunction.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        binding.btnMultiFunction.setBackgroundResource(R.drawable.btn_gray)

//        mic
        binding.imgMic.setImageResource(R.drawable.mic_off)

        binding.imgMic.setOnClickListener {
            handleButtonClick()
        }
        binding.btnMultiFunction.setOnClickListener {
            handleButtonClick()
        }
    }

    private fun checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_CODE_RECORD_AUDIO
            )
        } else {
            // Permission already granted, perform your action
            performAction()
        }
    }

    private fun performAction() {
        // Your logic when permission is granted
        Toast.makeText(requireContext(), "Izin merekam diberikan!", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Refresh fragment after permission granted
                refreshFragment()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Izin merekam ditolak, tolong izinkan!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun refreshFragment() {
        parentFragmentManager.beginTransaction().apply {
            detach(this@AnalyzeFragment)
            attach(this@AnalyzeFragment)
            commit()
        }
    }

    private fun startOnboardingManually() {
        val intent = Intent(requireContext(), OnboardingActivity::class.java).apply {
            putExtra("manual_start", true) // Flag to indicate manual start
        }
        startActivity(intent)
    }

    private fun checkAndShowOnboarding() {
        // Check if onboarding has been shown before
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        hasShownOnboarding = prefs.getBoolean(KEY_HAS_SHOWN_ONBOARDING, false)

        if (!hasShownOnboarding) {
            // Set timer to show onboarding after 5 seconds
            handler.postDelayed({
                if (isAdded && !hasShownOnboarding) { // Check if fragment is still attached
                    showOnboarding()
                }
            }, ONBOARDING_DELAY)
        }
    }

    private fun showOnboarding() {
        if (!hasShownOnboarding) {
            // Mark onboarding as shown
            requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_HAS_SHOWN_ONBOARDING, true)
                .apply()

            hasShownOnboarding = true
        }

        // Show onboarding activity
        startActivity(Intent(requireContext(), OnboardingActivity::class.java))
    }

    private fun handleButtonClick() {
        when (state) {
            0 -> {
                val fileName = binding.etTopic.text.toString()
                if (fileName.isNotEmpty()) {
                    binding.btnMultiFunction.text = getString(R.string.stop)
                    binding.btnMultiFunction.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                    binding.btnMultiFunction.setBackgroundResource(R.drawable.btn_red)
                    binding.imgMic.setImageResource(R.drawable.mic_on)
                    startRecording(fileName)
                    // Memulai Stopwatch
                    startTime = System.currentTimeMillis() - elapsedTime
                    isRunning = true
                    startStopwatch()
                    state = 1
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Mohon masukkan topik terlebih dahulu",
                        Toast.LENGTH_SHORT
                    ).show()
                    state = 0
                }

            }

            1 -> {
                binding.btnMultiFunction.text = getString(R.string.analyze)
                binding.btnMultiFunction.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.red
                    )
                )
                binding.btnMultiFunction.setBackgroundResource(R.drawable.btn_gray)
                binding.imgMic.setImageResource(R.drawable.mic_off)
                stopRecording()
                state = 2
            }

            2 -> {
                binding.btnMultiFunction.text = getString(R.string.start_record)
                binding.btnMultiFunction.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                sendDataToApi()
                elapsedTime = 0L // Reset waktu
                binding.tvTimer.text = "00:00"
                state = 0
            }
        }
    }

    private fun startRecording(fileName: String) {

        try {
            // Buat file sementara di cache directory
            val cacheDir = requireContext().cacheDir
            tempFile = File.createTempFile(fileName, ".m4a", cacheDir)

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(tempFile?.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Gagal merekam audio", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startStopwatch() {
        handlerTimer = Handler()
        runnable = object : Runnable {
            override fun run() {
                if (isRunning) {
                    val now = System.currentTimeMillis()
                    elapsedTime = now - startTime
                    binding.tvTimer.text = formatTime(elapsedTime)
                    handlerTimer.postDelayed(this, 1000) // Update setiap detik
                }
            }
        }
        handlerTimer.post(runnable)
    }

    private fun formatTime(ms: Long): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            isRunning = false
            handlerTimer.removeCallbacks(runnable)
            mediaRecorder = null
            Toast.makeText(requireContext(), "Recording stopped", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Gagal menghentikan perekaman", Toast.LENGTH_LONG)
                .show()
        }
    }


    private fun sendDataToApi() {
        val topic = binding.etTopic.text.toString()
        val deviceId =
            SharedPreferencesHelper.getFromSharedPreferences(requireContext(), "device_id")
                ?: "unknown_device"
        val userId = UserSession.getUserId(requireContext()) ?: ""
        if (tempFile == null || !tempFile!!.exists()) {
            Toast.makeText(requireContext(), "File audio tidak ditemukan", Toast.LENGTH_LONG).show()
            return
        }

        val serviceIntent = Intent(requireContext(), UploadForegroundService::class.java).apply {
            putExtra(UploadForegroundService.EXTRA_TOPIC, topic)
            if (userId.isEmpty()) {
                putExtra(UploadForegroundService.EXTRA_DEVICE_ID, deviceId)
                Log.d("ServiceIntent", "Mengirim deviceId: $deviceId")
            } else {
                putExtra(UploadForegroundService.EXTRA_USER_ID, userId)
                Log.d("ServiceIntent", "Mengirim deviceId: $userId")
            }
            putExtra(UploadForegroundService.EXTRA_FILE_PATH, tempFile!!.absolutePath)
        }

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Sedang Menjalankan Proses")
            .setIcon(R.drawable.ic_kaizen)
            .setMessage(
                "Proses analisis sedang berlangsung, anda bisa menunggu sambil meninggalkalkan aplikasi tetapi " +
                        "jangan menghapusnya dari background. Cek notifikasi untuk melihat hasil analisis"
            )
            .setCancelable(false) // Tidak bisa ditutup oleh pengguna
            .create()
        alertDialog.show()

//        receiver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context, intent: Intent) {
//                val result: Pair<Score, String>? =
//                    intent.getSerializableExtra("result") as? Pair<Score, String>
//                alertDialog.dismiss()
//
//                if (result != null) {
//                    val (score, message) = result
//                    val bundle = Bundle().apply {
//                        putParcelable("score", score)
//                        putString("analyze_message", message)
//                    }
//                    val pendingIntent = NavDeepLinkBuilder(requireContext())
//                        .setGraph(R.navigation.mobile_navigation)
//                        .setDestination(R.id.analyzeResultFragment)
//                        .setArguments(bundle)
//                        .createPendingIntent()
//
//                    val notificationManager =
//                        requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//                    val notification =
//                        NotificationCompat.Builder(requireContext(), "analysis_channel")
//                            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
//                            .setContentTitle("Analisis Selesai")
//                            .setContentText("Klik untuk melihat hasil analisis")
//                            .setContentIntent(pendingIntent)
//                            .setAutoCancel(true)
//                            .build()
//
//                    notificationManager.notify(2, notification)
//                } else {
//                    AlertDialog.Builder(requireContext())
//                        .setTitle("Analisis Gagal")
//                        .setMessage("Terjadi kesalahan saat menganalisis data. Silakan coba lagi.")
//                        .setPositiveButton("OK") { dialog, _ ->
//                            dialog.dismiss()
//                        }
//                        .create()
//                        .show()
//
//                    Toast.makeText(requireContext(), "Gagal menganalisis data", Toast.LENGTH_LONG)
//                        .show()
//                }
//                // Unregister receiver setelah tugas selesai
//                LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(this)
//                receiver = null
//            }
//        }

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    "ANALYZE_RESULT" -> {
                        val result: Pair<Score, String>? =
                            intent.getSerializableExtra("result") as? Pair<Score, String>
                        alertDialog.dismiss()

                        if (result != null) {
                            val (score, message) = result
                            val bundle = Bundle().apply {
                                putParcelable("score", score)
                                putString("analyze_message", message)
                            }
                            val pendingIntent = NavDeepLinkBuilder(requireContext())
                                .setGraph(R.navigation.mobile_navigation)
                                .setDestination(R.id.analyzeResultFragment)
                                .setArguments(bundle)
                                .createPendingIntent()

                            val notificationManager =
                                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                            val notification =
                                NotificationCompat.Builder(requireContext(), "analysis_channel")
                                    .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                                    .setContentTitle("Analisis Selesai")
                                    .setContentText("Klik untuk melihat hasil analisis")
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true)
                                    .build()

                            notificationManager.notify(2, notification)

                            // Tampilkan dialog berhasil
                            AlertDialog.Builder(requireContext())
                                .setTitle("Analisis Berhasil")
                                .setIcon(R.drawable.ic_kaizen)
                                .setMessage("Hasil analisis sudah tersedia. Apakah Anda ingin melihat hasilnya?")
                                .setPositiveButton("Lihat Hasil") { dialog, _ ->
                                    dialog.dismiss()

                                    // Navigasi ke AnalyzeResultFragment
                                    val bundle = Bundle().apply {
                                        putParcelable("score", score)
                                        putString("analyze_message", message)
                                    }
                                    findNavController().navigate(
                                        R.id.analyzeResultFragment,
                                        bundle
                                    )
                                }
                                .create()
                                .show()
                        } else {
                            // Fallback error handling
                            showFailureDialog()
                        }
                    }

                    "ANALYZE_RESULT_FAILURE" -> {
                        // Tutup dialog "proses" jika masih ditampilkan
                        alertDialog.dismiss()

                        // Tampilkan dialog "gagal"
                        showFailureDialog()
                    }
                }

                // Unregister receiver setelah tugas selesai
                LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(this)
                receiver = null
            }
        }

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            receiver!!,
            IntentFilter().apply {
                addAction("ANALYZE_RESULT")
                addAction("ANALYZE_RESULT_FAILURE")
            }
        )


        ContextCompat.startForegroundService(requireContext(), serviceIntent)
    }

    private fun showFailureDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Analisis Gagal")
            .setIcon(R.drawable.ic_kaizen)
            .setMessage("Terjadi kesalahan saat menganalisis data. Silakan coba lagi.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        receiver?.let {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(it)
            receiver = null
        }
    }
}