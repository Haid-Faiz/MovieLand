package com.example.movieland.ui.home.detail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.movieland.R
import com.example.movieland.databinding.FragmentDetailBsdBinding
import com.example.movieland.ui.home.HomeViewModel
import com.example.movieland.utils.Constants.TMDB_IMAGE_BASE_URL_W500
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class DetailFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDetailBsdBinding? = null
    private val binding get() = _binding!!
    private var videoUrl: String? = null
    private var movieId: Int? = null
    private var isMovie = false

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

        parentFragmentManager.setFragmentResultListener(
            "home_movie_key",
            viewLifecycleOwner
        ) { _, bundle ->

            bundle.apply {
                movieId = getInt("movie_id")
                isMovie = getBoolean("isMovie")
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
        setUpClickListeners()
    }

    private fun setUpClickListeners() {
        binding.closeDetailBtn.setOnClickListener { dismiss() }

        binding.playButton.setOnClickListener {
            Log.d("Movieid", "setUpClickListeners: $movieId")
            parentFragmentManager.setFragmentResult(
                "video_request",
                bundleOf(
                    "movie_id" to movieId,
                    "isMovie" to isMovie
                )
            )
            findNavController().navigate(R.id.action_detailFragment_to_playerFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}