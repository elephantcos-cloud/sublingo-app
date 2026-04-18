package com.sublingo.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translation_history")
data class TranslationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val sourceLang: String,
    val targetLang: String,
    val lineCount: Int,
    val outputPath: String,
    val createdAt: Long = System.currentTimeMillis()
)
