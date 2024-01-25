package com.example.helloworldmessenger.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.helloworldmessenger.R

/**
 * Entrance activity
 * Данная activity является входной точкой приложения.
 * Здесь происходит авторизация пользователя. Отдельня activity нужна для того, чтобы
 * обновлять статус пользователя в MainActivity.
 * @constructor Create empty Entrance activity
 */
class EntranceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entrance)
    }
}