package com.sublingo.app.data.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TranslationDao {
    @Query("SELECT * FROM translation_history ORDER BY createdAt DESC")
    fun getAll(): LiveData<List<TranslationEntity>>

    @Query("SELECT * FROM translation_history ORDER BY createdAt DESC LIMIT 5")
    fun getRecent(): LiveData<List<TranslationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TranslationEntity): Long

    @Delete
    suspend fun delete(entity: TranslationEntity)

    @Query("DELETE FROM translation_history")
    suspend fun deleteAll()
}
