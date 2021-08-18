package com.example.movieland.ui.home.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import coil.load
import com.example.movieland.databinding.FragmentDetailBsdBinding
import com.example.movieland.ui.home.HomeViewModel
import com.example.movieland.utils.Constants.TMDB_IMAGE_BASE_URL_W500
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDetailBsdBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBsdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.closeDetailBtn.setOnClickListener {
            dismiss()
        }

        parentFragmentManager.setFragmentResultListener(
            "home_movie_key",
            viewLifecycleOwner
        ) { key, bundle ->

            bundle.apply {
                val title = getString("movie_title")
                val overview = getString("movie_overview")
                val imgUrl = getString("movie_image_url")
                val year = getString("movie_year")

                binding.apply {
                    posterImage.load(TMDB_IMAGE_BASE_URL_W500.plus(imgUrl))
                    titleText.text = title
                    overviewText.text = overview
                    releaseDate.text = year
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}