package com.codingcosmos.movieland.ui.player.adapters

import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codingcosmos.datasource.remote.models.responses.Season
import com.codingcosmos.movieland.R
import com.codingcosmos.movieland.databinding.ItemPickerOptionBinding

class SeasonPickerAdapter(
    private val selectedItemPosition: Int,
    private val itemClick: (seasonNumber: Int) -> Unit
) :
    ListAdapter<Season, SeasonPickerAdapter.ViewHolder>(SeasonPickerDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ItemPickerOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), selectedItemPosition, position)
    }

    inner class ViewHolder(private val binding: ItemPickerOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(season: Season, selectedItemPosition: Int, position: Int) = binding.apply {
            seasonText.text = season.name
            // Now highlight the selected item
            val isSelected = selectedItemPosition == position

            seasonText.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isSelected) R.color.text_primary else R.color.text_secondary
                )
            )
            seasonText.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (isSelected) 20.0f else 18.0f)
            seasonText.setTypeface(
                seasonText.typeface,
                if (isSelected) Typeface.BOLD else Typeface.NORMAL
            )

            rootLayout.setOnClickListener { itemClick(season.seasonNumber) }
        }
    }

    class SeasonPickerDiffUtil : DiffUtil.ItemCallback<Season>() {
        override fun areItemsTheSame(oldItem: Season, newItem: Season): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Season, newItem: Season): Boolean {
            return oldItem.equals(newItem)
        }
    }
}
