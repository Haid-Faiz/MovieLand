package com.example.movieland.ui.auth

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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

                }
                1 -> {

                }
                2 -> {

                }
                else -> {

                }
            }
        }
    }

}