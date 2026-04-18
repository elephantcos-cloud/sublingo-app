package com.sublingo.app.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sublingo.app.data.db.TranslationEntity
import com.sublingo.app.data.repository.TranslationRepository
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = TranslationRepository(application)
    val history = repo.history

    fun delete(entity: TranslationEntity) = viewModelScope.launch { repo.deleteHistory(entity) }
    fun clearAll() = viewModelScope.launch { repo.clearHistory() }
}
