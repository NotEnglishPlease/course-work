package com.example.helloworldmessenger.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.helloworldmessenger.MyApplication
import com.example.helloworldmessenger.adapters.NoteAdapter
import com.example.helloworldmessenger.databinding.FragmentMyNoteBinding
import kotlinx.coroutines.launch


class MyNoteFragment : Fragment() {

    private lateinit var binding: FragmentMyNoteBinding
    private lateinit var recyclerView: RecyclerView
    private val noteDao by lazy {
        (requireActivity().application as MyApplication).noteDao
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMyNoteBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.noteRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        val noteAdapter = NoteAdapter() { note ->
            val action = MyNoteFragmentDirections.actionMyNoteFragmentToNewNoteFragment(
                id = note.id
            )
            findNavController().navigate(action)
        }

        recyclerView.adapter = noteAdapter
        lifecycleScope.launch {
            noteDao.getAll().collect() {
                noteAdapter.submitList(it)
            }
        }
    }

}