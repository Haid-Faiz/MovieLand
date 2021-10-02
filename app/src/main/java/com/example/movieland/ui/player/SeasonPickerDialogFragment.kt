package com.example.movieland.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.example.datasource.remote.models.responses.Season
import com.example.movieland.R
import com.example.movieland.databinding.FragmentPlayerBinding
import com.example.movieland.databinding.SeasonPickerDialogBinding
import com.example.movieland.ui.player.adapters.SeasonPickerAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SeasonPickerDialogFragment : BottomSheetDialogFragment() {

    private var _binding: SeasonPickerDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SeasonPickerAdapter

    override fun getTheme(): Int {
        return R.style.FullScreenDialog
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SeasonPickerDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parentFragmentManager.setFragmentResultListener(
            "seasons_list_request_key",
            viewLifecycleOwner
        ) { _, bundle ->

            adapter =
                SeasonPickerAdapter(bundle.getInt("selected_item_position")) { seasonNumber: Int ->
                    // Callback of season options click
                    parentFragmentManager.setFragmentResult(
                        "options_selected_item_position",
                        bundleOf(
                            "season_number" to seasonNumber
                        )
                    )
                    findNavController().navigateUp()
                }
            val seasonList = bundle.getParcelableArrayList<Season>("season_list")
            setUpRecyclerView()
            adapter.submitList(seasonList)
        }

        binding.closeIcon.setOnClickListener { dismiss() }
    }

    private fun setUpRecyclerView() {
        binding.rvSeasonsOptionsList.setHasFixedSize(true)
        binding.rvSeasonsOptionsList.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}