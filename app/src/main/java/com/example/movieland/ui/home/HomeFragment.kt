package com.example.movieland.ui.home

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.datasource.remote.models.requests.AddToWatchListRequest
import com.example.datasource.remote.models.responses.MovieResult
import com.example.movieland.R
import com.example.movieland.databinding.FragmentHomeBinding
import com.example.movieland.utils.Constants.GENRES_ID_LIST_KEY
import com.example.movieland.utils.Constants.IS_IT_A_MOVIE_KEY
import com.example.movieland.utils.Constants.MEDIA_CATEGORY_SEND_REQUEST_KEY
import com.example.movieland.utils.Constants.MEDIA_ID_KEY
import com.example.movieland.utils.Constants.MEDIA_IMAGE_KEY
import com.example.movieland.utils.Constants.MEDIA_OVERVIEW_KEY
import com.example.movieland.utils.Constants.MEDIA_PLAY_REQUEST_KEY
import com.example.movieland.utils.Constants.MEDIA_RATING_KEY
import com.example.movieland.utils.Constants.MEDIA_SEND_REQUEST_KEY
import com.example.movieland.utils.Constants.MEDIA_TITLE_KEY
import com.example.movieland.utils.Constants.MEDIA_YEAR_KEY
import com.example.movieland.utils.Constants.MOVIE
import com.example.movieland.utils.Constants.TMDB_IMAGE_BASE_URL_W780
import com.example.movieland.utils.Helpers
import com.example.movieland.utils.Resource
import com.example.movieland.utils.safeFragmentNavigation
import com.example.movieland.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlin.math.min

