package com.example.helloworldmessenger.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.helloworldmessenger.R
import com.example.helloworldmessenger.databinding.FragmentUserProfileBinding
import com.example.helloworldmessenger.models.Conversation
import com.example.helloworldmessenger.models.User
import com.example.helloworldmessenger.utils.KEY_COLLECTION_CONVERSATIONS
import com.example.helloworldmessenger.utils.KEY_COLLECTION_USERS
import com.example.helloworldmessenger.utils.KEY_FRIENDS
import com.example.helloworldmessenger.utils.KEY_ID
import com.example.helloworldmessenger.utils.KEY_INCOMING_REQUESTS
import com.example.helloworldmessenger.utils.KEY_IS_ONLINE
import com.example.helloworldmessenger.utils.KEY_OUTGOING_REQUESTS
import com.example.helloworldmessenger.utils.KEY_PARTICIPANTS
import com.example.helloworldmessenger.utils.UserManager
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * User profile fragment
 * Этот фрагмент используется для отображения данных пользователя.
 * @constructor создает пустой фрагмент пользователя
 */
class UserProfileFragment : DialogFragment() {

    /**
     * Friends status
     * Этот класс используется для задания всех возможных взаимосвязей между пользователями в виде констант
     */
    enum class FriendStatus {
        FRIENDS,
        INCOMING_REQUEST,
        OUTGOING_REQUEST,
        NOT_FRIENDS
    }

    private lateinit var binding: FragmentUserProfileBinding
    private val args: UserProfileFragmentArgs by navArgs()
    private val db = Firebase.firestore
    private lateinit var userListener: ListenerRegistration
    private lateinit var friendStatus: FriendStatus

    /**
     * On create dialog
     * Этот метод создает собственный пользовательский диалоговый контейнер
     * @param savedInstanceState - объект, необходимый для сохранения состояний
     * @return новый экземпляр диалогового окна, который будет отображаться фрагментом.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.TransparentDialog)
        binding = FragmentUserProfileBinding.inflate(layoutInflater)
        binding.apply {
            closeButton.setOnClickListener {
                findNavController().popBackStack()
            }
            usernameTextView.text = args.name
            statusTextView.text = if (args.isOnline) {
                getString(R.string.online_status)
            } else {
                getString(R.string.offline_status)
            }
            Glide.with(requireActivity())
                .load(args.profilePicture)
                .placeholder(R.drawable.baseline_account_circle_24)
                .into(profilePictureImageView)
            writeMessageButton.setOnClickListener {
                createOrRetrieveConversation()
            }
            noteButton.setOnClickListener {
                navigateToNoteFragment()
            }

            updateFriendButton()
        }

        setupSnapshotListener()
        builder.setView(binding.root)
        return builder.create()
    }

    private fun navigateToNoteFragment() {
        val action =
            UserProfileFragmentDirections.actionUserProfileFragmentToNewNoteFragment(args.id)
        requireActivity().findNavController(R.id.nav_host_fragment).navigate(action)
    }

    /**
     * Setup snapshot listener
     * Этот метод устанавливает слушатель моментальных изменений
     */
    private fun setupSnapshotListener() {
        val userDocRef = db.collection(KEY_COLLECTION_USERS).document(args.id)

        userListener = userDocRef.addSnapshotListener { userSnapshot, e ->
            if (e != null) {
                // Обработка ошибки
                return@addSnapshotListener
            }

            if (userSnapshot != null && userSnapshot.exists()) {
                binding.statusTextView.text = if (userSnapshot.getBoolean(KEY_IS_ONLINE) == true) {
                    getString(R.string.online_status)
                } else {
                    getString(R.string.offline_status)
                }
            }
        }
    }

