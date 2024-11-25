package com.example.kaizenspeaking.ui.home_signed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.kaizenspeaking.R
import com.github.mikephil.charting.data.Entry

class HomeSignedViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext

    val articleTitles: Array<String> = context.resources.getStringArray(R.array.tempdata_article_title)
    val articleDescriptions: Array<String> = context.resources.getStringArray(R.array.tempdata_article_description)
    val articleUrls: Array<String> = context.resources.getStringArray(R.array.tempdata_article_url)
    val articleImages: IntArray = context.resources.obtainTypedArray(R.array.tempdata_article_image).let {
        IntArray(it.length()) { index -> it.getResourceId(index, -1) }
    }
}
