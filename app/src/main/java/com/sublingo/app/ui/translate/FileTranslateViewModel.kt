package com.sublingo.app.ui.translate

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sublingo.app.data.model.FileType
import com.sublingo.app.translator.MLKitTranslator

class FileTranslateViewModel : ViewModel() {
    val selectedUri = MutableLiveData<Uri?>()
    val selectedFileName = MutableLiveData<String>()
    val selectedFileType = MutableLiveData<FileType>()
    val sourceLang = MutableLiveData(MLKitTranslator.LANGUAGES.first { it.first == "en" })
    val targetLang = MutableLiveData(MLKitTranslator.LANGUAGES.first { it.first == "bn" })
    val isReady get() = selectedUri.value != null

    fun swapLanguages() {
        val tmp = sourceLang.value
        sourceLang.value = targetLang.value
        targetLang.value = tmp
    }
}
