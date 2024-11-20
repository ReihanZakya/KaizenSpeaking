package com.example.kaizenspeaking.ui.analyze

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.kaizenspeaking.R
import com.example.kaizenspeaking.databinding.FragmentAnalyzeBinding
import java.io.File
import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Handler
import com.example.kaizenspeaking.ui.instructions.OnboardingActivity

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

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    private val REQUEST_CODE_RECORD_AUDIO = 1

    // Time
    private var isRunning = false
    private var elapsedTime: Long = 0L
    private var startTime: Long = 0L
    private lateinit var handlerTimer: Handler
    private lateinit var runnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyzeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkAndShowOnboarding()

//       Lihat Instruksi
        binding.btnViewIntructions.setOnClickListener {
            startOnboardingManually()
        }


//        button
        binding.btnMultiFunction.text = getString(R.string.start_record)
        binding.btnMultiFunction.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        binding.btnMultiFunction.setBackgroundResource(R.drawable.btn_gray)

//        mic
        binding.imgMic.setImageResource(R.drawable.mic_off)

        binding.btnMultiFunction.setOnClickListener {
            handleButtonClick()
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
                // Tahap 1: Mulai merekam
                binding.btnMultiFunction.text = getString(R.string.stop)
                binding.btnMultiFunction.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                binding.btnMultiFunction.setBackgroundResource(R.drawable.btn_red)
                binding.imgMic.setImageResource(R.drawable.mic_on)
                startRecording()
                state = 1
            }
            1 -> {
                // Tahap 2: Stop merekam
                binding.btnMultiFunction.text = getString(R.string.analyze)
                binding.btnMultiFunction.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                binding.btnMultiFunction.setBackgroundResource(R.drawable.btn_gray)
                binding.imgMic.setImageResource(R.drawable.mic_off)
                stopRecording()
                state = 2
            }
            2 -> {
                // Tahap 3: Pindah ke fragment analisis
                binding.btnMultiFunction.text = getString(R.string.start_record)
                binding.btnMultiFunction.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                navigateToAnalysisFragment()
                elapsedTime = 0L // Reset waktu
                binding.tvTimer.text = "00:00" // Reset tampilan stopwatch
                state = 0
            }
        }
    }

    private fun startRecording() {
        try {
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(requireContext())
            } else {
                MediaRecorder()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = requireContext().contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, "kaizenSpeaking.mp3")
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
                    put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
                }

                // Dapatkan URI untuk file audio
                val audioUri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw IllegalStateException("Failed to create MediaStore entry")

                // Buka ParcelFileDescriptor untuk mendapatkan FileDescriptor
                val parcelFileDescriptor = resolver.openFileDescriptor(audioUri, "w")
                    ?: throw IllegalStateException("Failed to open file descriptor")

                // Set Output File menggunakan FileDescriptor
                mediaRecorder?.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(parcelFileDescriptor.fileDescriptor) // Menggunakan FileDescriptor
                    prepare()
                    start()
                }
            } else {
                // Android 9 ke bawah: gunakan External Storage Public Directory
                audioFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                    "audio.mp3"
                )

                mediaRecorder?.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(audioFile!!.absolutePath)
                    prepare()
                    start()
                }
            }

            // Memulai Stopwatch
            startTime = System.currentTimeMillis() - elapsedTime
            isRunning = true
            startStopwatch()

            Toast.makeText(requireContext(), "Recording started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to start recording: ${e.message}", Toast.LENGTH_LONG).show()
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
                    handler.postDelayed(this, 1000) // Update setiap detik
                }
            }
        }
        handler.post(runnable)
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
                reset()
                release()
            }
            mediaRecorder = null

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                // Tampilkan lokasi file untuk Android 9 ke bawah
                Toast.makeText(
                    requireContext(),
                    "Recording saved: ${audioFile?.absolutePath}",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Recording saved in Music folder",
                    Toast.LENGTH_LONG
                ).show()
            }
            // Menghentikan Stopwatch
            isRunning = false
            handler.removeCallbacks(runnable)
            Toast.makeText(requireContext(), "Recording stopped", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to stop recording: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_RECORD_AUDIO && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }


    private fun navigateToAnalysisFragment() {
        // Logika untuk berpindah ke fragment analisis
        findNavController().navigate(R.id.analyzeResultFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}