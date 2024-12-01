package com.example.kaizenspeaking.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kaizenspeaking.R
import com.example.kaizenspeaking.databinding.FragmentHomeBinding
import com.example.kaizenspeaking.ui.auth.SignInActivity
import com.example.kaizenspeaking.ui.home_signed.HomeSignedFragment
import com.example.kaizenspeaking.ui.instructions.OnboardingActivity
import com.example.kaizenspeaking.utils.UserSession

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    //test
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Initialize RecyclerView with ArticleAdapter
        val articles = List(homeViewModel.articleTitles.size) { index ->
            Article(
                title = homeViewModel.articleTitles[index],
                description = homeViewModel.articleDescriptions[index],
                image = homeViewModel.articleImages[index],
                url = homeViewModel.articleUrls[index]
            )
        }
        val isLoggedIn = UserSession.isLoggedIn(requireContext())

        if (UserSession.isLoggedIn(requireContext())) {
            binding.homeFragment.visibility = View.GONE
        } else {
            binding.homeFragment.visibility = View.VISIBLE }

        val adapter = ArticleAdapter(requireContext(), articles)
        binding.ArticlesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.ArticlesRecyclerView.adapter = adapter

        // Set onClickListener for accountName
        binding.accountName.setOnClickListener {
            navigateToAuthentication() }

        binding.accountButton.setOnClickListener {
            navigateToAuthentication()
        }


        return binding.root
    }

    private fun  navigateToAuthentication() {
        val intent = Intent(requireContext(), SignInActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
