package com.example.helloworldmessenger.fragments

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.helloworldmessenger.R
import com.example.helloworldmessenger.adapters.GlobalMessageAdapter
import com.example.helloworldmessenger.databinding.FragmentGlobalBinding
import com.example.helloworldmessenger.models.Message
import com.example.helloworldmessenger.models.User
import com.example.helloworldmessenger.utils.ADMIN_ID
import com.example.helloworldmessenger.utils.KEY_COLLECTION_GLOBAL_MESSAGES
import com.example.helloworldmessenger.utils.KEY_ID
import com.example.helloworldmessenger.utils.KEY_SENDER_ID
import com.example.helloworldmessenger.utils.KEY_TEXT
import com.example.helloworldmessenger.utils.KEY_TIMESTAMP
import com.example.helloworldmessenger.utils.UserManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Global fragment
 * Этот фрагмент используется для отображения глобального чата.
 * @constructor создает пустой фрагмент глобального чата
 */
class GlobalFragment : Fragment() {

    private lateinit var binding: FragmentGlobalBinding
    private lateinit var adapter: GlobalMessageAdapter
    private val db = Firebase.firestore

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
        binding = FragmentGlobalBinding.inflate(layoutInflater)
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
        initRecyclerView()
        binding.sendButton.setOnClickListener {
            sendMessage()
        }
        setupKeyboardVisibilityListener()
    }

    /**
     * Send message
     * Этот метод реализует отправку сообщения в чате
     */
    private fun sendMessage() {
        val messageText = binding.messageEditText.text.toString().trim()
        if (messageText.isEmpty()) return
        if (messageText.length >= 300) {
            Toast.makeText(context, getString(R.string.message_is_too_long), Toast.LENGTH_SHORT)
                .show()
            return
        }
        val timestamp = Timestamp.now()
        val senderId = UserManager.currentUser.id
        val messageMap = hashMapOf(
            KEY_ID to "$timestamp$senderId",
            KEY_SENDER_ID to senderId,
            KEY_TEXT to messageText,
            KEY_TIMESTAMP to timestamp,
        )
        db.collection(KEY_COLLECTION_GLOBAL_MESSAGES)
            .document(messageMap[KEY_ID] as String)
            .set(messageMap)
            .addOnSuccessListener {
                binding.messageEditText.text?.clear()
            }
            .addOnFailureListener {
                // Handle the failure to send the message
            }
    }

    /**
     * Setup keyboard visibility listener
     * Этот метод изменяет положение сообщений в зависимоти от видимости клавиатуры
     */
    private fun setupKeyboardVisibilityListener() {
        val rootView = requireActivity().findViewById<ViewGroup>(android.R.id.content)
        rootView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            private val windowVisibleDisplayFrame = Rect()

            /**
             * On global layout
             * Этот метод используется для взаимодействия с интерфейсом
             */
            override fun onGlobalLayout() {
                rootView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrame)
                val heightDiff = rootView.rootView.height - windowVisibleDisplayFrame.bottom
                val isKeyboardVisible = heightDiff > rootView.rootView.height * 0.15

                if (isKeyboardVisible) {
                    binding.globalRecyclerView.post {
                        val itemCount = adapter.itemCount
                        if (itemCount > 0) {
                            binding.globalRecyclerView.scrollToPosition(itemCount - 1)
                        }
                    }
                }
            }
        })
    }

    /**
     * Init recycler view
     * Этот метод инициализирует представление RecyclerView, для отображения сообщений.
     */
    private fun initRecyclerView() {
        val query = db.collection(KEY_COLLECTION_GLOBAL_MESSAGES)

        val options = FirestoreRecyclerOptions.Builder<Message>()
            .setQuery(query, Message::class.java)
            .build()

        adapter = GlobalMessageAdapter(requireContext(), options) { user ->
            showUserCard(user)
        }

        adapter.startListening()
        // scroll to the bottom of the recycler view automatically
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.globalRecyclerView.smoothScrollToPosition(adapter.itemCount)
            }
        })

        if (ADMIN_ID.contains(UserManager.currentUser.id)) {
            adapter.setAdminClickListener { message ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(resources.getString(R.string.admin_rules))
                    .setMessage(resources.getString(R.string.delete_message_admin))
                    .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                        db.collection(KEY_COLLECTION_GLOBAL_MESSAGES)
                            .document(message.id)
                            .delete()
                    }
                    .setNegativeButton(resources.getString(R.string.no)) { _, _ -> }
                    .show()
            }
        }
        binding.globalRecyclerView.adapter = adapter
        binding.globalRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    /**
     * Show user card
     * Этот метод осуществляет переход на профиль пользователя
     * @param user - модель, из которой берутся данные
     */
    private fun showUserCard(user: User) {
        val action = GlobalFragmentDirections.actionGlobalFragmentToUserProfileFragment(
            name = user.name,
            id = user.id,
            profilePicture = user.profile_picture,
            isOnline = user.is_online
        )

        findNavController().navigate(action)
    }
}