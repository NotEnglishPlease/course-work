package com.example.helloworldmessenger.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.helloworldmessenger.R
import com.example.helloworldmessenger.utils.KEY_COLLECTION_USERS
import com.example.helloworldmessenger.utils.KEY_IS_ONLINE
import com.example.helloworldmessenger.utils.UserManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Main activity
 * Это главная activity приложения. Здесь происходит навигация между фрагментами.
 * @constructor Create empty Main activity
 */
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController // отвечает за навигацию между фрагментами
    private val db = Firebase.firestore // отвечает за работу с базой данных
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        // Находим NavHostFragment и получаем NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController
        // Управляем видимостью BottomNavigationView
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                // В этих фрагментах мы хотим видеть BottomNavigationView
                R.id.allChatsFragment, R.id.globalFragment, R.id.accountFragment, R.id.friendsFragment -> {
                    bottomNavigationView.visibility = View.VISIBLE
                }

                R.id.userProfileFragment -> {
                    // Если мы переходим на фрагмент профиля пользователя, то не изменяем видимость
                    Log.d("BottomNav", "onCreate: skipping bottom navigation view")
                }

                // В остальных случаях скрываем BottomNavigationView
                else -> bottomNavigationView.visibility = View.GONE
            }
        }
        // Устанавливаем BottomNavigationView для NavController
        bottomNavigationView.setupWithNavController(navController)
    }


    /**
     * OnResume
     * При возвращении в приложение, обновляем статус пользователя на "онлайн"
     */
    override fun onResume() {
        super.onResume()
        updateStatus(true)
    }

    /**
     * OnStop
     * При выходе из приложения, обновляем статус пользователя на "оффлайн"
     */
    override fun onStop() {
        updateStatus(false)
        super.onStop()
    }

    /**
     * Update status
     * Обновляет статус пользователя в базе данных
     * @param status
     */
    private fun updateStatus(status: Boolean) {
        UserManager.currentUser.is_online = status
        db.collection(KEY_COLLECTION_USERS).document(UserManager.currentUser.id).update(
            KEY_IS_ONLINE, status
        )
    }
}