package com.example.helloworldmessenger.adapters

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.helloworldmessenger.R
import com.example.helloworldmessenger.databinding.MessageItemBinding
import com.example.helloworldmessenger.models.Message
import com.example.helloworldmessenger.utils.KEY_COLLECTION_MESSAGES
import com.example.helloworldmessenger.utils.KEY_IS_READ
import com.example.helloworldmessenger.utils.UserManager
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Message adapter
 * Этот адаптер используется для отображения сообщений в обычном чате.
 * @property context - контекст, нужен для получения ресурсов
 * @property options - опции для FirestoreRecyclerAdapter, в котором хранятся данные
 * @constructor Создает пустое сообщение
 */
class MessageAdapter(
    private val context: Context,
    options: FirestoreRecyclerOptions<Message>
) :
    FirestoreRecyclerAdapter<Message, MessageAdapter.MessageViewHolder>(options) {

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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding =
            MessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    /**
     * On bind view holder
     * Этот метод вызывается при привязке ViewHolder'а к определенной позиции.
     * В нем происходит привязка данных к View
     * @param holder - ViewHolder, к которому привязываются данные
     * @param position - позиция, к которой привязываются данные
     * @param model - модель, из которой берутся данные
     */
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int, model: Message) {
        holder.bind(model)
    }

    /**
     * Message view holder
     * Этот класс используется для хранения View элемента списка
     * @constructor
     * @param binding - привязка к View
     */
    inner class MessageViewHolder(private val binding: MessageItemBinding) :
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
                messageTime.text = formattedDate

                val layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )

                val marginPixels = dpToPx(marginDp)

                if (message.sender_id == UserManager.currentUser.id) {
                    layoutParams.topMargin = marginPixels
                    layoutParams.bottomMargin = marginPixels
                    layoutParams.gravity = Gravity.END
                    messageCard.setCardBackgroundColor(myMessageCardColor)
                    messageText.setTextColor(myMessageTextColor)
                    messageTime.setTextColor(myMessageTextColor)
                    isReadIcon.visibility = View.VISIBLE
                    isReadIcon.setImageDrawable(getIsReadIcon(message.is_read))
                    isReadIcon.layoutParams.width = if (message.is_read) 40 else 20
                    isReadIcon.layoutParams.height = if (message.is_read) 40 else 20
                } else {
                    layoutParams.topMargin = marginPixels
                    layoutParams.bottomMargin = marginPixels
                    layoutParams.gravity = Gravity.START
                    messageCard.setCardBackgroundColor(
                        notMyMessageCardColor
                    )
                    messageText.setTextColor(notMyMessageTextColor)
                    messageTime.setTextColor(notMyMessageTextColor)
                    message.is_read = true
                    Firebase.firestore.collection(KEY_COLLECTION_MESSAGES)
                        .document(message.id).update(KEY_IS_READ, true)
                    isReadIcon.visibility = View.GONE
                }

                messageCard.layoutParams = layoutParams
            }
        }

        /**
         * Get is read icon
         * Этот метод возвращает соответствующую иконку в зависимости от статуса сообщения
         */
        private fun getIsReadIcon(isRead: Boolean) =
            ContextCompat.getDrawable(
                binding.root.context,
                if (isRead) R.drawable.baseline_check_24 else R.drawable.baseline_circle_24
            )

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
