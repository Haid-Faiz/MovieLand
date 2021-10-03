package com.example.movieland.ui.home

import com.example.datasource.remote.models.responses.MovieResult
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.movieland.databinding.ItemPosterBinding
import com.example.movieland.databinding.ItemPosterRatedBinding
import com.example.movieland.utils.Constants.TMDB_IMAGE_BASE_URL_W500

class RatedMoviesAdapter(
    private var onPosterClick: ((movieResult: MovieResult) -> Unit)? = null
) : ListAdapter<MovieResult, RatedMoviesAdapter.ViewHolder>(DiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ItemPosterRatedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemPosterRatedBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movieResult: MovieResult) = binding.apply {
            posterImage.load(TMDB_IMAGE_BASE_URL_W500.plus(movieResult.posterPath))
            posterImage.setOnClickListener {
                onPosterClick?.invoke(movieResult)
            }
            movieResult.ratingByYou?.let {
                yourRatingText.text = "$it"
            }
        }
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<MovieResult>() {

        override fun areItemsTheSame(oldItem: MovieResult, newItem: MovieResult): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: MovieResult, newItem: MovieResult): Boolean =
            oldItem.equals(newItem)
    }
}