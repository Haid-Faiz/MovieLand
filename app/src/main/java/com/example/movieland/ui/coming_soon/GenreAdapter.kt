package com.example.movieland.ui.coming_soon

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.datasource.remote.models.responses.Genre
import com.example.movieland.R
import com.example.movieland.databinding.ItemGenreBinding

class GenreAdapter(
    private val cardBackColor: Int? = null
) : ListAdapter<Genre, GenreAdapter.ViewHolder>(DiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ItemGenreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemGenreBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(genre: Genre) = binding.apply {
            cardBackColor?.let {
                card.setCardBackgroundColor(it)
            }
            genresText.text = genre.name
        }
    }
}


class DiffUtilCallback : DiffUtil.ItemCallback<Genre>() {

    override fun areItemsTheSame(oldItem: Genre, newItem: Genre): Boolean =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: Genre, newItem: Genre): Boolean =
        oldItem.equals(newItem)
}
