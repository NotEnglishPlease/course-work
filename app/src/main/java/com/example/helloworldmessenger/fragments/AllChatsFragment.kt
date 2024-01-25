package com.example.helloworldmessenger.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.helloworldmessenger.adapters.AllChatsAdapter
import com.example.helloworldmessenger.databinding.FragmentAllChatsBinding
import com.example.helloworldmessenger.models.Conversation
import com.example.helloworldmessenger.utils.KEY_COLLECTION_CONVERSATIONS
import com.example.helloworldmessenger.utils.KEY_COLLECTION_USERS
import com.example.helloworldmessenger.utils.KEY_ID
import com.example.helloworldmessenger.utils.KEY_NAME
import com.example.helloworldmessenger.utils.KEY_PARTICIPANTS
import com.example.helloworldmessenger.utils.UserManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * All chats fragment
 * Этот фрагмент используется для отображения чатов.
 * @constructor создает пустой фрагмент чатов
 */
class AllChatsFragment : Fragment() {

    private lateinit var binding: FragmentAllChatsBinding
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
        binding = FragmentAllChatsBinding.inflate(layoutInflater)
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

        //handleBottomNavigationViewVisibility()
        setupSearch()
        initMainRecyclerView()
        handleSearchViewBackPressed()
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
     * Setup search
     * Этот метод настраивает представления поиска и повторного просмотра результатов поиска.
     */
    private fun setupSearch() {
        binding.searchView.editText.addTextChangedListener {
            performSearch(searchText = it.toString().trim())
        }
        initSearchRecyclerView()
    }

