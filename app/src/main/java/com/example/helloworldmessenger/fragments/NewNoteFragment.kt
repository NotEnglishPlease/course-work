package com.example.helloworldmessenger.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.helloworldmessenger.MyApplication
import com.example.helloworldmessenger.R
import com.example.helloworldmessenger.databinding.FragmentNewNoteBinding
import com.example.helloworldmessenger.room.Note
import com.example.helloworldmessenger.utils.KEY_COLLECTION_USERS
import com.example.helloworldmessenger.utils.KEY_PROFILE_PICTURE
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NewNoteFragment : Fragment() {

    private lateinit var binding: FragmentNewNoteBinding
    private val args: NewNoteFragmentArgs by navArgs()
    private val db = Firebase.firestore
    private val noteDao by lazy {
        (requireActivity().application as MyApplication).noteDao
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentNewNoteBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db.collection(KEY_COLLECTION_USERS)
            .document(args.id)
            .get()
            .addOnSuccessListener {
                Glide.with(requireContext())
                    .load(it.getString(KEY_PROFILE_PICTURE))
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .into(binding.profilePictureImageView)

                binding.usernameTextView.text = it.getString("name")
            }

        binding.apply {

            noteToolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            lifecycleScope.launch {
                noteDao.getById(args.id)?.let {
                    editTextTextMultiLine.setText(it.noteText)
                }
            }

            saveButton.setOnClickListener {
                val note = Note(
                    args.id,
                    editTextTextMultiLine.text.toString()
                )
                lifecycleScope.launch(Dispatchers.IO) {
                    noteDao.insertOrUpdate(note)
                }
            }

            deleteButton.setOnClickListener {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        noteDao.delete(args.id)
                    }
                    findNavController().popBackStack()
                }
            }
        }
    }
}