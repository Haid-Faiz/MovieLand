package com.example.movieland.ui.auth

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.movieland.R
import com.example.movieland.databinding.IntroPagerItemBinding

class IntroPagerAdapter : RecyclerView.Adapter<IntroPagerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        IntroPagerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = 3

    class ViewHolder(
        private val binding: IntroPagerItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) = binding.apply {
            when (position) {
                0 -> {
                    introTitle.text = "Trailers & info of millions of movies, Tv shows"
                    introDescription.text = "Every information of any movie/show at no price."
                    introImage.load(R.drawable.intro_1)
                }
                1 -> {
                    introTitle.text = "Search and discover Movies, TV shows"
                    introDescription.text = "Search or discover movie/show by genres, get cast, find what's trendy from millions of data"
                    introImage.load(R.drawable.avengers_red)
                }
                2 -> {
                    introTitle.text = "Liked something... want to watch it later ?"
                    introDescription.text = "Create your own watchlist, favourite list, or rate any movie/show"
                    introImage.load(R.drawable.fast_saga)
                }
                else -> {

                }
            }
        }
    }

}