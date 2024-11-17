package com.example.kaizenspeaking.ui.analyze

import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
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
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class AnalyzeFragment : Fragment() {

    private var _binding: FragmentAnalyzeBinding? = null

    private val binding get() = _binding!!

    private var state = 0

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    private val REQUEST_CODE_RECORD_AUDIO = 1

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

    private fun handleButtonClick() {
        when (state) {
            0 -> {
                binding.btnMultiFunction.text = getString(R.string.stop)
                binding.btnMultiFunction.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                binding.btnMultiFunction.setBackgroundResource(R.drawable.btn_red)
                binding.imgMic.setImageResource(R.drawable.mic_on)
                startRecording()
                state = 1
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

            Toast.makeText(requireContext(), "Recording started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to start recording: ${e.message}", Toast.LENGTH_LONG).show()
        }
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