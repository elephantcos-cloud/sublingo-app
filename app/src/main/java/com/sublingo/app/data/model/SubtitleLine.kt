package com.sublingo.app.data.model

data class SubtitleLine(
    val index: Int,
    val startTime: Long,   // milliseconds
    val endTime: Long,     // milliseconds
    val originalText: String,
    var translatedText: String? = null
)
