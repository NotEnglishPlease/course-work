package com.example.helloworldmessenger.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.helloworldmessenger.R
import com.example.helloworldmessenger.databinding.FragmentEditProfileBinding
import com.example.helloworldmessenger.utils.KEY_COLLECTION_USERS
import com.example.helloworldmessenger.utils.KEY_NAME
import com.example.helloworldmessenger.utils.KEY_PROFILE_PICTURE
import com.example.helloworldmessenger.utils.UserManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

/**
 * Edit profile fragment
 * Этот фрагмент используется для редактирования данных профиля.
 * @constructor создает пустой фрагмент
 */
class EditProfileFragment : Fragment() {

    private lateinit var binding: FragmentEditProfileBinding
    private val db = Firebase.firestore
    private lateinit var profilePictureUri: Uri

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
        binding = FragmentEditProfileBinding.inflate(layoutInflater)
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
        binding.editProfileToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        // Загрузка исходных данных
        Glide.with(this)
            .load(UserManager.currentUser.profile_picture)
            .placeholder(R.drawable.baseline_account_circle_24)
            .into(binding.avatarImageView)
        binding.nameEditText.setText(UserManager.currentUser.name)

        // Запрет нажатия на saveButton для устранения лишних сохранений
        binding.saveButton.isEnabled = false

        // Смена аватара
        val pickImage =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    val uri = data?.data
                    profilePictureUri = uri!!
                    binding.avatarImageView.setImageURI(uri)
                    binding.saveButton.isEnabled = true
                }
            }

        binding.editAvatarButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImage.launch(intent)
        }

        // Сохранение новвых данных в firebase
        binding.saveButton.setOnClickListener {
            val userData = db.collection(KEY_COLLECTION_USERS).document(UserManager.currentUser.id)
            val name = binding.nameEditText.text.toString()
            if (!checkName(name)) {
                return@setOnClickListener
            }
            userData.update(KEY_NAME, name)
            if (::profilePictureUri.isInitialized) {
                val storageRef =
                    FirebaseStorage.getInstance().reference
                        .child("avatars/${UserManager.currentUser.id}.jpg")
                // Загрузка файла в хранилище Firebase
                storageRef.putFile(profilePictureUri).addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener {
                        // Update avatar URL in Firestore
                        userData.update(KEY_PROFILE_PICTURE, it.toString())
                    }
                }
            }
            Toast.makeText(activity, R.string.profile_has_updated, Toast.LENGTH_SHORT).show()
            binding.saveButton.isEnabled = false
        }
        binding.nameEditText.addTextChangedListener {
            binding.saveButton.isEnabled = checkName(it.toString())
        }
    }

    /**
     * Check name
     * Этот метод проверяет допустимость введеного имени
     * @param name - введенное имя
     */
    private fun checkName(name: String): Boolean {
        val trimName = name.trim()
        return if (trimName.isEmpty()) {
            binding.textInputLayout.error = getString(R.string.empty_name)
            false
        } else if (trimName.length >= 30) {
            binding.textInputLayout.error = getString(R.string.name_too_long)
            false
        } else {
            binding.textInputLayout.isErrorEnabled = false
            true
        }
    }
}