package com.example.helloworldmessenger.adapters

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.helloworldmessenger.R
import com.example.helloworldmessenger.databinding.GlobalMessageItemBinding
import com.example.helloworldmessenger.models.Message
import com.example.helloworldmessenger.models.User
import com.example.helloworldmessenger.utils.KEY_COLLECTION_USERS
import com.example.helloworldmessenger.utils.UserManager
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Global message adapter
 * Этот адаптер используется для отображения сообщений в глобальном чате.
 * @property context - контекст, нужен для получения ресурсов
 * @property options - опции для FirestoreRecyclerAdapter, в котором хранятся данные
 * @property onClick - лямбда-выражение, которое вызывается при нажатии на сообщение
 * @constructor Создает пустое сообщение
 */
class GlobalMessageAdapter(
    private val context: Context,
    options: FirestoreRecyclerOptions<Message>,
    private val onClick: (User) -> Unit,
) : FirestoreRecyclerAdapter<Message, GlobalMessageAdapter.GlobalMessageViewHolder>(options) {

    private var onItemClick: ((Message) -> Unit)? = null

    fun setAdminClickListener(action: (Message) -> Unit) {
        onItemClick = action
    }

    // Переменная, отвечающая за отступ сообщения
    private val marginDp = 4

    // Переменные, отвечающие за цвет фона и текста отправленного сообщения
    private var myMessageCardColor: Int
    private var myMessageTextColor: Int

    // Переменные, отвечающие за цвет фона и текста полученного сообщения
    private var notMyMessageCardColor: Int
    private var notMyMessageTextColor: Int

    // Инициализация цветов из ресурсов приложения
    init {
        context.theme.apply {
            val typedValue = TypedValue()
            resolveAttribute(
                com.google.android.material.R.attr.colorPrimaryContainer,
                typedValue,
                true
            )
            myMessageCardColor = typedValue.data
            resolveAttribute(
                com.google.android.material.R.attr.colorOnPrimaryContainer,
                typedValue,
                true
            )
            myMessageTextColor = typedValue.data
            resolveAttribute(
                com.google.android.material.R.attr.colorSecondaryContainer,
                typedValue,
                true
            )
            notMyMessageCardColor = typedValue.data
            resolveAttribute(
                com.google.android.material.R.attr.colorOnSecondaryContainer,
                typedValue,
                true
            )
            notMyMessageTextColor = typedValue.data
        }
    }

    /**
     * On create view holder
     * Этот метод вызывается при создании ViewHolder'а, в нем создается View
     * @param parent - родительский ViewGroup
     * @param viewType - тип View
     * @return возвращает созданный ViewHolder
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GlobalMessageAdapter.GlobalMessageViewHolder {
        val viewHolder = GlobalMessageViewHolder(
            GlobalMessageItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        onItemClick?.let { onItemClick ->
            viewHolder.itemView.setOnClickListener {
                val position = viewHolder.bindingAdapterPosition
                onItemClick(getItem(position))
            }
        }
        return viewHolder
    }

    /**
     * On bind view holder
     * Этот метод вызывается при привязке ViewHolder'а к определенной позиции.
     * В нем происходит привязка данных к View
     * @param holder - ViewHolder, к которому привязываются данные
     * @param position - позиция, к которой привязываются данные
     * @param model - модель, из которой берутся данные
     */
    override fun onBindViewHolder(
        holder: GlobalMessageAdapter.GlobalMessageViewHolder,
        position: Int,
        model: Message
    ) {
        holder.bind(model)
    }

    /**
     * Global message view holder
     * Этот класс используется для хранения View элемента списка
     * @constructor
     * @param binding - привязка к View
     */
    inner class GlobalMessageViewHolder(private val binding: GlobalMessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Bind
         * Этот метод используется для привязки данных к View
         * @param message - модель, из которой берутся данные
         */
        fun bind(message: Message) {
            binding.apply {
                messageText.text = message.text
                val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                val formattedDate =
                    message.timestamp.toDate().let { formatter.format(it) } ?: "12:00"
                timestampText.text = formattedDate

                val marginPx = dpToPx(marginDp)
                val layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )

                if (message.sender_id == UserManager.currentUser.id) {
                    layoutParams.apply {
                        gravity = Gravity.END
                        topMargin = marginPx
                        bottomMargin = marginPx
                    }

                    globalMessageCard.setCardBackgroundColor(myMessageCardColor)
                    messageText.setTextColor(myMessageTextColor)
                    timestampText.setTextColor(myMessageTextColor)
                    usernameText.visibility = View.GONE
                    profilePictureImageView.visibility = View.GONE
                } else {
                    layoutParams.apply {
                        gravity = Gravity.START
                        topMargin = marginPx
                        bottomMargin = marginPx
                    }
                    globalMessageCard.setCardBackgroundColor(notMyMessageCardColor)
                    messageText.setTextColor(notMyMessageTextColor)
                    timestampText.setTextColor(notMyMessageTextColor)
                    usernameText.visibility = View.VISIBLE
                    profilePictureImageView.visibility = View.VISIBLE
                    if (message.id.isNotEmpty()) {
                        Firebase.firestore.collection(KEY_COLLECTION_USERS)
                            .document(message.sender_id)
                            .get()
                            .addOnSuccessListener { user ->
                                usernameText.text = user.getString("name")
                                Glide.with(context)
                                    .load(user.getString("profile_picture"))
                                    .placeholder(R.drawable.baseline_account_circle_24)
                                    .into(profilePictureImageView)

                                usernameText.setOnClickListener {
                                    onClick(user.toObject(User::class.java)!!)
                                }
                                profilePictureImageView.setOnClickListener {
                                    onClick(user.toObject(User::class.java)!!)
                                }
                            }
                    }
                }

                globalMessageCard.layoutParams = layoutParams
            }
        }

        /**
         * Dp to px
         * Этот метод используется для преобразования размерности из dp в px
         */
        private fun dpToPx(dp: Int): Int {
            val density = context.resources.displayMetrics.density
            return (dp * density + 0.5f).toInt()
        }
    }

}