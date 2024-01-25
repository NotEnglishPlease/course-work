package com.example.helloworldmessenger.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "note_text") var noteText: String = ""
)
