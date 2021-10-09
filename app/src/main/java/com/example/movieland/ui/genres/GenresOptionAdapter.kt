package com.example.movieland.ui.genres

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.datasource.remote.models.responses.Genre
import com.example.movieland.databinding.ItemGenresOptionsBinding

class GenresOptionAdapter(
    private val selectedStrokeColor: Int,
    private val unSelectedStrokeColor: Int,
    private val cardBgColor: Int,
    private val selectGenreItemClick: (genre: Genre) -> Unit,
    private val removeGenreItemClick: (genre: Genre) -> Unit
) : ListAdapter<Genre, GenresOptionAdapter.ViewHolder>(GenresDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ItemGenresOptionsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class ViewHolder(
        private val binding: ItemGenresOptionsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(genre: Genre) = binding.apply {
            genresText.text = genre.name
            card.setOnClickListener {
                if (!card.isChecked) {
                    card.isChecked = true
                    // Change bg of clicked item
                    card.strokeColor = selectedStrokeColor
//                    card.setCardBackgroundColor(cardBgColor)
                    // send callback to ui
                    selectGenreItemClick(genre)
                } else {
                    card.isChecked = false
                    // Change bg of clicked item
                    card.strokeColor = unSelectedStrokeColor
                    // send callback to ui
                    removeGenreItemClick(genre)
                }
            }
        }
    }

}

class GenresDiffUtilCallback : DiffUtil.ItemCallback<Genre>() {

    override fun areItemsTheSame(oldItem: Genre, newItem: Genre): Boolean =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: Genre, newItem: Genre): Boolean =
        oldItem.equals(newItem)
}