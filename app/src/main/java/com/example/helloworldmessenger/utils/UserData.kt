package com.example.helloworldmessenger.utils

import com.example.helloworldmessenger.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * User manager
 * This class is used to manage the current user data.
 * @constructor Create empty User manager
 */
object UserManager {
    var currentUser: User = User()

    init {
        listen()
    }

    private fun listen() {
        Firebase.firestore
            .collection(KEY_COLLECTION_USERS)
            .document(Firebase.auth.uid.toString())
            .addSnapshotListener { userSnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (userSnapshot != null && userSnapshot.exists()) {
                    currentUser = userSnapshot.toObject(User::class.java)!!
                }
            }
    }
}



