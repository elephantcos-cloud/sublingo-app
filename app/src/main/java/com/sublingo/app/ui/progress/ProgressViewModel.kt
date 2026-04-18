package com.sublingo.app.ui.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sublingo.app.data.model.FileType
import com.sublingo.app.data.model.TranslationJob
import com.sublingo.app.data.repository.TranslationRepository
import com.sublingo.app.data.repository.TranslationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProgressViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = TranslationRepository(application)
    private val _state = MutableStateFlow<TranslationState>(TranslationState.Idle)
    val state: StateFlow<TranslationState> = _state

    fun startTranslation(fileUri: String, fileName: String, fileType: String, sourceLang: String, targetLang: String) {
        val job = TranslationJob(
            fileUri = fileUri,
            fileName = fileName,
            fileType = if (fileType == "VTT") FileType.VTT else FileType.SRT,
            sourceLang = sourceLang,
            targetLang = targetLang
        )
        viewModelScope.launch {
            repo.translate(job).collect { state ->
                _state.value = state
            }
        }
    }
}
