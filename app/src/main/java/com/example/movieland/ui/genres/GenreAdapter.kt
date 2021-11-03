package com.example.movieland.ui.genres

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.datasource.remote.models.responses.Genre
import com.example.movieland.databinding.ItemGenreBinding

class GenreAdapter(
    private val cardBackColor: Int? = null,
    private val cardStrokeColor: Int? = null
) : ListAdapter<Genre, GenreAdapter.ViewHolder>(GenresDiffUtilCallback()) {

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
            cardStrokeColor?.let {
                card.strokeColor = it
            }
            genresText.text = genre.name
        }
    }
}
