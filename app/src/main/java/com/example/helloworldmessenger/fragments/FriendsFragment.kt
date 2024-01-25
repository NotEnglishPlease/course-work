package com.example.helloworldmessenger.fragments

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.helloworldmessenger.R
import com.example.helloworldmessenger.adapters.UsersAdapter
import com.example.helloworldmessenger.databinding.FragmentFriendsBinding
import com.example.helloworldmessenger.models.User
import com.example.helloworldmessenger.utils.KEY_COLLECTION_USERS
import com.example.helloworldmessenger.utils.KEY_FRIENDS
import com.example.helloworldmessenger.utils.KEY_ID
import com.example.helloworldmessenger.utils.KEY_INCOMING_REQUESTS
import com.example.helloworldmessenger.utils.KEY_IS_ONLINE
import com.example.helloworldmessenger.utils.KEY_NAME
import com.example.helloworldmessenger.utils.KEY_OUTGOING_REQUESTS
import com.example.helloworldmessenger.utils.UserManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Friends fragment
 * Этот фрагмент используется для отображения друзей.
 * @constructor создает пустой фрагмент друзей
 */
class FriendsFragment : Fragment() {

    private lateinit var binding: FragmentFriendsBinding
    private val db = Firebase.firestore
    private var checkedButton = R.id.friendsButton

