package com.sublingo.app.translator

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

class MLKitTranslator {

    private var translator: Translator? = null
    private val cache = LinkedHashMap<String, String>(100, 0.75f, true)

    suspend fun initialize(sourceLang: String, targetLang: String): Result<Unit> {
        return try {
            translator?.close()
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLang)
                .setTargetLanguage(targetLang)
                .build()
            translator = Translation.getClient(options)

            val conditions = DownloadConditions.Builder().build()
            translator!!.downloadModelIfNeeded(conditions).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun translate(text: String): String {
        if (text.isBlank()) return text
        cache[text]?.let { return it }

        return try {
            val result = translator?.translate(text)?.await() ?: text
            cache[text] = result
            result
        } catch (e: Exception) {
            text // Return original on error
        }
    }

    fun close() {
        translator?.close()
        translator = null
        cache.clear()
    }

    companion object {
        val LANGUAGES: List<Pair<String, String>> = listOf(
            Pair(TranslateLanguage.AFRIKAANS, "Afrikaans"),
            Pair(TranslateLanguage.ARABIC, "Arabic"),
            Pair(TranslateLanguage.BENGALI, "Bengali"),
            Pair(TranslateLanguage.CHINESE, "Chinese"),
            Pair(TranslateLanguage.DANISH, "Danish"),
            Pair(TranslateLanguage.DUTCH, "Dutch"),
            Pair(TranslateLanguage.ENGLISH, "English"),
            Pair(TranslateLanguage.FRENCH, "French"),
            Pair(TranslateLanguage.GERMAN, "German"),
            Pair(TranslateLanguage.GREEK, "Greek"),
            Pair(TranslateLanguage.GUJARATI, "Gujarati"),
            Pair(TranslateLanguage.HINDI, "Hindi"),
            Pair(TranslateLanguage.INDONESIAN, "Indonesian"),
            Pair(TranslateLanguage.ITALIAN, "Italian"),
            Pair(TranslateLanguage.JAPANESE, "Japanese"),
            Pair(TranslateLanguage.KANNADA, "Kannada"),
            Pair(TranslateLanguage.KOREAN, "Korean"),
            Pair(TranslateLanguage.MALAY, "Malay"),
            Pair(TranslateLanguage.MARATHI, "Marathi"),
            Pair(TranslateLanguage.PERSIAN, "Persian"),
            Pair(TranslateLanguage.POLISH, "Polish"),
            Pair(TranslateLanguage.PORTUGUESE, "Portuguese"),
            Pair(TranslateLanguage.ROMANIAN, "Romanian"),
            Pair(TranslateLanguage.RUSSIAN, "Russian"),
            Pair(TranslateLanguage.SPANISH, "Spanish"),
            Pair(TranslateLanguage.SWEDISH, "Swedish"),
            Pair(TranslateLanguage.TAMIL, "Tamil"),
            Pair(TranslateLanguage.TELUGU, "Telugu"),
            Pair(TranslateLanguage.THAI, "Thai"),
            Pair(TranslateLanguage.TURKISH, "Turkish"),
            Pair(TranslateLanguage.UKRAINIAN, "Ukrainian"),
            Pair(TranslateLanguage.URDU, "Urdu"),
            Pair(TranslateLanguage.VIETNAMESE, "Vietnamese")
        )
    }
}
