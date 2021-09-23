package com.example.movieland.ui.home.detail

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.movieland.R
import com.example.movieland.databinding.FragmentDetailBsdBinding
import com.example.movieland.ui.coming_soon.GenreAdapter
import com.example.movieland.utils.Constants.TMDB_IMAGE_BASE_URL_W500
import com.example.movieland.utils.Constants.TMDB_IMAGE_BASE_URL_W780
import com.example.movieland.utils.Helpers
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.ArrayList

@AndroidEntryPoint
class DetailFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDetailBsdBinding? = null
    private val binding get() = _binding!!
    private var videoUrl: String? = null
    private lateinit var genreAdapter: GenreAdapter
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

//    override fun getTheme(): Int {
//        return R.style.AppBottomSheetDialog
//    }

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val dialog = BottomSheetDialog(requireContext(), R.style.Theme_MovieLand)
//        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
//        return dialog
//    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpGenreRecyclerView()
        setUpFragmentResultListeners()
        setUpClickListeners()
    }

    private fun setUpFragmentResultListeners() {
        parentFragmentManager.setFragmentResultListener(
            "home_movie_key",
            viewLifecycleOwner
        ) { _, bundle ->

            bundle.apply {
                movieId = getInt("movie_id")
                isMovie = getBoolean("isMovie")

                val genreList: ArrayList<Int>? = getIntegerArrayList("genre_ids_list")
                val title = getString("movie_title")
                val overview = getString("movie_overview")
                val imgUrl = getString("movie_image_url")
                val year = getString("movie_year")

                binding.apply {
                    genreAdapter.submitList(Helpers.getGenreListFromIds(genreList))
                    posterImage.load(TMDB_IMAGE_BASE_URL_W780.plus(imgUrl))
                    titleText.text = title
                    overviewText.text = overview
                    releaseDate.text = year
                }
            }
        }
    }

    private fun setUpGenreRecyclerView() {
        genreAdapter = GenreAdapter(ContextCompat.getColor(requireContext(), R.color.dark_gray))
        binding.rvGenres.setHasFixedSize(true)
        binding.rvGenres.adapter = genreAdapter
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