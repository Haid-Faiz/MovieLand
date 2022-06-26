package com.codingcosmos.movieland.ui.player.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codingcosmos.datasource.remote.models.responses.VideoResult
import com.codingcosmos.movieland.databinding.ItemVideoBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback

class MoreVideosAdapter :
    ListAdapter<VideoResult, MoreVideosAdapter.ViewHolder>(DiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(val binding: ItemVideoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(videoResult: VideoResult?) = binding.apply {
            titleText.text = videoResult?.name
            youtubePlayerView.getYouTubePlayerWhenReady(
                youTubePlayerCallback = object :
                    YouTubePlayerCallback {
                    override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                        youTubePlayer.cueVideo(videoId = videoResult!!.key, 0f)
                    }
                }
            )
        }
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<VideoResult>() {
        override fun areItemsTheSame(oldItem: VideoResult, newItem: VideoResult): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: VideoResult, newItem: VideoResult): Boolean {
            return oldItem.equals(newItem)
        }
    }
}
