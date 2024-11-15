package com.example.kaizenspeaking.ui.analyze

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kaizenspeaking.R
import com.example.kaizenspeaking.databinding.FragmentAnalyzeBinding
import com.example.kaizenspeaking.databinding.FragmentAnalyzeResultBinding


class AnalyzeResultFragment : Fragment() {

    private var _binding: FragmentAnalyzeResultBinding? = null

    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyzeResultBinding.inflate(inflater,container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}