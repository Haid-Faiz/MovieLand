package com.example.movieland.ui.home

import com.example.datasource.remote.models.responses.MovieResult
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import coil.load
import com.example.movieland.databinding.ItemPosterBinding
import com.example.movieland.utils.Constants.TMDB_IMAGE_BASE_URL_W500

class HorizontalAdapter(
    private var onPosterClick: ((movieResult: MovieResult) -> Unit)? = null
) : ListAdapter<MovieResult, HorizontalAdapter.ViewHolder>(DiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ItemPosterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemPosterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movieResult: MovieResult) = binding.apply {
            posterImage.load(TMDB_IMAGE_BASE_URL_W500.plus(movieResult.posterPath))
            ratingText.text = String.format("%.1f", movieResult.voteAverage)
            posterImage.setOnClickListener { onPosterClick?.invoke(movieResult) }
        }
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<MovieResult>() {

        override fun areItemsTheSame(oldItem: MovieResult, newItem: MovieResult): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: MovieResult, newItem: MovieResult): Boolean =
            oldItem.equals(newItem)
    }
}


class HorizontalPagerAdapter(
    private var onPosterClick: ((movieResult: MovieResult) -> Unit)? = null,
) : PagingDataAdapter<MovieResult, HorizontalPagerAdapter.ViewHolder>(DiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ItemPosterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }

    inner class ViewHolder(
        private val binding: ItemPosterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movieResult: MovieResult) = binding.apply {
            posterImage.load(TMDB_IMAGE_BASE_URL_W500.plus(movieResult.posterPath))
            ratingText.text = String.format("%.1f", movieResult.voteAverage)
            posterImage.setOnClickListener {
                onPosterClick?.invoke(movieResult)
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