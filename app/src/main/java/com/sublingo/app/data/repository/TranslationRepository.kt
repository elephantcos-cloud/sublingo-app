package com.sublingo.app.data.repository

import android.content.Context
import android.net.Uri
import com.sublingo.app.data.db.AppDatabase
import com.sublingo.app.data.db.TranslationEntity
import com.sublingo.app.data.model.FileType
import com.sublingo.app.data.model.SubtitleLine
import com.sublingo.app.data.model.TranslationJob
import com.sublingo.app.parser.SrtParser
import com.sublingo.app.parser.VttParser
import com.sublingo.app.translator.MLKitTranslator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

sealed class TranslationState {
    object Idle : TranslationState()
    data class Parsing(val progress: Float) : TranslationState()
    data class Translating(
        val progress: Float,
        val current: Int,
        val total: Int,
        val currentLine: String,
        val translatedLine: String
    ) : TranslationState()
    data class Saving(val progress: Float) : TranslationState()
    data class Completed(val outputPath: String, val lineCount: Int) : TranslationState()
    data class Error(val message: String) : TranslationState()
}

class TranslationRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val translator = MLKitTranslator()

    val history = db.translationDao().getAll()
    val recentHistory = db.translationDao().getRecent()

    fun translate(job: TranslationJob): Flow<TranslationState> = flow {
        emit(TranslationState.Parsing(0f))

        // 1. Read file
        val content = try {
            context.contentResolver.openInputStream(Uri.parse(job.fileUri))
                ?.bufferedReader()?.readText() ?: run {
                emit(TranslationState.Error("Cannot read file"))
                return@flow
            }
        } catch (e: Exception) {
            emit(TranslationState.Error("File read error: ${e.message}"))
            return@flow
        }

        emit(TranslationState.Parsing(0.5f))

        // 2. Parse file
        val lines: List<SubtitleLine> = when (job.fileType) {
            FileType.SRT -> SrtParser.parse(content)
            FileType.VTT -> VttParser.parse(content)
        }

        if (lines.isEmpty()) {
            emit(TranslationState.Error("No subtitle lines found"))
            return@flow
        }

        emit(TranslationState.Parsing(1f))

        // 3. Initialize translator
        val initResult = translator.initialize(job.sourceLang, job.targetLang)
        if (initResult.isFailure) {
            emit(TranslationState.Error("Model download failed: ${initResult.exceptionOrNull()?.message}"))
            return@flow
        }

        // 4. Translate each line
        lines.forEachIndexed { index, line ->
            val translated = translator.translate(line.originalText)
            line.translatedText = translated

            val progress = (index + 1).toFloat() / lines.size
            emit(TranslationState.Translating(
                progress = progress,
                current = index + 1,
                total = lines.size,
                currentLine = line.originalText.take(60),
                translatedLine = translated.take(60)
            ))
        }

        emit(TranslationState.Saving(0.3f))

        // 5. Generate output
        val outputContent = when (job.fileType) {
            FileType.SRT -> SrtParser.generate(lines)
            FileType.VTT -> VttParser.generate(lines)
        }

        emit(TranslationState.Saving(0.7f))

        // 6. Save file
        val outputName = job.fileName.replaceBeforeLast(".", "").let {
            job.fileName.removeSuffix(it) + "_${job.targetLang}" + it
        }
        val outputDir = File(context.getExternalFilesDir(null), "SubLingo")
        outputDir.mkdirs()
        val outputFile = File(outputDir, outputName)
        outputFile.writeText(outputContent)

        emit(TranslationState.Saving(1f))

        // 7. Save to history
        db.translationDao().insert(
            TranslationEntity(
                fileName = job.fileName,
                sourceLang = job.sourceLang,
                targetLang = job.targetLang,
                lineCount = lines.size,
                outputPath = outputFile.absolutePath
            )
        )

        translator.close()
        emit(TranslationState.Completed(outputFile.absolutePath, lines.size))
    }

    suspend fun deleteHistory(entity: TranslationEntity) {
        db.translationDao().delete(entity)
    }

    suspend fun clearHistory() {
        db.translationDao().deleteAll()
    }
}
