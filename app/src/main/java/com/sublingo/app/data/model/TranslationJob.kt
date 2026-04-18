package com.sublingo.app.data.model

data class TranslationJob(
    val fileUri: String,
    val fileName: String,
    val fileType: FileType,
    val sourceLang: String,
    val targetLang: String
)

enum class FileType { SRT, VTT }