// Multi clicks on dialog crash handling
//

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by activityViewModels()
    private var _binding: FragmentHomeBinding? = null
    private lateinit var navController: NavController

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var homeAdapter: HomeAdapter
    private lateinit var bannerMovie: MovieResult
    private lateinit var popingAnim: Animation

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        popingAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.poping_anim)
        setUpRecyclerView()
        setUpClickListeners()

        homeViewModel.allFeedList.observe(viewLifecycleOwner) {
//            (it is Resource.Loading).let {
//                binding.apply {
//                    addToListButton.isEnabled = !it
//                    bannerInfoButton.isEnabled = !it
//                    playButton.isEnabled = !it
//                }
//            }
            when (it) {
                is Resource.Error -> TODO()
                is Resource.Success -> {
                    // Setup movie banner
                    bannerMovie = it.data!![0].list[0]
                    binding.bannerImage.load("${TMDB_IMAGE_BASE_URL_W780}${bannerMovie.posterPath}")
                    binding.bannerGenres.text = Helpers.getMovieGenreListFromIds(
                        bannerMovie.genreIds
                    ).joinToString(" • ") { it.name }

                    homeAdapter.submitList(it.data)
                    // Show RV
                    binding.rvPlaceholder.isGone = true
                    binding.rvHomeFeed.isGone = false
                }
            }
        }

        binding.nestedScrollview.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            val color = changeAppBarAlpha(
                ContextCompat.getColor(requireContext(), R.color.black_transparent),
                (min(255, scrollY).toFloat() / 255.0f).toDouble()
            )
            binding.appBarLayout.setBackgroundColor(color)
        }
    }

    private fun setUpClickListeners() = binding.apply {
        moviesText.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                MEDIA_CATEGORY_SEND_REQUEST_KEY,
                bundleOf(IS_IT_A_MOVIE_KEY to true)
            )
            navController.navigate(R.id.action_navigation_home_to_movieListFragment)
        }

        tvShowsText.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                MEDIA_CATEGORY_SEND_REQUEST_KEY,
                bundleOf(IS_IT_A_MOVIE_KEY to false)
            )
            navController.navigate(R.id.action_navigation_home_to_movieListFragment)
        }

        genresText.setOnClickListener {
            safeFragmentNavigation(
                navController = navController,
                currentFragmentId = R.id.navigation_home,
                actionId = R.id.action_navigation_home_to_selectGenresDialogFragment
            )
        }

        bannerInfoButton.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                MEDIA_SEND_REQUEST_KEY,
                bundleOf(
                    MEDIA_ID_KEY to bannerMovie.id,
                    GENRES_ID_LIST_KEY to bannerMovie.genreIds,
                    MEDIA_TITLE_KEY to (bannerMovie.title ?: bannerMovie.tvShowName),
                    MEDIA_OVERVIEW_KEY to bannerMovie.overview,
                    IS_IT_A_MOVIE_KEY to !bannerMovie.title.isNullOrEmpty(),
                    MEDIA_IMAGE_KEY to bannerMovie.backdropPath,
                    MEDIA_YEAR_KEY to (bannerMovie.releaseDate ?: bannerMovie.tvShowFirstAirDate),
                    MEDIA_RATING_KEY to String.format("%.1f", bannerMovie.voteAverage)
                )
            )
            safeFragmentNavigation(
                navController = navController,
                currentFragmentId = R.id.navigation_home,
                actionId = R.id.action_navigation_home_to_detailFragment
            )
        }

        addToListButton.setOnClickListener {
            addToListButton.startAnimation(popingAnim)
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {

                val sessionId = homeViewModel.getSessionId().first()
                val accountId = homeViewModel.getAccountId().first()
                if (sessionId != null && accountId != null) {
                    homeViewModel.addToWatchList(
                        accountId = accountId,
                        sessionId = sessionId,
                        addToWatchListRequest = AddToWatchListRequest(
                            mediaId = bannerMovie.id,
                            mediaType = MOVIE,
                            watchlist = true
                        )
                    ).let { response ->
                        when (response) {
                            is Resource.Error -> showSnackBar(
                                response.message ?: "Something went wrong"
                            )
//                        is Resource.Loading -> TODO()
                            is Resource.Success -> showSnackBar("Added to My List")
                        }
                    }

                } else
                    showSnackBar(
                        "Please login to avail this feature",
                        action = {
                            navController.navigate(R.id.action_navigation_home_to_navigation_account)
                        },
                        actionMsg = "Login"
                    )


            }
        }

        playButton.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                MEDIA_PLAY_REQUEST_KEY,
                bundleOf(
                    MEDIA_ID_KEY to bannerMovie.id,
                    IS_IT_A_MOVIE_KEY to true
                )
            )
            navController.navigate(R.id.action_navigation_home_to_playerFragment)
        }
    }

    private fun setUpRecyclerView() {
        homeAdapter = HomeAdapter(
            onPosterClick = {
                parentFragmentManager.setFragmentResult(
                    MEDIA_SEND_REQUEST_KEY,
                    bundleOf(
                        GENRES_ID_LIST_KEY to it.genreIds,
                        MEDIA_TITLE_KEY to (it.title ?: it.tvShowName),
                        IS_IT_A_MOVIE_KEY to !it.title.isNullOrEmpty(),
                        MEDIA_OVERVIEW_KEY to it.overview,
                        MEDIA_IMAGE_KEY to it.backdropPath,
                        MEDIA_YEAR_KEY to (it.releaseDate ?: it.tvShowFirstAirDate),
                        MEDIA_ID_KEY to it.id,
                        MEDIA_RATING_KEY to String.format("%.1f", it.voteAverage)
                    )
                )
                safeFragmentNavigation(
                    navController = navController,
                    currentFragmentId = R.id.navigation_home,
                    actionId = R.id.action_navigation_home_to_detailFragment
                )
            },
            onBollywoodPosterClick = {
                parentFragmentManager.setFragmentResult(
                    MEDIA_PLAY_REQUEST_KEY,
                    bundleOf(
                        MEDIA_ID_KEY to it.id,
                        IS_IT_A_MOVIE_KEY to true
                    )
                )
                safeFragmentNavigation(
                    navController = navController,
                    currentFragmentId = R.id.navigation_home,
                    actionId = R.id.action_navigation_home_to_playerFragment
                )
            }
        )
        binding.rvHomeFeed.setHasFixedSize(true)
        binding.rvHomeFeed.adapter = homeAdapter
    }

    private fun changeAppBarAlpha(color: Int, fraction: Double): Int {
        val red: Int = Color.red(color)
        val green: Int = Color.green(color)
        val blue: Int = Color.blue(color)
        val alpha: Int = (Color.alpha(color) * fraction).toInt()
        return Color.argb(alpha, red, green, blue)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}