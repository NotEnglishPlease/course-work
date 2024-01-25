package com.example.helloworldmessenger.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.helloworldmessenger.R
import com.example.helloworldmessenger.activities.MainActivity
import com.example.helloworldmessenger.databinding.FragmentSignUpBinding
import com.example.helloworldmessenger.models.User
import com.example.helloworldmessenger.utils.KEY_COLLECTION_USERS
import com.example.helloworldmessenger.utils.KEY_EMAIL
import com.example.helloworldmessenger.utils.KEY_FRIENDS
import com.example.helloworldmessenger.utils.KEY_ID
import com.example.helloworldmessenger.utils.KEY_INCOMING_REQUESTS
import com.example.helloworldmessenger.utils.KEY_IS_ONLINE
import com.example.helloworldmessenger.utils.KEY_NAME
import com.example.helloworldmessenger.utils.KEY_OUTGOING_REQUESTS
import com.example.helloworldmessenger.utils.KEY_PROFILE_PICTURE
import com.example.helloworldmessenger.utils.UserManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage


/**
 * Register fragment
 * Этот фрагмент используется для регистрации нового пользователя.
 * @constructor создает пустой фрагмент регистрации
 */
class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding
    private var auth = Firebase.auth
    private var db = Firebase.firestore
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
        binding = FragmentSignUpBinding.inflate(layoutInflater)
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

        setEditTextListeners()

        val pickImage =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    val uri = data?.data
                    profilePictureUri = uri!!
                    binding.chooseAvatarImageView.setImageURI(uri)
                    binding.chooseAvatarTextView.visibility = View.GONE
                }
            }

        binding.chooseAvatarImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImage.launch(intent)
        }

        // Обработка нажатия на кнопку регистрации
        binding.signUpButton.setOnClickListener {
            if (isValidDetails()) { // Если данные корректны
                signUp(
                    name = binding.inputNameEditText.text.toString().trim(),
                    email = binding.inputEmailEditText.text.toString().trim(),
                    password = binding.inputPasswordEditText.text.toString(),
                    profilePicture = profilePictureUri
                ) // Регистрация
            }
        }

        binding.loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_logInFragment)
        }
    }

    /**
     * Sign up
     * Этот метод использует пользовательские данные для регистрации нового пользователя в Firebase.
     * @param name
     * @param email
     * @param password
     * @param profilePicture
     */
    private fun signUp(
        name: String,
        email: String,
        password: String,
        profilePicture: Uri
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val authUser = auth.currentUser
                val user = User(
                    id = authUser!!.uid,
                    name = name,
                    email = email
                )
                UserManager.currentUser = user
                val userMap = hashMapOf(
                    KEY_ID to user.id,
                    KEY_NAME to user.name,
                    KEY_EMAIL to user.email,
                    KEY_INCOMING_REQUESTS to emptyList<String>(),
                    KEY_OUTGOING_REQUESTS to emptyList<String>(),
                    KEY_FRIENDS to emptyList<String>(),
                    KEY_PROFILE_PICTURE to "",
                    KEY_IS_ONLINE to false
                )
                db.collection(KEY_COLLECTION_USERS).document(user.id).set(userMap)

                // Добавление URL-адреса аватара в хранилище аватаров
                // Получение ссылки на файл хранилища в avatars/<FILENAME>
                val storageRef =
                    FirebaseStorage.getInstance().reference.child("avatars/${user.id}.jpg")
                // Загрузка файла в хранилище Firebase
                storageRef.putFile(profilePicture)
                    .addOnSuccessListener {
                        storageRef.downloadUrl
                            .addOnSuccessListener {
                                UserManager.currentUser.profile_picture = it.toString()
                                // Обновление URL-адрес аватара в Firestone
                                db.collection(KEY_COLLECTION_USERS).document(user.id)
                                    .update(KEY_PROFILE_PICTURE, it.toString())
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                            }

                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    }

                // Успешный вход в систему,
                // обновление пользовательского интерфейса информацией о вошедшем пользователе
                Toast.makeText(context, getString(R.string.sign_up_success), Toast.LENGTH_SHORT)
                    .show()
                val intent = Intent(activity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                activity?.startActivity(intent)
            }
            .addOnFailureListener { e ->
                // При неудачном входе в систему, отобразим пользователю сообщение.
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Set edit text listeners
     * Установка слушателей для обработки добавления аватара, ввода почты и пароля
     */
    private fun setEditTextListeners() {
        binding.apply {
            inputNameEditText.addTextChangedListener { name ->
                checkName(name.toString())
            }

            inputEmailEditText.addTextChangedListener { email ->
                checkEmail(email.toString())
            }

            inputPasswordEditText.addTextChangedListener { password ->
                checkPassword(password.toString())
            }

            inputConfirmPasswordEditText.addTextChangedListener { confirmPassword ->
                checkConfirmPassword(confirmPassword.toString())
            }
        }
    }

    /**
     * Check password
     * Этот метод используется для проверки загруженного аватара.
     * @param password
     */
    private fun checkAvatar(): Boolean {
        if (binding.chooseAvatarImageView.drawable == null) { // Если аватар не выбран
            Toast.makeText(context, getString(R.string.avatar_error), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    /**
     * Check password
     * Этот метод используется для проверки корректности повторно введенного пароля.
     * @param password
     */
    private fun checkConfirmPassword(confirmPassword: String): Boolean {
        return if (confirmPassword != binding.inputPasswordEditText.text.toString()) {
            binding.inputConfirmPasswordLayout.error = getString(R.string.confirm_password_error)
            false
        } else {
            binding.inputConfirmPasswordLayout.isErrorEnabled = false
            true
        }
    }

    /**
     * Check password
     * Этот метод используется для проверки корректности введенного пароля.
     * @param password
     */
    private fun checkPassword(password: String): Boolean {
        // Шаблон регулярного выражения для соответствия буквам, цифрам и специальным символам
        val passwordRegex =
            "^[a-zA-Z0-9@#\$%^&+=]+$"

        return when {
            password.trim().isEmpty() -> {
                binding.inputPasswordLayout.error = getString(R.string.password_error)
                false
            }

            password.length < 6 || password.length > 24 -> {
                binding.inputPasswordLayout.error = getString(R.string.password_length_error)
                false
            }

            !password.matches(passwordRegex.toRegex()) -> {
                binding.inputPasswordLayout.error =
                    getString(R.string.password_invalid_characters_error)
                false
            }

            else -> {
                binding.inputPasswordLayout.isErrorEnabled = false
                true
            }
        }
    }

    /**
     * Check email
     * Этот метод используется для проверки корректности введенной почты.
     * @param email
     */
    private fun checkEmail(email: String): Boolean {
        return if (email.trim().isEmpty()) {
            binding.inputEmailLayout.error = getString(R.string.email_error)
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputEmailLayout.error = getString(R.string.invalid_email)
            false
        } else {
            binding.inputEmailLayout.isErrorEnabled = false
            true
        }
    }

    /**
     * Check name
     * Этот метод используется для проверки корректности введенного имени.
     * @param name
     */
    private fun checkName(name: String): Boolean {
        val trimName = name.trim()
        return if (trimName.isEmpty()) {
            binding.inputNameLayout.error = getString(R.string.name_error)
            false
        } else if (trimName.length >= 30) {
            binding.inputNameLayout.error = getString(R.string.name_too_long)
            false
        } else {
            binding.inputNameLayout.isErrorEnabled = false
            true
        }
    }


    /**
     * Is valid details
     * Этот метод проверяет достоверность введенных данных.
     * @return Boolean
     */
    private fun isValidDetails(): Boolean {
        // Проверка аватара
        if (!checkAvatar()) return false

        // check errors
        binding.let {
            if (checkName(it.inputNameEditText.text.toString().trim()) &&
                checkEmail(it.inputEmailEditText.text.toString().trim()) &&
                checkPassword(it.inputPasswordEditText.text.toString()) &&
                checkConfirmPassword(it.inputConfirmPasswordEditText.text.toString())
            ) {
                return true
            }
        }
        return false
    }

}