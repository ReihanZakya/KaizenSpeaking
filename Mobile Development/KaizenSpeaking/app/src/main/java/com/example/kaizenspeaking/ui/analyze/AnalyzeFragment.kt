package com.example.kaizenspeaking.ui.analyze

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.kaizenspeaking.R
import com.example.kaizenspeaking.databinding.FragmentAnalyzeBinding
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
//                startRecording()
                state = 1 // Update ke state berikutnya
            }
            1 -> {
                // Tahap 2: Stop merekam
                binding.btnMultiFunction.text = getString(R.string.analyze)
                binding.btnMultiFunction.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                binding.btnMultiFunction.setBackgroundResource(R.drawable.btn_gray)
                binding.imgMic.setImageResource(R.drawable.mic_off)
//                stopRecording()
                state = 2 // Update ke state berikutnya
            }
            2 -> {
                // Tahap 3: Pindah ke fragment analisis
                binding.btnMultiFunction.text = getString(R.string.start_record)
                binding.btnMultiFunction.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                navigateToAnalysisFragment()
                state = 0 // Reset ke state awal (opsional jika button digunakan lagi)
            }
        }
    }

//    private fun startRecording() {
//        // Logika untuk memulai perekaman
//    }
//
//    private fun stopRecording() {
//        // Logika untuk menghentikan perekaman
//    }
//
    private fun navigateToAnalysisFragment() {
        // Logika untuk berpindah ke fragment analisis
        findNavController().navigate(R.id.analyzeResultFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}