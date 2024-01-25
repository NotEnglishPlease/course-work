package com.example.helloworldmessenger.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.helloworldmessenger.R
import com.example.helloworldmessenger.activities.EntranceActivity
import com.example.helloworldmessenger.databinding.FragmentAccountBinding
import com.example.helloworldmessenger.models.User
import com.example.helloworldmessenger.utils.KEY_COLLECTION_USERS
import com.example.helloworldmessenger.utils.UserManager
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Account fragment
 * Этот фрагмент используется для отображения данных текущего пользователя.
 * @constructor создает пустой фрагмент аккаунта
 */
class AccountFragment : Fragment() {

    private lateinit var binding: FragmentAccountBinding
    private var db = Firebase.firestore
    private lateinit var currentUserListener: ListenerRegistration

    /**
     * On create view
     * Этот метод устанавливает представление фрагмента
     * @param inflater - объект, который раздувает все элементы view на фрагменте
     * @param savedInstanceState - объект, необходимый для сохранения состояний
     * @return возвращает созданное представление
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(layoutInflater)
        return binding.root
    }

    /**
     * On view created
     * Этот метод вызывается сразу после установки представления
     * @param view - представление полученное из метода onCreateView
     * @param savedInstanceState - объект, необходимый для сохранения состояний
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("AccountFragment", "URL: ${UserManager.currentUser.profile_picture}")

        updateUserUi(UserManager.currentUser)
        setupSnapshotListener()
        setListeners()
    }

    /**
     * Set listeners
     * Этот метод обрабатывает нажатия на элементы фрагмента
     */
    private fun setListeners() {
        binding.logOutButton.setOnClickListener {
            val sharedPreferences =
                requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
            val intent = Intent(activity, EntranceActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity?.startActivity(intent)
        }

        binding.editAccountButton.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_editProfileFragment)
        }

        binding.notesButton.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_myNoteFragment)
        }
    }

    /**
     * Setup snapshot listener
     * Этот метод устанавливает слушатель моментальных изменений
     */
    private fun setupSnapshotListener() {
        val currentUserRef =
            db.collection(KEY_COLLECTION_USERS).document(UserManager.currentUser.id)

        currentUserListener = currentUserRef.addSnapshotListener { userSnapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }

            if (userSnapshot != null && userSnapshot.exists()) {
                val user = userSnapshot.toObject(User::class.java)
                if (user != null) {
                    updateUserUi(user)
                }
            }
        }
    }

    /**
     * Update user ui
     * Этот метод обновляет данные пользователя
     * @param user - модель, из которой берутся данные
     */
    private fun updateUserUi(user: User) {
        Glide.with(this)
            .load(user.profile_picture)
            .placeholder(R.drawable.baseline_account_circle_24)
            .into(binding.avatarImageView)

        binding.nameTextView.text = user.name
    }

    /**
     * On destroy view
     * Этот метод удаляет слушатель изменения последнего сообщения
     */
    override fun onDestroyView() {
        currentUserListener.remove()
        super.onDestroyView()
    }
}