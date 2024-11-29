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
import com.example.kaizenspeaking.data.response.AnalyzeResponse
import com.example.kaizenspeaking.data.response.Score
import com.example.kaizenspeaking.data.retrofit.ApiConfig
import com.example.kaizenspeaking.helper.SharedPreferencesHelper
import com.example.kaizenspeaking.helper.SharedPreferencesHelper.getFromSharedPreferences
import com.example.kaizenspeaking.ui.instructions.OnboardingActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
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
    private var tempFile: File? = null

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
        checkAudioPermission()

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
        Toast.makeText(requireContext(), "Permission granted!", Toast.LENGTH_SHORT).show()
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
                    "Permission denied. Please grant permission to proceed.",
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
            // Menghentikan Stopwatch
            isRunning = false
            handlerTimer.removeCallbacks(runnable)
            mediaRecorder = null
            Toast.makeText(requireContext(), "Recording stopped", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Gagal menghentikan perekaman", Toast.LENGTH_LONG).show()
        }
    }


    private fun sendDataToApi() {
        val topic = binding.etTopic.text.toString()
        val deviceId = SharedPreferencesHelper.getFromSharedPreferences(requireContext(), "device_id") ?: "unknown_device"

        // Pastikan file sementara tersedia
        if (tempFile == null || !tempFile!!.exists()) {
            Toast.makeText(requireContext(), "File audio tidak ditemukan", Toast.LENGTH_LONG).show()
            return
        }

        // Konversi data menjadi multipart
        val topicRequestBody = topic.toRequestBody("text/plain".toMediaType())
        val deviceIdRequestBody = deviceId.toRequestBody("text/plain".toMediaType())
        val fileRequestBody = tempFile!!.asRequestBody("audio/m4a".toMediaType())
        val multipartFile = MultipartBody.Part.createFormData("file", tempFile!!.name, fileRequestBody)

        binding.progressBar.visibility = View.VISIBLE

        // Panggil API menggunakan Coroutine
        lifecycleScope.launch {
            try {
                val response = ApiConfig.instance.uploadRecording(topicRequestBody, multipartFile, deviceIdRequestBody)
                if (response.isSuccessful) {
                    val rawResponse = response.body()?.string()
                    Log.d("RawResponse", "Response: $rawResponse")

                    val analyzeResponse = parseAnalyzeResponse(rawResponse)
                    if (analyzeResponse != null) {
                        navigateToAnalysisFragment(analyzeResponse)
                    } else {
                        Toast.makeText(requireContext(), "Gagal memproses respons", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Log.e("UploadError", "Error uploading audio: ${response.errorBody()?.string()}")
                    Toast.makeText(requireContext(), "Gagal mengunggah audio", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("UploadError", "Error uploading audio: ${e.message}")
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun parseAnalyzeResponse(rawResponse: String?): AnalyzeResponse? {
        if (rawResponse.isNullOrEmpty()) return null

        val gson = Gson()
        val lines = rawResponse.split("\n").filter { it.isNotBlank() }

        var score: Score? = null
        val words = mutableListOf<String>()

        for (line in lines) {
            try {
                if (line.contains("score")) {
                    val jsonObject = gson.fromJson(line, JsonObject::class.java)
                    val scoreObject = jsonObject["score"]?.asJsonObject
                    if (scoreObject != null) {
                        score = Score(
                            kejelasan = scoreObject["kejelasan"]?.asString,
                            diksi = scoreObject["diksi"]?.asString,
                            kelancaran = scoreObject["kelancaran"]?.asString,
                            emosi = scoreObject["emosi"]?.asString
                        )
                    }
                } else if (line.contains("word")) {
                    val wordObject = gson.fromJson(line, JsonObject::class.java)
                    wordObject["word"]?.let { words.add(it.asString) }
                }
                Log.d("ParseResponse", "Score: $score")
                Log.d("ParseResponse", "Words: $words")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ParseError", "Error parsing response: ${e.message}")
            }
        }

        return if (score != null) {
            AnalyzeResponse(score, words)
        } else {
            null
        }
    }

    private fun navigateToAnalysisFragment(result: AnalyzeResponse) {
        val bundle = Bundle().apply {
            putParcelable("result", result)
        }
        findNavController().navigate(R.id.analyzeResultFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}