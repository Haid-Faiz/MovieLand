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
                    introTitle.text = "Unlimited entertainment, one low price."
                    introDescription.text = "Everything on Netflix, starting at just Rs 199"
                    introImage.load(R.drawable.intro_1)
                }
                1 -> {
                    introTitle.text = "Don't forget to save your favourite movie."
                    introDescription.text = "Access saved movies without internet."
                    introImage.load(R.drawable.avengers_red)
                }
                2 -> {
                    introTitle.text = "Rate movies, comment on movies."
                    introDescription.text = "It's a world of movies"
                    introImage.load(R.drawable.avengers_blue)
                }
                else -> {

                }
            }
        }
    }

}