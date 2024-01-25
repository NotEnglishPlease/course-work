package com.example.helloworldmessenger.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.helloworldmessenger.R
import com.example.helloworldmessenger.models.User
import com.example.helloworldmessenger.utils.KEY_COLLECTION_USERS
import com.example.helloworldmessenger.utils.KEY_EMAIL
import com.example.helloworldmessenger.utils.KEY_ID
import com.example.helloworldmessenger.utils.KEY_NAME
import com.example.helloworldmessenger.utils.KEY_PROFILE_PICTURE
import com.example.helloworldmessenger.utils.UserManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Splash activity
 * В данной activity происходит проверка, авторизован ли пользователь.
 * @constructor Create empty Splash activity
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        checkLogIn()
    }

    /**
     * Check log in
     * Проверяет, авторизован ли пользователь.
     * Если да, то переходит в MainActivity. Если нет, то в EnteranceActivity.
     */
    private fun checkLogIn() {
        // Проверяем, авторизован ли пользователь
        // Для этого проверяем значение isLoggedIn в SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        // Если пользователь авторизован, то получаем его данные из Firestore
        if (isLoggedIn) {
            // Получаем ссылку на коллекцию пользователей
            val usersRef = Firebase.firestore.collection(KEY_COLLECTION_USERS)

            // Создаем корутину, которая получает данные пользователя из Firestore
            lifecycleScope.launch {
                try {
                    // Получаем данные пользователя из Firestore
                    val document = withContext(Dispatchers.IO) {
                        usersRef.whereEqualTo(KEY_ID, Firebase.auth.currentUser?.uid).get().await()
                    }

                    // Если данные получены, то сохраняем их в UserManager
                    if (document != null) {
                        for (doc in document) {
                            UserManager.currentUser = User(
                                id = doc.data[KEY_ID] as String,
                                email = doc.data[KEY_EMAIL] as String,
                                name = doc.data[KEY_NAME] as String,
                                profile_picture = doc.data[KEY_PROFILE_PICTURE] as String
                            )
                        }
                        // Переходим в MainActivity
                        val intent = Intent(this@SplashActivity, MainActivity::class.java)
                        // Не позволяем пользователю вернуться на экран авторизации
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                } catch (e: Exception) {
                    // Если произошла ошибка, то переходим в EntranceActivity
                    val intent = Intent(this@SplashActivity, EntranceActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        } else {
            // Если пользователь не авторизован, то переходим в EntranceActivity
            val intent = Intent(this, EntranceActivity::class.java)
            // Не позволяем пользователю вернуться на экран авторизации
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}