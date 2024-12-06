package com.example.kaizenspeaking.ui.instructions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.kaizenspeaking.MainActivity
import com.example.kaizenspeaking.R
import com.example.kaizenspeaking.ui.instructions.adapter.OnboardingAdapter
import com.example.kaizenspeaking.ui.instructions.animation.CubeInScalingTransformation
import com.example.kaizenspeaking.ui.instructions.data.OnboardingItem

class OnboardingActivity : AppCompatActivity() {

    private lateinit var onboardingViewPager: ViewPager2
    private lateinit var indicatorContainer: LinearLayout
    private lateinit var buttonBack: TextView
    private lateinit var buttonNext: Button
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

        fun hasSeenOnboarding(context: Context): Boolean {
            return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getBoolean(KEY_HAS_SHOWN_ONBOARDING, false)
        }

        fun startWithCheck(context: Context) {
            if (!hasSeenOnboarding(context)) {
                context.startActivity(Intent(context, OnboardingActivity::class.java))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (hasSeenOnboarding(this) && !isManualStart()) {
            proceedToMainActivity()
            return
        }

        setContentView(R.layout.activity_onboarding)

        onboardingViewPager = findViewById(R.id.onboardingViewPager)
        indicatorContainer = findViewById(R.id.indicatorContainer)
        buttonBack = findViewById(R.id.buttonBack)
        buttonNext = findViewById(R.id.buttonNext)

        setupOnboardingItems()
        setupNavigationButtons()
        setupPageChangeCallbacks()
    }

    private fun isManualStart(): Boolean {
        return intent.getBooleanExtra("manual_start", false)
    }

    private fun setupOnboardingItems() {
        adapter = OnboardingAdapter(
            listOf(
                OnboardingItem(
                    R.drawable.img_instructions_1,
                    getString(R.string.text_instruction_1),
                    getString(R.string.desc_instruction_1),
                    R.drawable.background_instructions_1
                ),
                OnboardingItem(
                    R.drawable.img_instructions_2,
                    getString(R.string.text_instruction_2),
                    getString(R.string.desc_instruction_2),
                    R.drawable.background_instruction_2
                ),
                OnboardingItem(
                    R.drawable.img_instructions_3,
                    getString(R.string.text_instruction_3),
                    getString(R.string.desc_instruction_3),
                    R.drawable.background_instruction_3
                ),
                OnboardingItem(
                    R.drawable.img_instructions_4,
                    getString(R.string.text_instruction_4),
                    getString(R.string.desc_instruction_4),
                    R.drawable.background_instruction_4
                ),
                OnboardingItem(
                    R.drawable.img_instructions_5,
                    getString(R.string.text_instruction_5),
                    getString(R.string.desc_instruction_5),
                    R.drawable.background_instruction_5
                )
            )
        )
        onboardingViewPager.adapter = adapter
        onboardingViewPager.setPageTransformer(CubeInScalingTransformation())
    }

    private fun setupNavigationButtons() {
        buttonBack.setOnClickListener {
            if (onboardingViewPager.currentItem == 0) {
                finish()
            } else {
                onboardingViewPager.currentItem -= 1
            }
        }

        buttonNext.setOnClickListener {
            if (onboardingViewPager.currentItem == adapter.itemCount - 1) {
                completeOnboarding()
            } else {
                onboardingViewPager.currentItem += 1
            }
        }
    }

    private fun setupPageChangeCallbacks() {
        onboardingViewPager.registerOnPageChangeCallback(
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
        indicatorContainer.removeAllViews()

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
            indicatorContainer.addView(indicator)
        }
    }

    private fun updateNavigationButtons(position: Int) {
        buttonBack.visibility = View.VISIBLE
        buttonNext.text = if (position == adapter.itemCount - 1) "Selesai" else "Selanjutnya"
        buttonNext.setBackgroundResource(
            when (position) {
                0 -> R.drawable.btn_next_yellow
                1 -> R.drawable.btn_next_red
                2 -> R.drawable.btn_next_yellow
                3 -> R.drawable.btn_next_red
                else -> R.drawable.btn_next_yellow
            }
        )
    }

    private fun completeOnboarding() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_HAS_SHOWN_ONBOARDING, true)
            .apply()

        proceedToMainActivity()
    }

    private fun proceedToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}