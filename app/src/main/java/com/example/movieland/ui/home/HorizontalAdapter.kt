package com.example.movieland.ui.home

import android.util.Log
import com.example.datasource.remote.models.responses.Result
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.movieland.databinding.ItemPosterBinding
import com.example.movieland.utils.Constants.TMDB_IMAGE_BASE_URL_W500

class HorizontalAdapter(
    private var onPosterClick: (result: Result) -> Unit
) : ListAdapter<Result, HorizontalAdapter.ViewHolder>(DiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ItemPosterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemPosterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(result: Result) = binding.apply {
            posterImage.load(TMDB_IMAGE_BASE_URL_W500.plus(result.posterPath))
            posterImage.setOnClickListener {
                onPosterClick(result)
            }
        }
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<Result>() {

        override fun areItemsTheSame(oldItem: Result, newItem: Result): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: Result, newItem: Result): Boolean =
            oldItem.equals(newItem)
    }
}