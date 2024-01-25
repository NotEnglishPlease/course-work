package com.example.helloworldmessenger

import android.app.Application
import androidx.room.Room
import com.example.helloworldmessenger.room.NoteDatabase

class MyApplication : Application() {
    private val noteDatabase by lazy {
        Room.databaseBuilder(
            this,
            NoteDatabase::class.java,
            "notes"
        ).build()
    }
    val noteDao by lazy {
        noteDatabase.noteDao()
    }
}