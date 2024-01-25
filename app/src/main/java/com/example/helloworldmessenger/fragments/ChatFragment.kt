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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.helloworldmessenger.R
import com.example.helloworldmessenger.adapters.MessageAdapter
import com.example.helloworldmessenger.databinding.FragmentChatBinding
import com.example.helloworldmessenger.models.Message
import com.example.helloworldmessenger.utils.KEY_COLLECTION_CONVERSATIONS
import com.example.helloworldmessenger.utils.KEY_COLLECTION_MESSAGES
import com.example.helloworldmessenger.utils.KEY_COLLECTION_USERS
import com.example.helloworldmessenger.utils.KEY_CONVERSATION_ID
import com.example.helloworldmessenger.utils.KEY_ID
import com.example.helloworldmessenger.utils.KEY_IS_ONLINE
import com.example.helloworldmessenger.utils.KEY_IS_READ
import com.example.helloworldmessenger.utils.KEY_NAME
import com.example.helloworldmessenger.utils.KEY_PARTICIPANTS
import com.example.helloworldmessenger.utils.KEY_PROFILE_PICTURE
import com.example.helloworldmessenger.utils.KEY_SENDER_ID
import com.example.helloworldmessenger.utils.KEY_TEXT
import com.example.helloworldmessenger.utils.KEY_TIMESTAMP
import com.example.helloworldmessenger.utils.UserManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Chat fragment
 * Этот фрагмент используется для отображения локального чата.
 * @constructor создает пустой фрагмент чата
 */
class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private val db = Firebase.firestore
    private val args: ChatFragmentArgs by navArgs()
    private lateinit var adapter: MessageAdapter
    private lateinit var companionListener: ListenerRegistration

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
        binding = FragmentChatBinding.inflate(layoutInflater)
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
        binding.chatToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        setCompanionInfo()
        initRecyclerView()
        setListeners()
        setupKeyboardVisibilityListener()
    }

    /**
     * Set companion info
     * Этот метод устанавливает данные собеседника
     */
    private fun setCompanionInfo() {
        db.collection(KEY_COLLECTION_CONVERSATIONS).document(args.conversationId)
            .get().addOnSuccessListener { conversationSnapshot ->
                val participants = conversationSnapshot.data!![KEY_PARTICIPANTS] as List<String>
                val companionId = participants.filter { it != UserManager.currentUser.id }[0]
                val companionDocRef = db.collection(KEY_COLLECTION_USERS).document(companionId)

                companionListener = companionDocRef.addSnapshotListener { companionSnapshot, e ->
                    if (e != null) {
                        // Handle the error
                        return@addSnapshotListener
                    }

                    if (companionSnapshot != null && companionSnapshot.exists()) {
                        Glide.with(this)
                            .load(companionSnapshot.getString(KEY_PROFILE_PICTURE))
                            .placeholder(R.drawable.baseline_account_circle_24)
                            .into(binding.profilePictureImageView)

                        binding.usernameTextView.text = companionSnapshot.getString(KEY_NAME)
                        val isOnline = companionSnapshot.getBoolean(KEY_IS_ONLINE) == true
                        binding.lastOnlineTextView.text = if (isOnline)
                            getString(R.string.online_status)
                        else
                            getString(R.string.offline_status)
                    }
                }
            }
    }

    /**
     * Init recycler view
     * Этот метод инициализирует представление RecyclerView, для отображения сообщений.
     */
    private fun initRecyclerView() {
        val query = db.collection(KEY_COLLECTION_MESSAGES)
            .whereEqualTo(KEY_CONVERSATION_ID, args.conversationId)

        val options = FirestoreRecyclerOptions.Builder<Message>()
            .setQuery(query) {
                val newMessage = it.toObject(Message::class.java)!!
                newMessage.is_read = it.getBoolean(KEY_IS_READ) ?: false
                newMessage
            }
            .build()


        adapter = MessageAdapter(requireContext(), options)
        adapter.startListening()
        // scroll to the bottom of the recycler view automatically
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.messageRecyclerView.smoothScrollToPosition(adapter.itemCount)
            }
        })
        binding.messageRecyclerView.adapter = adapter
        binding.messageRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    /**
     * Set listeners
     * Этот метод обрабатывает нажатия на элементы фрагмента
     */
    private fun setListeners() {
        binding.sendButton.setOnClickListener { sendMessage() }
    }

    /**
     * Send message
     * Этот метод реализует отправку сообщения в чате
     */
    private fun sendMessage() {
        val messageText = binding.messageEditText.text?.toString()?.trim()
        if (messageText.isNullOrEmpty()) return
        if (messageText.length >= 300) {
            Toast.makeText(context, getString(R.string.message_is_too_long), Toast.LENGTH_SHORT)
                .show()
            return
        }
        val timestamp = Timestamp.now()
        val senderId = UserManager.currentUser.id
        val messageMap = hashMapOf(
            KEY_ID to "$timestamp$senderId",
            KEY_CONVERSATION_ID to args.conversationId,
            KEY_SENDER_ID to senderId,
            KEY_TEXT to messageText,
            KEY_TIMESTAMP to timestamp,
            KEY_IS_READ to false
        )
        db.collection(KEY_COLLECTION_MESSAGES)
            .document(messageMap[KEY_ID] as String)
            .set(messageMap)
            .addOnSuccessListener {
                binding.messageEditText.text?.clear()
            }
            .addOnFailureListener {
                // Обработка ошибки отправки сообщения
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
                    binding.messageRecyclerView.post {
                        val itemCount = adapter.itemCount
                        if (itemCount > 0) {
                            binding.messageRecyclerView.scrollToPosition(itemCount - 1)
                        }
                    }
                }
            }
        })
    }

    /**
     * On destroy view
     * Этот метод вызывается при уничтожении представления
     */
    override fun onDestroyView() {
        super.onDestroyView()
        // Удаление мгновенного слушателя после уничтожения view
        companionListener.remove()
    }
}
