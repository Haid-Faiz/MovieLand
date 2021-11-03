package com.example.movieland.ui.player.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.datasource.remote.models.responses.Episode
import com.example.movieland.databinding.ItemEpisodeBinding
import com.example.movieland.utils.Constants.TMDB_IMAGE_BASE_URL_W500

class TvShowEpisodesAdapter(
    private val addToWatchListClick: (episode: Episode) -> Unit
) :
    ListAdapter<Episode, TvShowEpisodesAdapter.ViewHolder>(TvShowEpisodesDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ItemEpisodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemEpisodeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(episode: Episode) = binding.apply {
            // Removing '/' from starting of stillPath, because we already have it in Base Url
            stillImage.load(TMDB_IMAGE_BASE_URL_W500.plus(episode.stillPath?.removePrefix("/")))
            overviewText.text = episode.overview
            nameText.text = episode.name
            ratingText.text = String.format("%.1f", episode.voteAverage)
            addToListImg.setOnClickListener {
                addToWatchListClick(episode)
            }
        }
    }

    class TvShowEpisodesDiffUtil : DiffUtil.ItemCallback<Episode>() {
        override fun areItemsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem.equals(newItem)
        }
    }
}
