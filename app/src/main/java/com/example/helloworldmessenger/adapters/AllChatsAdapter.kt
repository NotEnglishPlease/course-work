package com.example.helloworldmessenger.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.helloworldmessenger.R
import com.example.helloworldmessenger.databinding.ChatItemBinding
import com.example.helloworldmessenger.models.Conversation
import com.example.helloworldmessenger.utils.KEY_COLLECTION_MESSAGES
import com.example.helloworldmessenger.utils.KEY_COLLECTION_USERS
import com.example.helloworldmessenger.utils.KEY_CONVERSATION_ID
import com.example.helloworldmessenger.utils.KEY_IS_READ
import com.example.helloworldmessenger.utils.KEY_NAME
import com.example.helloworldmessenger.utils.KEY_PROFILE_PICTURE
import com.example.helloworldmessenger.utils.KEY_SENDER_ID
import com.example.helloworldmessenger.utils.KEY_TEXT
import com.example.helloworldmessenger.utils.KEY_TIMESTAMP
import com.example.helloworldmessenger.utils.UserManager
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * All chats adapter
 * Этот адаптер используется для отображения списка всех чатов.
 * @property context - контекст, нужен для получения ресурсов
 * @property options - опции для FirestoreRecyclerAdapter, в котором хранятся данные
 * @property onItemClick - лямбда-выражение, которое вызывается при нажатии на элемент списка
 * @constructor Создает пустой адаптер чатов
 */
class AllChatsAdapter(
    private val context: Context,
    options: FirestoreRecyclerOptions<Conversation>,
    private val onItemClick: (String) -> Unit
) :
    FirestoreRecyclerAdapter<Conversation, AllChatsAdapter.ChatViewHolder>(options) {

    // Лямбда-выражение, которое вызывается при изменении данных
    private var onDataChangedListener: (() -> Unit)? = null

    /**
     * Set on data changed listener
     * Этот метод используется для установки слушателя изменения данных
     * @param listener - лямбда-выражение, которое вызывается при изменении данных
     */
    fun setOnDataChangedListener(listener: () -> Unit) {
        onDataChangedListener = listener
    }

    /**
     * On create view holder
     * Этот метод вызывается при создании ViewHolder'а, в нем создается View
     * @param parent - родительский ViewGroup
     * @param viewType - тип View
     * @return возвращает созданный ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    /**
     * On bind view holder
     * Этот метод вызывается при привязке ViewHolder'а к определенной позиции.
     * В нем происходит привязка данных к View
     * @param holder - ViewHolder, к которому привязываются данные
     * @param position - позиция, к которой привязываются данные
     * @param model - модель, из которой берутся данные
     */
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int, model: Conversation) {
        holder.bind(model)
    }

    /**
     * On data changed
     * Этот метод вызывается при изменении данных
     */
    override fun onDataChanged() {
        super.onDataChanged()
        onDataChangedListener?.let { it() }
    }

    /**
     * Chat view holder
     * Этот класс используется для хранения View элемента списка
     * @constructor
     * @param binding - привязка к View
     */
    inner class ChatViewHolder(private val binding: ChatItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Слушатель изменения последнего сообщения
        private var lastMessageListener: ListenerRegistration? = null

        /**
         * Bind
         * Этот метод используется для привязки данных к View
         * @param conversation - модель, из которой берутся данные
         */
        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(conversation: Conversation) {
            // Получение id собеседника
            val userId = conversation.participants.filter { it != UserManager.currentUser.id }[0]
            // Получаем ссылку на пользователя
            val userRef = Firebase.firestore.collection(KEY_COLLECTION_USERS).document(userId)
            // Получаем имя и аватарку пользователя
            userRef.get().addOnSuccessListener { userSnapshot ->
                // Получаем имя
                val chatName = userSnapshot.getString(KEY_NAME).orEmpty()
                // Получаем аватарку
                val profilePicture = userSnapshot.getString(KEY_PROFILE_PICTURE).orEmpty()
                // Устанавливаем имя и аватарку
                binding.usernameTextView.text = chatName
                Glide.with(itemView.context)
                    .load(profilePicture)
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .into(binding.chatAvatar)
            }

            // Устанавливаем слушатель изменения последнего сообщения
            lastMessageListener = Firebase.firestore.collection(KEY_COLLECTION_MESSAGES)
                .whereEqualTo(KEY_CONVERSATION_ID, conversation.id)
                .addSnapshotListener { querySnapshot, error ->
                    if (error != null) {
                        // Если произошла ошибка
                        return@addSnapshotListener
                    }

                    // Получаем последнее сообщение
                    val lastMessageDoc = querySnapshot?.documents?.lastOrNull()
                    // Если сообщение есть
                    if (lastMessageDoc != null && lastMessageDoc.exists()) {
                        // Получаем текст сообщения
                        val lastMessageText = lastMessageDoc.getString(KEY_TEXT)
                        // Проверяем, отправитель это текущий пользователь или нет
                        val isCurrentUserSender =
                            lastMessageDoc.getString(KEY_SENDER_ID) == UserManager.currentUser.id

                        // Если сообщение отправил текущий пользователь
                        binding.lastMessage.text = if (isCurrentUserSender) {
                            // Добавляем текст "Вы: " перед сообщением
                            context.getString(R.string.outgoing_last_message) + " " + lastMessageText
                        } else {
                            // Иначе просто возвращаем текст сообщения
                            lastMessageText
                        }

                        // Получаем время последнего сообщения

                        // Форматируем время
                        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val lastMessageTime = lastMessageDoc.getTimestamp(KEY_TIMESTAMP)
                            ?.toDate()?.let { formatter.format(it) } ?: "12:00"
                        // Устанавливаем время
                        binding.lastMessageTime.text = lastMessageTime

                        // Получаем статус сообщения
                        val isRead = lastMessageDoc.getBoolean(KEY_IS_READ)
                        // Устанавливаем иконку в соответствии с полученным статусом
                        val iconResId = if (isRead == true) {
                            R.drawable.baseline_check_24
                        } else {
                            R.drawable.baseline_circle_24
                        }
                        val icon = context.getDrawable(iconResId)
                        // Устанавливаем размер иконки в соответствии с полученным статусом
                        val iconSize = if (isRead == true) 40 else 20
                        binding.isReadIcon.layoutParams.width = iconSize
                        binding.isReadIcon.layoutParams.height = iconSize
                        binding.isReadIcon.requestLayout()
                        binding.isReadIcon.setImageDrawable(icon)
                    }
                }

            itemView.setOnClickListener {
                onItemClick(conversation.id)
            }
        }

        /**
         * Remove listener
         * Этот метод удаляет слушатель изменения последнего сообщения
         */
        fun removeListener() {
            lastMessageListener?.remove()
        }
    }

    /**
     * On view recycled
     * Этот метод вызывается, когда объект представления был переработан.
     * @param holder - ViewHolder, объект, данные которого будут очищены
     */
    override fun onViewRecycled(holder: ChatViewHolder) {
        holder.removeListener()
        super.onViewRecycled(holder)
    }
}

