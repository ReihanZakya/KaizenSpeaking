package com.example.kaizenspeaking.ui.instructions.animation

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs
import kotlin.math.max


class CubeInScalingTransformation : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        page.cameraDistance = 20000f


        if (position < -1) {     // [-Infinity,-1)
            // This page is way off-screen to the left.
            page.alpha = 0f
        } else if (position <= 0) {    // [-1,0]
            page.alpha = 1f
            page.pivotX = page.width.toFloat()
            page.rotationY = (90 * abs(position.toDouble())).toFloat()
        } else if (position <= 1) {    // (0,1]
            page.alpha = 1f
            page.pivotX = 0f
            page.rotationY = (-90 * abs(position.toDouble())).toFloat()
        } else {    // (1,+Infinity]
            // This page is way off-screen to the right.
            page.alpha = 0f
        }



        if (abs(position.toDouble()) <= 0.5) {
            page.scaleY = max(.4, (1 - abs(position.toDouble())).toDouble())
                .toFloat()
        } else if (abs(position.toDouble()) <= 1) {
            page.scaleY = max(.4, abs(position.toDouble())).toFloat()
        }
    }
}