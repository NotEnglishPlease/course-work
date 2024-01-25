package com.example.helloworldmessenger.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.helloworldmessenger.R
import com.example.helloworldmessenger.databinding.NoteItemBinding
import com.example.helloworldmessenger.room.Note
import com.example.helloworldmessenger.utils.KEY_COLLECTION_USERS
import com.example.helloworldmessenger.utils.KEY_NAME
import com.example.helloworldmessenger.utils.KEY_PROFILE_PICTURE
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class NoteAdapter(private val onItemClicked: (Note) -> Unit) :
    ListAdapter<Note, NoteAdapter.NoteViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem == newItem
            }
        }
    }


    class NoteViewHolder(private var binding: NoteItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            binding.noteTextView.text = note.noteText
            Firebase.firestore.collection(KEY_COLLECTION_USERS)
                .document(note.id)
                .get()
                .addOnSuccessListener {
                    binding.usernameTextView.text = it.getString(KEY_NAME)
                    Glide.with(itemView.context)
                        .load(it.get(KEY_PROFILE_PICTURE))
                        .placeholder(R.drawable.baseline_account_circle_24)
                        .into(binding.profilePictureImageView)
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val viewHolder = NoteViewHolder(
            NoteItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.bindingAdapterPosition
            onItemClicked(getItem(position))
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}