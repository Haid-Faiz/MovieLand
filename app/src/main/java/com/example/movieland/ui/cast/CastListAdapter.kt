package com.example.movieland.ui.cast

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.datasource.remote.models.responses.Cast
import com.example.movieland.databinding.ItemCastBinding
import com.example.movieland.utils.Constants.TMDB_CAST_IMAGE_BASE_URL_W185

class CastListAdapter(
    val onCastItemClick: (cast: Cast) -> Unit
) : ListAdapter<Cast, CastListAdapter.ViewHolder>(DiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ItemCastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemCastBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cast: Cast) = binding.apply {
            crewPicture.load(TMDB_CAST_IMAGE_BASE_URL_W185.plus(cast.profilePath))
            crewRealName.text = cast.name
            crewCharacterName.text = cast.character
            root.setOnClickListener {
                onCastItemClick(cast)
            }
        }
    }

}

class DiffUtilCallback : DiffUtil.ItemCallback<Cast>() {

    override fun areItemsTheSame(oldItem: Cast, newItem: Cast): Boolean =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: Cast, newItem: Cast): Boolean =
        oldItem.equals(newItem)
}