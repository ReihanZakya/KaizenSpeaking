package com.example.kaizenspeaking.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kaizenspeaking.R
import com.example.kaizenspeaking.databinding.FragmentProfileBinding
import com.example.kaizenspeaking.utils.UserSession
import com.example.kaizenspeaking.MainActivity


        class ProfileFragment : Fragment() {
            private lateinit var binding: FragmentProfileBinding

            override fun onCreateView(
                inflater: LayoutInflater, container: ViewGroup?,
                savedInstanceState: Bundle?
            ): View {
                binding = FragmentProfileBinding.inflate(inflater, container, false)

                // Add logout functionality
                binding.logoutButton.setOnClickListener {
                    // Clear login session
                    UserSession.setLoggedIn(requireContext(), false)

                    // Navigate back to home/login screen
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }

                return binding.root
            }
        }