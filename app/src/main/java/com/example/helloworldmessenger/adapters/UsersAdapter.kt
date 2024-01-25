package com.example.helloworldmessenger.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.helloworldmessenger.R
import com.example.helloworldmessenger.databinding.UserItemBinding
import com.example.helloworldmessenger.models.User
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

/**
 * Users adapter
 * Этот адаптер используется для отображения пользователей.
 * @property context - контекст, нужен для получения ресурсов
 * @property options - опции для FirestoreRecyclerAdapter, в котором хранятся данные
 * @property onItemClick - лямбда-выражение, которое вызывается при нажатии на пользователя
 * @constructor Создает пустое сообщение
 */
class UsersAdapter(
    private val context: Context,
    options: FirestoreRecyclerOptions<User>,
    private val onItemClick: (User) -> Unit
) :
    FirestoreRecyclerAdapter<User, UsersAdapter.UserViewHolder>(options) {

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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = UserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    /**
     * On bind view holder
     * Этот метод вызывается при привязке ViewHolder'а к определенной позиции.
     * В нем происходит привязка данных к View
     * @param holder - ViewHolder, к которому привязываются данные
     * @param position - позиция, к которой привязываются данные
     * @param model - модель, из которой берутся данные
     */
    override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
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
     * User view holder
     * Этот класс используется для хранения View элемента списка
     * @constructor
     * @param binding - привязка к View
     */
    inner class UserViewHolder(private val binding: UserItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Bind
         * Этот метод используется для привязки данных к View
         * @param user - модель, из которой берутся данные
         */
        fun bind(user: User) {
            binding.apply {
                usernameTextView.text = user.name
                Log.d("AAA", "bind: ${user.profile_picture}")
                Glide.with(itemView.context)
                    .load(user.profile_picture)
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .into(profilePictureImageView)
                itemView.setOnClickListener {
                    onItemClick(user)
                }
                Log.d("Online", "bind: ${user.is_online}")
                isOnlineTextView.text = if (user.is_online)
                    context.getString(R.string.online_status)
                else
                    context.getString(R.string.offline_status)
            }
        }
    }
}