    /**
     * Create or retrieve conversation
     * Этот метод используется для создания чата между выбранным и текущим пользователем
     */
    private fun createOrRetrieveConversation() {
        val currentUser = UserManager.currentUser
        val participants = listOf(currentUser.id, args.id).sorted()
        val conversationId = participants.joinToString("_")

        val conversation = Conversation(participants = participants, id = conversationId)
        val conversationMap = mapOf(
            KEY_PARTICIPANTS to conversation.participants,
            KEY_ID to conversation.id
        )

        db.collection(KEY_COLLECTION_CONVERSATIONS)
            .document(conversation.id)
            .set(conversationMap)
            .addOnSuccessListener {
                navigateToChatFragment(conversation.id)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "Failed to create conversation: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    /**
     * Navigate to chat fragment
     * Этот метод осуществляет переход к чату с выбранным пользователем
     * @param conversationId - id чата
     */
    private fun navigateToChatFragment(conversationId: String) {
        val action =
            UserProfileFragmentDirections.actionUserProfileFragmentToChatFragment(conversationId)
        requireActivity().findNavController(R.id.nav_host_fragment).navigate(action)
    }

    /**
     * Update friend button
     * Этот метод определяет кем является выбранный пользователь для текущего пользователя
     * и в зависимости от этого устанавливает значение friendStatus
     */
    private fun updateFriendButton() {
        db.collection(KEY_COLLECTION_USERS)
            .document(UserManager.currentUser.id)
            .get()
            .addOnSuccessListener { currentUserSnapshot ->
                val currentUser = currentUserSnapshot.toObject(User::class.java)!!
                val friends = currentUser.friends
                val incomingRequests = currentUser.incoming_requests
                val outgoingRequests = currentUser.outgoing_requests

                friendStatus = when {
                    friends.contains(args.id) -> FriendStatus.FRIENDS
                    incomingRequests.contains(args.id) -> FriendStatus.INCOMING_REQUEST
                    outgoingRequests.contains(args.id) -> FriendStatus.OUTGOING_REQUEST
                    else -> FriendStatus.NOT_FRIENDS
                }

                setFriendButtonUi()
            }
    }

    /**
     * Set friend button ui
     * Этот метод обновляет представление кнопки FriendButton
     */
    private fun setFriendButtonUi() {
        binding.friendButton.apply {
            when (friendStatus) {
                FriendStatus.FRIENDS -> {
                    setOnClickListener { removeFriend(args.id) }
                    text = getString(R.string.remove_friend)
                }

                FriendStatus.INCOMING_REQUEST -> {
                    setOnClickListener { acceptFriendRequest(args.id) }
                    text = getString(R.string.accept_request)
                }

                FriendStatus.OUTGOING_REQUEST -> {
                    setOnClickListener { cancelFriendRequest(args.id) }
                    text = getString(R.string.cancel_request)
                }

                FriendStatus.NOT_FRIENDS -> {
                    setOnClickListener { sendFriendRequest(args.id) }
                    text = getString(R.string.add_friend)
                }
            }
        }
    }

    /**
     * Remove friend
     * Этот метод осуществляет удаление друга
     */
    private fun removeFriend(id: String) {
        val currentUserRef =
            db.collection(KEY_COLLECTION_USERS).document(UserManager.currentUser.id)
        val otherUserRef = db.collection(KEY_COLLECTION_USERS).document(id)

        currentUserRef.get().addOnSuccessListener { currentUserSnapshot ->
            val friends = currentUserSnapshot.get(KEY_FRIENDS) as List<String>
            val newFriends = friends.toMutableList().apply { remove(id) }
            currentUserRef.update(KEY_FRIENDS, newFriends)
        }

        otherUserRef.get().addOnSuccessListener { otherUserSnapshot ->
            val friends = otherUserSnapshot.get(KEY_FRIENDS) as List<String>
            val newFriends = friends.toMutableList().apply { remove(UserManager.currentUser.id) }
            otherUserRef.update(KEY_FRIENDS, newFriends)
        }

        friendStatus = FriendStatus.NOT_FRIENDS
        setFriendButtonUi()
    }

    /**
     * Accept friend request
     * Этот метод осуществляет принятие запроса в друзья
     */
    private fun acceptFriendRequest(id: String) {
        val currentUserRef =
            db.collection(KEY_COLLECTION_USERS).document(UserManager.currentUser.id)
        val otherUserRef = db.collection(KEY_COLLECTION_USERS).document(id)

        currentUserRef.get().addOnSuccessListener { currentUserSnapshot ->
            val friends = currentUserSnapshot.get(KEY_FRIENDS) as List<String>
            val newFriends = friends.toMutableList().apply { add(id) }
            currentUserRef.update(KEY_FRIENDS, newFriends)

            val incomingRequests = currentUserSnapshot.get(KEY_INCOMING_REQUESTS) as List<String>
            val newIncomingRequests = incomingRequests.toMutableList().apply { remove(id) }
            currentUserRef.update(KEY_INCOMING_REQUESTS, newIncomingRequests)
        }

        otherUserRef.get().addOnSuccessListener { otherUserSnapshot ->
            val outgoingRequests = otherUserSnapshot.get(KEY_OUTGOING_REQUESTS) as List<String>
            val newOutgoingRequests =
                outgoingRequests.toMutableList().apply { remove(UserManager.currentUser.id) }
            otherUserRef.update(KEY_OUTGOING_REQUESTS, newOutgoingRequests)

            val friends = otherUserSnapshot.get(KEY_FRIENDS) as List<String>
            val newFriends = friends.toMutableList().apply { add(UserManager.currentUser.id) }
            otherUserRef.update(KEY_FRIENDS, newFriends)
        }

        friendStatus = FriendStatus.FRIENDS
        setFriendButtonUi()
    }

    /**
     * Cancel friend request
     * Этот метод осуществляет отклонение запроса в друзья
     */
    private fun cancelFriendRequest(id: String) {
        val currentUserRef =
            db.collection(KEY_COLLECTION_USERS).document(UserManager.currentUser.id)
        val otherUserRef = db.collection(KEY_COLLECTION_USERS).document(id)

        currentUserRef.get().addOnSuccessListener { currentUserSnapshot ->
            val outgoingRequests = currentUserSnapshot.get(KEY_OUTGOING_REQUESTS) as List<String>
            val newOutgoingRequests = outgoingRequests.toMutableList().apply { remove(id) }
            currentUserRef.update(KEY_OUTGOING_REQUESTS, newOutgoingRequests)
        }

        otherUserRef.get().addOnSuccessListener { otherUserSnapshot ->
            val incomingRequests = otherUserSnapshot.get(KEY_INCOMING_REQUESTS) as List<String>
            val newIncomingRequests =
                incomingRequests.toMutableList().apply { remove(UserManager.currentUser.id) }
            otherUserRef.update(KEY_INCOMING_REQUESTS, newIncomingRequests)
        }

        friendStatus = FriendStatus.NOT_FRIENDS
        setFriendButtonUi()
    }

    /**
     * Send friend request
     * Этот метод осуществляет отправку запроса в друзья
     */
    private fun sendFriendRequest(id: String) {
        val currentUserRef =
            db.collection(KEY_COLLECTION_USERS).document(UserManager.currentUser.id)
        val otherUserRef = db.collection(KEY_COLLECTION_USERS).document(id)

        currentUserRef.get().addOnSuccessListener { currentUserSnapshot ->
            val outgoingRequests = currentUserSnapshot.get(KEY_OUTGOING_REQUESTS) as List<String>
            val newOutgoingRequests = outgoingRequests.toMutableList().apply { add(id) }
            currentUserRef.update(KEY_OUTGOING_REQUESTS, newOutgoingRequests)
        }

        otherUserRef.get().addOnSuccessListener { otherUserSnapshot ->
            val incomingRequests = otherUserSnapshot.get(KEY_INCOMING_REQUESTS) as List<String>
            val newIncomingRequests =
                incomingRequests.toMutableList().apply { add(UserManager.currentUser.id) }
            otherUserRef.update(KEY_INCOMING_REQUESTS, newIncomingRequests)
        }

        friendStatus = FriendStatus.OUTGOING_REQUEST
        setFriendButtonUi()
    }

    /**
     * On dismiss
     * Данный метод используется для удаления слушателя пользователя
     */
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        userListener.remove()
    }

}