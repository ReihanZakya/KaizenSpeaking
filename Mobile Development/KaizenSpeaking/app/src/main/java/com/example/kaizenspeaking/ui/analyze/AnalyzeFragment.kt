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
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.kaizenspeaking.data.retrofit.ApiConfig
import com.example.kaizenspeaking.helper.SharedPreferencesHelper
import com.example.kaizenspeaking.helper.SharedPreferencesHelper.getFromSharedPreferences
import com.example.kaizenspeaking.ui.instructions.OnboardingActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

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
                val fileName = binding.etTopic.text.toString()
                if (fileName.isNotEmpty()) {
                    binding.btnMultiFunction.text = getString(R.string.stop)
                    binding.btnMultiFunction.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    binding.btnMultiFunction.setBackgroundResource(R.drawable.btn_red)
                    binding.imgMic.setImageResource(R.drawable.mic_on)
                    startRecording(fileName)
                    // Memulai Stopwatch
                    startTime = System.currentTimeMillis() - elapsedTime
                    isRunning = true
                    startStopwatch()
                    state = 1
                }else{
                    Toast.makeText(requireContext(), "Mohom masukkan topic terlebih dahulu", Toast.LENGTH_SHORT).show()
                    state = 0
                }

            }
            1 -> {
                binding.btnMultiFunction.text = getString(R.string.analyze)
                binding.btnMultiFunction.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                binding.btnMultiFunction.setBackgroundResource(R.drawable.btn_gray)
                binding.imgMic.setImageResource(R.drawable.mic_off)
                stopRecording()
                state = 2
            }
            2 -> {
                binding.btnMultiFunction.text = getString(R.string.start_record)
                binding.btnMultiFunction.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                navigateToAnalysisFragment()
                sendDataToApi()
                elapsedTime = 0L // Reset waktu
                binding.tvTimer.text = "00:00"
                state = 0
            }
        }
    }

    private fun startRecording(fileName : String) {

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_CODE_RECORD_AUDIO
            )
            return
        }

        try {
            mediaRecorder?.release()
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(requireContext())
            } else {
                MediaRecorder()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = requireContext().contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, "$fileName.mp3")
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
                    "$fileName.mp3"
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



            Toast.makeText(requireContext(), "Recording started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("MediaRecorder", "Error starting recording: ${e.message}")
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
            handlerTimer.removeCallbacks(runnable)
            Toast.makeText(requireContext(), "Recording stopped", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("MediaRecorder", "Failed to stop recording: ${e.message}")
            Toast.makeText(requireContext(), "Failed to stop recording: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun sendDataToApi() {
        val topic = binding.etTopic.text.toString()
        val userId = SharedPreferencesHelper.getFromSharedPreferences(requireContext(), "device_id") ?: return
        val audioFilePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC)}/$topic.mp3"
        } else {
            audioFile?.absolutePath ?: ""
        }

        if (!File(audioFilePath).exists()) {
            Toast.makeText(requireContext(), "Audio file not found!", Toast.LENGTH_SHORT).show()
            return
        }

        // Prepare request body
        val topicPart = RequestBody.create("text/plain".toMediaTypeOrNull(), topic)
        val userIdPart = RequestBody.create("text/plain".toMediaTypeOrNull(), userId)

        val file = File(audioFilePath)
        val filePart = MultipartBody.Part.createFormData(
            "file",
            file.name,
            file.asRequestBody("audio/mpeg".toMediaTypeOrNull())
        )

        lifecycleScope.launch {
            try {
                val response = ApiConfig.instance.uploadRecording(topicPart, filePart, userIdPart)
                if (response.isSuccessful) {
                    val analyzeResponse = response.body()
                    if (analyzeResponse != null) {
                        val score = analyzeResponse.score
                        val words = analyzeResponse.words.joinToString(" ")

                        // Menampilkan hasil analisis
                        val resultMessage = """
                        Kejelasan: ${score.kejelasan}%
                        Diksi: ${score.diksi}%
                        Kelancaran: ${score.kelancaran}%
                        Emosi: ${score.emosi}%
                        
                        Words: $words
                    """.trimIndent()

                        Toast.makeText(requireContext(), resultMessage, Toast.LENGTH_LONG).show()
                    }
                } else {
                    Log.e("API_ERROR", response.errorBody()?.string() ?: "Unknown error")
                    Toast.makeText(requireContext(), "Upload Failed: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("API_ERROR", "Error uploading data: ${e.message}")
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
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