    /**
     * On create view
     * Этот метод устанавливает представление фрагмента
     * @param inflater - объект, который раздувает все элементы view на фрагменте
     * @param savedInstanceState - объект, необходимый для сохранения состояний
     * @return возвращает созданное представление
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFriendsBinding.inflate(layoutInflater)
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

        handleSearchViewBackPressed()
        setupSearch()
        handleToggleButtonGroup()
    }

    /**
     * On save inctance state
     * Этот метод используется для сохранения значений
     * @param outState - словарь, содержащий сохраненные значения
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("checkedButton", checkedButton)
    }

    /**
     * Handle search view back pressed
     * Этот метод обрабатывает нажатие кнопки назад на объекте SearchView
     */
    private fun handleSearchViewBackPressed() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.searchView.isShown) {
                    binding.searchView.hide()
                } else {
                    findNavController().navigateUp()
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, onBackPressedCallback
        )
    }

    /**
     * Handle toggle button group
     * Этот метод обрабатывает выбор кнопки из группы кнопок
     */
    private fun handleToggleButtonGroup() {

        /**
         * Dp as pixels
         * Этот метод используется для получение значения dp из значения pixels
         * @param pixels
         */
        fun dpAsPixels(pixels: Int): Int =
            (pixels * resources.displayMetrics.density + 0.5f).toInt()

        // Создание экземпляра перехода для анимации
        val transition = AutoTransition().apply {
            duration = 300 // Установка продолжительности анимации (в миллисекундах)
        }

        // Скрытие текста невыбранных кнопок с анимацией
        binding.toggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            val button = binding.toggleButton.findViewById<MaterialButton>(checkedId)
            TransitionManager.beginDelayedTransition(binding.toggleButton, transition)

            if (isChecked) {
                // Установка layout_weight значению 2
                (button.layoutParams as LinearLayout.LayoutParams).weight = 2f
                button.iconPadding = dpAsPixels(8)

                when (checkedId) {
                    R.id.friendsButton -> {
                        button.text = getString(R.string.friends)
                        // Настрока запросов "Друзья" в главном окне recyclerview
                        setUpWithFriendsQuery(binding.friendsRecyclerView)
                    }

                    R.id.outgoingButton -> {
                        button.text = getString(R.string.outgoing)
                        // Настрока запросов исходящих запросов в главном представлении recyclerview
                        setUpWithOutgoingRequestsQuery(binding.friendsRecyclerView)

                    }

                    R.id.incomingButton -> {
                        button.text = getString(R.string.incoming)
                        // Настрока запросов входящих запросов в главном окне recyclerview
                        setUpWithIncomingRequestsQuery(binding.friendsRecyclerView)
                    }
                }
            } else {
                (button.layoutParams as LinearLayout.LayoutParams).weight = 0f
                button.text = ""
                button.iconPadding = 0
            }

            checkedButton = checkedId
        }

        // Выбор кнопку "Друзья" по умолчанию
        binding.toggleButton.check(checkedButton)
    }

    /**
     * Setup search
     * Этот метод настраивает представления поиска и повторного просмотра результатов поиска.
     */
    private fun setupSearch() {
        binding.apply {
            searchView.editText.addTextChangedListener {
                if (it.toString().isEmpty()) {
                    handleSearchResultsUi(false)
                } else {
                    performSearchWithQuery(searchText = it.toString())
                }
            }
        }
    }

    /**
     * Perform search with query
     * Этот метод выполняет поиск по заданному тексту поиска.
     * @param searchText - текст, по которому проводится поиск
     */
    private fun performSearchWithQuery(searchText: String) {
        db.collection(KEY_COLLECTION_USERS).whereGreaterThanOrEqualTo(KEY_NAME, searchText)
            .whereLessThanOrEqualTo(KEY_NAME, searchText + '\uf8ff').get()
            .addOnSuccessListener { querySnapshot ->
                // Фильтруем текущего пользователя
                val users = querySnapshot.documents.mapNotNull { documentSnapshot ->
                    documentSnapshot.getString(KEY_ID)
                }

                if (users.isNotEmpty()) {
                    val query = db.collection(KEY_COLLECTION_USERS).whereIn(KEY_ID, users)
                        .whereNotEqualTo(KEY_ID, UserManager.currentUser.id)

                    setUpUsersRecyclerView(binding.searchResultsRecyclerView, query)
                } else {
                    handleSearchResultsUi(false)
                }
            }
    }

    /**
     * Set up with friends query
     * Этот метод вызывает установку RecyclerView с запросом списка друзей
     * @param recyclerView
     */
    private fun setUpWithFriendsQuery(recyclerView: RecyclerView) {
        val query = db.collection(KEY_COLLECTION_USERS)
            .whereArrayContains(KEY_FRIENDS, UserManager.currentUser.id)

        setUpUsersRecyclerView(recyclerView, query)
    }

    /**
     * Set up with outgoing requests query
     * Этот метод вызывает установку RecyclerView с запросом списка исходящих заявок
     * @param friendsRecyclerView
     */
    private fun setUpWithOutgoingRequestsQuery(friendsRecyclerView: RecyclerView) {
        val query = db.collection(KEY_COLLECTION_USERS)
            .whereArrayContains(KEY_INCOMING_REQUESTS, UserManager.currentUser.id)

        setUpUsersRecyclerView(friendsRecyclerView, query)
    }

    /**
     * Set up with outgoing requests query
     * Этот метод вызывает установку RecyclerView с запросом списка входящих заявок
     * @param friendsRecyclerView
     */
    private fun setUpWithIncomingRequestsQuery(friendsRecyclerView: RecyclerView) {
        val query = db.collection(KEY_COLLECTION_USERS)
            .whereArrayContains(KEY_OUTGOING_REQUESTS, UserManager.currentUser.id)

        setUpUsersRecyclerView(friendsRecyclerView, query)
    }

    /**
     * Set up with users query
     * Этот метод устанавливет RecyclerView по заданному запросу
     * @param recyclerView
     * @param usersQuery - полученный запрос
     */
    private fun setUpUsersRecyclerView(recyclerView: RecyclerView, usersQuery: Query) {
        val options =
            FirestoreRecyclerOptions.Builder<User>()
                .setQuery(usersQuery) {
                    val newUser = it.toObject(User::class.java)!!
                    newUser.is_online = it.getBoolean(KEY_IS_ONLINE) ?: false
                    newUser
                }
                .build()

        val adapter = UsersAdapter(requireContext(), options) { user ->
            showUserCard(user)
        }

        adapter.setOnDataChangedListener {
            when (recyclerView) {
                binding.friendsRecyclerView -> {
                    handleFriendsUi(adapter.itemCount > 0)
                }

                binding.searchResultsRecyclerView -> {
                    handleSearchResultsUi(adapter.itemCount > 0)
                }
            }
        }
        adapter.startListening()
        // Удаление старого адаптера из памяти
        (recyclerView.adapter as? UsersAdapter)?.stopListening()
        recyclerView.adapter = null
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    /**
     * Show user card
     * Этот метод осуществляет переход на профиль пользователя
     * @param user - модель, из которой берутся данные
     */
    private fun showUserCard(user: User) {
        val action = FriendsFragmentDirections.actionFriendsFragmentToUserProfileFragment(
            name = user.name,
            id = user.id,
            profilePicture = user.profile_picture,
            isOnline = user.is_online
        )

        findNavController().navigate(action)
    }

    /**
     * Handle Friends ui
     * Этот метод обрабатывет видимость чата
     * @param isFriendsFound - переменная, отвечающая за нахождения друзей.
     */
    private fun handleFriendsUi(isFriendsFound: Boolean) {
        // Обновление пользовательского интерфейс, чтобы показать, что у пользователя нет друзей
        binding.apply {
            if (isFriendsFound) {
                friendsRecyclerView.visibility = View.VISIBLE
                emptyFriendsTextView.visibility = View.GONE
                emptySearchResultsTextView.visibility = View.GONE
            } else {
                friendsRecyclerView.visibility = View.GONE
                searchResultsRecyclerView.visibility = View.GONE
                emptyFriendsTextView.visibility = View.VISIBLE
            }
        }

    }

    /**
     * Handle search results ui
     * Этот метод обрабатывет видимоcть результатов поиска
     * @param isUsersFound - переменная, отвечающая за нахождения пользователей.
     */
    private fun handleSearchResultsUi(isUsersFound: Boolean) {
        // Обновление пользовательского интерфейса, чтобы показать, что результаты поиска пусты
        // Например, отображение сообщения или скрытие результатов поиска в RecyclerView
        binding.apply {
            searchResultsRecyclerView.visibility = if (isUsersFound) View.VISIBLE else View.GONE
            emptySearchResultsTextView.visibility = if (isUsersFound) View.GONE else View.VISIBLE
        }
    }
}

