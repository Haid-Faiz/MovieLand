package com.example.movieland.data.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.movieland.databinding.PageStateItemBinding

class PagingStateAdapter(val retry: () -> Unit) :
    LoadStateAdapter<PagingStateAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ViewHolder {
        return ViewHolder(
            PageStateItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    inner class ViewHolder(private val binding: PageStateItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(loadState: LoadState) = binding.apply {
            retryButton.setOnClickListener { retry() }
            when (loadState) {
                LoadState.Loading -> {
//                    loadMsg.text = "Hang on... loading content"
                    progressBar.isGone = false
                    retryButton.isGone = true
                }
                // is LoadState.NotLoading -> TODO()
                is LoadState.Error -> {
//                    loadMsg.text = "Oops... Could not load content"
                    progressBar.isGone = true
                    retryButton.isGone = false
                }
            }
        }
    }
}
