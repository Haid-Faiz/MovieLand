package com.example.movieland.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.datasource.remote.models.responses.MovieResult
import com.example.movieland.data.models.HomeFeed
import com.example.movieland.databinding.ItemFeedHorizontalListBinding

class HomeAdapter(
    private var onPosterClick: (movieResult: MovieResult) -> Unit
) : ListAdapter<HomeFeed, HomeAdapter.ViewHolder>(DiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ItemFeedHorizontalListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

//    fun updateList(homeFeed: HomeFeed) {
//        val oldList: ArrayList<HomeFeed> = ArrayList()
//        oldList.addAll(currentList)
//        Log.d("CurrentList", "updateList: $oldList")
//        oldList.add(homeFeed)
//        submitList(oldList)
//    }

    inner class ViewHolder(private val binding: ItemFeedHorizontalListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(homeFeed: HomeFeed) = binding.apply {
            title.text = homeFeed.title
            // Setting up inner horizontal recyclerview
            recyclerviewPostersList.setHasFixedSize(true)
            HorizontalAdapter(onPosterClick = onPosterClick).let {
                recyclerviewPostersList.adapter = it
                it.submitList(homeFeed.list)
            }
        }
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<HomeFeed>() {

        override fun areItemsTheSame(oldItem: HomeFeed, newItem: HomeFeed): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: HomeFeed, newItem: HomeFeed): Boolean =
            oldItem.equals(newItem)
    }

}