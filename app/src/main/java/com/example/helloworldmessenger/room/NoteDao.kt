package com.example.helloworldmessenger.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    // Получить все заметки
    @Query("SELECT * FROM notes")
    fun getAll(): Flow<List<Note>>

    // Получить заметку по id
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: String): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(note: Note)

    // Удалить заметку
    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: String)
}