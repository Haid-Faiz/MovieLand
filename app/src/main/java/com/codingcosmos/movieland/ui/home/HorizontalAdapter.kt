package com.codingcosmos.movieland.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.codingcosmos.datasource.remote.models.responses.MovieResult
import com.codingcosmos.movieland.databinding.ItemPosterBinding
import com.codingcosmos.movieland.databinding.ItemPosterListBinding
import com.codingcosmos.movieland.utils.Constants.TMDB_POSTER_IMAGE_BASE_URL_W342

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
            posterImage.load(TMDB_POSTER_IMAGE_BASE_URL_W342.plus(movieResult.posterPath))
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

class RecommendationsAdapter(
    private var onPosterClick: ((movieResult: MovieResult) -> Unit)? = null
) : RecyclerView.Adapter<RecommendationsAdapter.ViewHolder>() {

    private var _list: MutableList<MovieResult> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ItemPosterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(_list.get(position))
    }

    override fun getItemCount(): Int = _list.size

    inner class ViewHolder(
        private val binding: ItemPosterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movieResult: MovieResult) = binding.apply {
            posterImage.load(TMDB_POSTER_IMAGE_BASE_URL_W342.plus(movieResult.posterPath))
            ratingText.text = String.format("%.1f", movieResult.voteAverage)
            posterImage.setOnClickListener { onPosterClick?.invoke(movieResult) }
        }
    }

    fun submitList(list: List<MovieResult>) {
        _list = list as MutableList<MovieResult>
        notifyDataSetChanged()
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
            posterImage.load(TMDB_POSTER_IMAGE_BASE_URL_W342.plus(movieResult.posterPath))
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

class MediaListPagerAdapter(
    private var onPosterClick: ((movieResult: MovieResult) -> Unit)? = null,
) : PagingDataAdapter<MovieResult, MediaListPagerAdapter.ViewHolder>(DiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ItemPosterListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }

    inner class ViewHolder(
        private val binding: ItemPosterListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movieResult: MovieResult) = binding.apply {
            posterImage.load(TMDB_POSTER_IMAGE_BASE_URL_W342.plus(movieResult.posterPath))
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
