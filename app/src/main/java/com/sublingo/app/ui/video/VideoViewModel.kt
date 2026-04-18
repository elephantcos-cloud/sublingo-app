package com.sublingo.app.ui.video

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sublingo.app.data.model.SubtitleLine
import com.sublingo.app.parser.SrtParser
import com.sublingo.app.parser.VttParser
import com.sublingo.app.translator.MLKitTranslator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoViewModel(application: Application) : AndroidViewModel(application) {

    private val translator = MLKitTranslator()
    val subtitleLines = MutableLiveData<List<SubtitleLine>>()
    val currentSubtitle = MutableLiveData<String>("")
    val isTranslatorReady = MutableLiveData(false)

    fun loadSubtitle(content: String, isVtt: Boolean, sourceLang: String, targetLang: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val lines = if (isVtt) VttParser.parse(content) else SrtParser.parse(content)
            subtitleLines.postValue(lines)
            val result = translator.initialize(sourceLang, targetLang)
            isTranslatorReady.postValue(result.isSuccess)
        }
    }

    fun updateSubtitleForPosition(positionMs: Long) {
        val lines = subtitleLines.value ?: return
        val line = lines.find { positionMs in it.startTime..it.endTime }
        if (line != null) {
            if (line.translatedText != null) {
                currentSubtitle.value = line.translatedText
            } else {
                viewModelScope.launch {
                    val translated = translator.translate(line.originalText)
                    line.translatedText = translated
                    withContext(Dispatchers.Main) {
                        currentSubtitle.value = translated
                    }
                }
            }
        } else {
            currentSubtitle.value = ""
        }
    }

    override fun onCleared() {
        super.onCleared()
        translator.close()
    }
}
