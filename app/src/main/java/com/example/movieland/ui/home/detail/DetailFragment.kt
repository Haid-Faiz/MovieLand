package com.example.movieland.ui.home.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.datasource.remote.models.requests.AddToWatchListRequest
import com.example.movieland.R
import com.example.movieland.databinding.FragmentDetailBsdBinding
import com.example.movieland.ui.home.HomeViewModel
import com.example.movieland.utils.Constants.GENRES_ID_LIST_KEY
import com.example.movieland.utils.Constants.IS_IT_A_MOVIE_KEY
import com.example.movieland.utils.Constants.MEDIA_ID_KEY
import com.example.movieland.utils.Constants.MEDIA_IMAGE_KEY
import com.example.movieland.utils.Constants.MEDIA_OVERVIEW_KEY
import com.example.movieland.utils.Constants.MEDIA_PLAY_REQUEST_KEY
import com.example.movieland.utils.Constants.MEDIA_RATING_KEY
import com.example.movieland.utils.Constants.MEDIA_RE_PLAY_REQUEST_KEY
import com.example.movieland.utils.Constants.MEDIA_SEND_REQUEST_KEY
import com.example.movieland.utils.Constants.MEDIA_TITLE_KEY
import com.example.movieland.utils.Constants.MEDIA_YEAR_KEY
import com.example.movieland.utils.Constants.MOVIE
import com.example.movieland.utils.Constants.TMDB_IMAGE_BASE_URL_W780
import com.example.movieland.utils.Constants.TV
import com.example.movieland.utils.Helpers
import com.example.movieland.utils.Resource
import com.example.movieland.utils.formatMediaDate
import com.example.movieland.utils.showSnackBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first

@AndroidEntryPoint
class DetailFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDetailBsdBinding? = null
    private val homeViewModel: HomeViewModel by viewModels()
    private val binding get() = _binding!!
    private var _mediaId: Int? = null
    private var _isItMovie = false
    private lateinit var popingAnim: Animation
    private lateinit var navController: NavController
    private var isExpanded: Boolean = false

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
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
        }
        popingAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.poping_anim)
        navController = findNavController()
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
                    tvGenres.text = Helpers.getMovieGenreListFromIds(genreList)
                        .joinToString("  •  ") { it.name }
                    posterImage.load(TMDB_IMAGE_BASE_URL_W780.plus(imgUrl))
                    titleText.text = title
                    releaseDate.formatMediaDate(year)
                    ratingText.text = rating
                    overviewText.text = overview
                }
            }
        }
    }

    private fun setUpClickListeners() = binding.apply {

        overviewText.setOnClickListener {
            if (!isExpanded) {
                // Expand it
                isExpanded = true
                overviewText.maxLines = 100
            } else {
                // Collapse it
                isExpanded = false
                overviewText.maxLines = 4
            }
        }

        closeDetailBtn.setOnClickListener { dismiss() }

        playButton.setOnClickListener {

            if (navController.previousBackStackEntry?.destination?.id?.toString() == R.id.playerFragment.toString() ||
                navController.previousBackStackEntry?.destination?.id?.toString() == R.id.castDetailsFragment.toString()
            ) {
                // Popup backstack up till PlayerFragment & reload data in player fragment
                sendReloadCallbackToPlayerFragment(_mediaId, _isItMovie)
            } else {
                parentFragmentManager.setFragmentResult(
                    MEDIA_PLAY_REQUEST_KEY,
                    bundleOf(
                        MEDIA_ID_KEY to _mediaId, // id & isItMovie working
                        IS_IT_A_MOVIE_KEY to _isItMovie
                    )
                )
                navController.navigate(R.id.action_detailFragment_to_playerFragment)
            }
        }

        addToWatchlistBtn.setOnClickListener {
            addToWatchlistBtn.startAnimation(popingAnim)
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {

                val sessionId = homeViewModel.getSessionId().first()
                val accountId = homeViewModel.getAccountId().first()
                if (sessionId != null && accountId != null) {
                    homeViewModel.addToWatchList(
                        accountId = accountId,
                        sessionId = sessionId,
                        addToWatchListRequest = AddToWatchListRequest(
                            mediaId = _mediaId!!,
                            mediaType = if (_isItMovie) MOVIE else TV,
                            watchlist = true
                        )
                    ).let { response ->
                        when (response) {
                            is Resource.Error -> showSnackBar(
                                response.message ?: "Something went wrong"
                            )
                            // is Resource.Loading -> TODO()
                            is Resource.Success -> showSnackBar("Added to My List")
                        }
                    }
                } else showSnackBar("Please login to avail this feature")
            }
        }
    }

    private fun sendReloadCallbackToPlayerFragment(_mediaId: Int?, _isItMovie: Boolean) {
        parentFragmentManager.setFragmentResult(
            MEDIA_RE_PLAY_REQUEST_KEY,
            bundleOf(
                MEDIA_ID_KEY to _mediaId, // id & isItMovie working
                IS_IT_A_MOVIE_KEY to _isItMovie
            )
        )
        navController.popBackStack(R.id.playerFragment, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
