package com.sublingo.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.sublingo.app.data.repository.TranslationRepository

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = TranslationRepository(application)
    val recentHistory = repo.recentHistory
}