    /**
     * Init main recycler view
     * Этот метод инициализирует основное представление RecyclerView.
     * В главном окне recycler отображаются все разговоры, частью которых является текущий пользователь.
     */
    private fun initMainRecyclerView() {
        val query = db.collection(KEY_COLLECTION_CONVERSATIONS)
            .whereArrayContains(KEY_PARTICIPANTS, UserManager.currentUser.id)

        val options = buildConversationOptions(query)

        val mainAdapter = AllChatsAdapter(requireContext(), options) { conversation ->
            navigateToChat(conversation)
        }
        mainAdapter.setOnDataChangedListener {
            handleChatsUi(mainAdapter.itemCount > 0)
        }
        mainAdapter.startListening()

        binding.chatsRecyclerView.adapter = mainAdapter
        binding.chatsRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    /**
     * Update search adapter
     * Этот метод обновляет представления recycler результатов поиска с заданными параметрами.
     * @param options - параметры, с помощью которых можно обновить представление recycler результатов поиска.
     */
    private fun updateSearchAdapter(options: FirestoreRecyclerOptions<Conversation>) {
        val newSearchAdapter = AllChatsAdapter(requireContext(), options) { conversation ->
            navigateToChat(conversation)
        }
        newSearchAdapter.startListening()
        binding.searchResultsRecyclerView.adapter = newSearchAdapter
    }

    /**
     * Perform search
     * Этот метод выполняет поиск по заданному тексту поиска.
     * Если текст поиска пуст, поиск будет выполняться по запросу по умолчанию.
     * @param searchText - текст, по которому проводится поиск
     */
    private fun performSearch(searchText: String) {
        if (searchText.isEmpty()) {
            performSearchWithDefaultQuery()
        } else {
            performSearchWithQuery(searchText)
        }
    }

    /**
     * Perform search with default query
     * Этот метод выполняет поиск по запросу по умолчанию.
     * Запрос по умолчанию используется для отображения всех разговоров, частью которых является текущий пользователь.
     */
    private fun performSearchWithDefaultQuery() {
        val defaultQuery = db.collection(KEY_COLLECTION_CONVERSATIONS)
            .whereArrayContains(KEY_PARTICIPANTS, UserManager.currentUser.id)

        val newSearchOptions = buildConversationOptions(defaultQuery)
        updateSearchAdapter(newSearchOptions)
    }

    /**
     * Perform search with query
     * Этот метод выполняет поиск по заданному тексту поиска.
     * @param searchText - текст, по которому проводится поиск
     */
    private fun performSearchWithQuery(searchText: String) {
        // Search for users with the given search text
        db.collection(KEY_COLLECTION_USERS)
            .whereGreaterThanOrEqualTo(KEY_NAME, searchText)
            .whereLessThanOrEqualTo(KEY_NAME, searchText + '\uf8ff')
            .orderBy(KEY_NAME)
            .get()
            .addOnSuccessListener { snapshot ->
                // Get the user ids of the users that match the search text
                val matches = snapshot.mapNotNull { doc ->
                    doc.getString(KEY_ID)
                }

                // Если есть какие-либо совпадения, выполняется поиск разговоров, содержащих какие-либо совпадения
                if (matches.isNotEmpty()) {
                    db.collection(KEY_COLLECTION_CONVERSATIONS)
                        .whereArrayContainsAny(KEY_PARTICIPANTS, matches).get()
                        .addOnSuccessListener { containAnyMatchSnapshot ->
                            // Получение разговоров, частью которых является текущий пользователь
                            val filteredConversationsId =
                                containAnyMatchSnapshot.filter { doc ->
                                    val conversationParticipants =
                                        doc.get(KEY_PARTICIPANTS) as List<String>
                                    conversationParticipants.contains(UserManager.currentUser.id)
                                }.map { doc -> doc.id }

                            // Если есть какие-либо разговоры, частью которых является текущий пользователь,
                            // найдем их
                            if (filteredConversationsId.isNotEmpty()) {
                                val newSearchQuery = db.collection(KEY_COLLECTION_CONVERSATIONS)
                                    .whereIn(KEY_ID, filteredConversationsId)
                                val newSearchOptions = buildConversationOptions(newSearchQuery)
                                updateSearchAdapter(newSearchOptions)
                            }
                        }
                }
            }
    }

    /**
     * Init search recycler view
     * Этот метод инициализирует представления обработчика результатов поиска.
     */
    private fun initSearchRecyclerView() {
        val searchQuery = db.collection(KEY_COLLECTION_CONVERSATIONS)
            .whereArrayContains(KEY_PARTICIPANTS, UserManager.currentUser.id)

        val searchOptions = buildConversationOptions(searchQuery)

        // Инициализируем вложенный RecyclerView (RecyclerView результатов поиска)
        val searchAdapter = AllChatsAdapter(requireContext(), searchOptions) { conversation ->
            navigateToChat(conversation)
        }
        searchAdapter.setOnDataChangedListener {
            handleSearchResultsUi(searchAdapter.itemCount > 0)
        }
        searchAdapter.startListening()

        binding.searchResultsRecyclerView.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    /**
     * Navigate to chat
     * Этот метод используется для перехода к фрагменту чата с заданным разговором.
     * Идентификатор беседы передается в качестве аргумента фрагменту чата.
     * @param conversationId - идентификатор беседы, к которой нужно перейти.
     */
    private fun navigateToChat(conversationId: String) {
        val action =
            AllChatsFragmentDirections.actionAllChatsFragmentToChatFragment(conversationId)
        findNavController().navigate(action)
    }

    /**
     * Build conversation options
     * Этот метод создает параметры для представления recycler conversation recycler.
     * @param query - запрос для создания параметров с помощью.
     * @return возвращает параметры для повторного просмотра беседы.
     */
    private fun buildConversationOptions(query: Query): FirestoreRecyclerOptions<Conversation> {
        return FirestoreRecyclerOptions.Builder<Conversation>()
            .setQuery(query, Conversation::class.java)
            .build()
    }

    /**
     * Handle chats ui
     * Этот метод обрабатывет видимость чата
     * @param isChatsFound - переменная, отвечающая за нахождения чата.
     */
    private fun handleChatsUi(isChatsFound: Boolean) {
        if (isChatsFound) {
            binding.chatsRecyclerView.visibility = View.VISIBLE
            binding.noChatsFoundTextView.visibility = View.GONE
        } else {
            binding.chatsRecyclerView.visibility = View.GONE
            binding.noChatsFoundTextView.visibility = View.VISIBLE
        }
    }

    /**
     * Handle search results ui
     * Этот метод обрабатывет видимоcть результатов поиска
     * @param isChatsFound - переменная, отвечающая за нахождения чата.
     */
    private fun handleSearchResultsUi(isChatsFound: Boolean) {
        if (isChatsFound) {
            binding.searchResultsRecyclerView.visibility = View.VISIBLE
            binding.emptySearchResultsTextView.visibility = View.GONE
        } else {
            binding.searchResultsRecyclerView.visibility = View.GONE
            binding.emptySearchResultsTextView.visibility = View.VISIBLE
        }
    }
}