package com.example.kaizenspeaking.ui.instructions.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kaizenspeaking.databinding.ItemOnboardingBinding
import com.example.kaizenspeaking.ui.instructions.data.OnboardingItem

class OnboardingAdapter(val onboardingItems: List<OnboardingItem>) :
    RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    inner class OnboardingViewHolder(private val binding: ItemOnboardingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(onboardingItem: OnboardingItem) {
            with(binding) {
                imageOnboarding.setImageResource(onboardingItem.image)
                textTitle.text = onboardingItem.title
                textDescription.text = onboardingItem.description
                containerOnboarding.setBackgroundResource(onboardingItem.backgroundImage)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        return OnboardingViewHolder(
            ItemOnboardingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(onboardingItems[position])
    }

    override fun getItemCount(): Int = onboardingItems.size
}