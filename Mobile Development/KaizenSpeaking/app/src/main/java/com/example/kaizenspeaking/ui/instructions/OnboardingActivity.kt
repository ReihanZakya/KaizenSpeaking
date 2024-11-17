package com.example.kaizenspeaking.ui.instructions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.kaizenspeaking.R
import com.example.kaizenspeaking.databinding.ActivityOnboardingBinding
import com.example.kaizenspeaking.ui.instructions.adapter.OnboardingAdapter
import com.example.kaizenspeaking.ui.instructions.data.OnboardingItem

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var adapter: OnboardingAdapter

    companion object {
        private const val PREFS_NAME = "OnboardingPrefs"
        private const val KEY_HAS_SHOWN_ONBOARDING = "hasShownOnboarding"

        fun startManually(context: Context) {
            val intent = Intent(context, OnboardingActivity::class.java).apply {
                putExtra("manual_start", true)
            }
            context.startActivity(intent)
        }

        // Function to check if onboarding has been shown
        fun hasSeenOnboarding(context: Context): Boolean {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_HAS_SHOWN_ONBOARDING, false)
        }

        // Function to start onboarding with check
        fun startWithCheck(context: Context) {
            if (!hasSeenOnboarding(context)) {
                context.startActivity(Intent(context, OnboardingActivity::class.java))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if onboarding should be shown
        if (hasSeenOnboarding(this) && !isManualStart()) {
            proceedToMainActivity()
            return
        }

        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupOnboardingItems()
        setupNavigationButtons()
        setupPageChangeCallbacks()
    }

    private fun isManualStart(): Boolean {
        // Check if activity was started manually (from button click)
        return intent.getBooleanExtra("manual_start", false)
    }

    private fun setupOnboardingItems() {
        adapter = OnboardingAdapter(
            listOf(
                OnboardingItem(
                    R.drawable.img_instructions_1,
                    "Pastikan Anda berada di ruangan yang senyap untuk mengurangi gangguan suara. ",
                    "Untuk hasil analisis suara yang akurat, pastikan Anda berada di ruangan yang senyap dan minim gangguan suara. Keheningan lingkungan sekitar akan membantu aplikasi dalam mendeteksi dan menganalisis ucapan dengan lebih jelas, sehingga memberikan hasil yang lebih optimal.",
                    R.color.red_onboarding,
                ),
                OnboardingItem(
                    R.drawable.img_instructions_2,
                    "Persiapkan topik pembicaraan Anda",
                    "Sebelum memulai rekaman, siapkan topik yang ingin Anda bicarakan. Hal ini akan membantu Anda berbicara dengan lebih lancar dan terstruktur.",
                    R.color.yellow_onboarding,
                ),
                OnboardingItem(
                    R.drawable.img_instructions_3,
                    "Mulai rekaman dengan suara yang jelas",
                    "Bicaralah dengan suara yang jelas dan tempo yang sesuai. Hindari berbicara terlalu cepat atau terlalu lambat.",
                    R.color.red_onboarding,
                ),
                OnboardingItem(
                    R.drawable.img_instructions_4,
                    "Tunggu hasil analisis",
                    "Setelah selesai merekam, tunggu beberapa saat untuk sistem menganalisis rekaman Anda. Hasil analisis akan menunjukkan berbagai aspek dari cara berbicara Anda.",
                    R.color.yellow_onboarding,
                ),
                OnboardingItem(
                    R.drawable.img_instructions_5,
                    "Perhatikan hasil analisis",
                    "Pelajari hasil analisis dengan baik untuk mengetahui area mana yang perlu ditingkatkan dalam kemampuan berbicara Anda.",
                    R.color.red_onboarding,
                )
            )
        )
        binding.onboardingViewPager.adapter = adapter
    }

    private fun setupNavigationButtons() {
        // Setup back button
        binding.buttonBack.setOnClickListener {
            if (binding.onboardingViewPager.currentItem == 0) {
                finish()
            } else {
                binding.onboardingViewPager.currentItem -= 1
            }
        }

        // Setup next button
        binding.buttonNext.setOnClickListener {
            if (binding.onboardingViewPager.currentItem == adapter.itemCount - 1) {
                completeOnboarding()
            } else {
                binding.onboardingViewPager.currentItem += 1
            }
        }

        // Add skip button if needed

    }

    private fun setupPageChangeCallbacks() {
        binding.onboardingViewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    updateIndicators(position)
                    updateNavigationButtons(position)
                }
            }
        )
    }

    private fun updateIndicators(position: Int) {
        binding.indicatorContainer.removeAllViews()

        val indicators = Array(adapter.itemCount) { index ->
            androidx.appcompat.widget.AppCompatImageView(this).apply {
                val padding = resources.getDimensionPixelSize(R.dimen.indicator_padding)
                setPadding(padding, 0, padding, 0)
                setImageResource(
                    if (index == position) R.drawable.indicator_active
                    else R.drawable.indicator_inactive
                )
            }
        }

        indicators.forEach { indicator ->
            binding.indicatorContainer.addView(indicator)
        }
    }

    private fun updateNavigationButtons(position: Int) {
        binding.buttonBack.visibility = View.VISIBLE

        // Update next button appearance based on position
        binding.buttonNext.setImageResource(
            if (position == adapter.itemCount - 1) {
                R.drawable.btn_next // Ganti dengan icon finish jika ada
            } else {
                R.drawable.btn_next
            }
        )

        // Show/hide skip button based on position

    }

    private fun completeOnboarding() {
        // Mark onboarding as completed
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_HAS_SHOWN_ONBOARDING, true)
            .apply()

        proceedToMainActivity()
    }

    private fun proceedToMainActivity() {
        finish()
    }
}