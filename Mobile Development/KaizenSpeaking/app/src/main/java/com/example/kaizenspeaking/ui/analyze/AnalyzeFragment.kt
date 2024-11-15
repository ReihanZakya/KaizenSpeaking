package com.example.kaizenspeaking.ui.analyze

import android.os.Bundle
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

class AnalyzeFragment : Fragment() {

    private var _binding: FragmentAnalyzeBinding? = null

    private val binding get() = _binding!!

    private var state = 0

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