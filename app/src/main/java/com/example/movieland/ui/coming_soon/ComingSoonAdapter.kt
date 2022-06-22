package com.example.movieland.ui.coming_soon

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.datasource.remote.models.responses.MovieResult
import com.example.movieland.databinding.ItemComingSoonBinding
import com.example.movieland.utils.Constants

class ComingSoonAdapter(
    private var onImageClick: (movieResult: MovieResult) -> Unit,
    private var onFirstLoad: (movieResult: MovieResult) -> Unit,
) : ListAdapter<MovieResult, ComingSoonAdapter.ViewHolder>(DiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ItemComingSoonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0)
            onFirstLoad(getItem(0))
        holder.bind(getItem(position))
    }

    fun getSelectedItem(position: Int): MovieResult = getItem(position)

    inner class ViewHolder(
        private val binding: ItemComingSoonBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movieResult: MovieResult) = binding.apply {
            movieImage.load(Constants.TMDB_IMAGE_BASE_URL_W780.plus(movieResult.posterPath))
            this.root.setOnClickListener {
                onImageClick(movieResult)
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
