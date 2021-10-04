package com.example.movieland.ui.home.detail

import android.os.Bundle
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
import com.example.movieland.utils.Constants.GENRES_ID_LIST_KEY
import com.example.movieland.utils.Constants.IS_IT_A_MOVIE_KEY
import com.example.movieland.utils.Constants.MEDIA_ID_KEY
import com.example.movieland.utils.Constants.MEDIA_IMAGE_KEY
import com.example.movieland.utils.Constants.MEDIA_OVERVIEW_KEY
import com.example.movieland.utils.Constants.MEDIA_SEND_REQUEST_KEY
import com.example.movieland.utils.Constants.MEDIA_TITLE_KEY
import com.example.movieland.utils.Constants.MEDIA_YEAR_KEY
import com.example.movieland.utils.Constants.MEDIA_PLAY_REQUEST_KEY
import com.example.movieland.utils.Constants.MEDIA_RATING_KEY
import com.example.movieland.utils.Constants.TMDB_IMAGE_BASE_URL_W780
import com.example.movieland.utils.Helpers
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.ArrayList

@AndroidEntryPoint
class DetailFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDetailBsdBinding? = null
    private val binding get() = _binding!!
    private lateinit var genreAdapter: GenreAdapter
    private var _mediaId: Int? = null
    private var _isItMovie = false

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
            MEDIA_SEND_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            bundle.apply {
                _mediaId = getInt(MEDIA_ID_KEY)
                _isItMovie = getBoolean(IS_IT_A_MOVIE_KEY)

                val genreList: ArrayList<Int>? = getIntegerArrayList(GENRES_ID_LIST_KEY)
                val title = getString(MEDIA_TITLE_KEY)
                val overview = getString(MEDIA_OVERVIEW_KEY)
                val imgUrl = getString(MEDIA_IMAGE_KEY)
                val year = getString(MEDIA_YEAR_KEY)
                val rating = getString(MEDIA_RATING_KEY)

                binding.apply {
                    genreAdapter.submitList(Helpers.getMovieGenreListFromIds(genreList))
                    posterImage.load(TMDB_IMAGE_BASE_URL_W780.plus(imgUrl))
                    titleText.text = title
                    overviewText.text = overview
                    releaseDate.text = year
                    ratingText.text = rating
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
            parentFragmentManager.setFragmentResult(
                MEDIA_PLAY_REQUEST_KEY,
                bundleOf(
                    MEDIA_ID_KEY to _mediaId,    // id & isItMovie working
                    IS_IT_A_MOVIE_KEY to _isItMovie
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