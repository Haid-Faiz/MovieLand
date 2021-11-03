package com.example.movieland.ui.home

import android.graphics.Color
import android.os.Bundle
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
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.datasource.remote.models.requests.AddToWatchListRequest
import com.example.datasource.remote.models.responses.MovieResult
import com.example.movieland.R
import com.example.movieland.databinding.FragmentHomeBinding
import com.example.movieland.utils.*
import com.example.movieland.utils.Constants.GENRES_ID_LIST_KEY
import com.example.movieland.utils.Constants.IS_IT_A_MOVIE_KEY
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
import com.example.movieland.utils.Constants.TRENDING_MOVIES
import com.example.movieland.utils.Constants.TRENDING_TV_SHOWS
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlin.math.min

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by activityViewModels()
    private var _binding: FragmentHomeBinding? = null
    private lateinit var navController: NavController

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var homeAdapter: HomeAdapter
    private var _bannerMovie: MovieResult? = null
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

        viewModel.allFeedList.observe(viewLifecycleOwner) {
            when (it) {
                // is Resource.Loading -> loading done automatically
                is Resource.Error -> {
                    showSnackBar(
                        message = it.message!!,
                        length = Snackbar.LENGTH_INDEFINITE,
                        actionMsg = "Retry"
                    ) { viewModel.retry() }
                }
                is Resource.Success -> binding.apply {

                    homeAdapter.submitList(it.data!!.homeFeedList)
                    // Setup movie banner
                    _bannerMovie = it.data.bannerMovie
                    binding.bannerImage.load("${TMDB_IMAGE_BASE_URL_W780}${_bannerMovie!!.posterPath}")
                    binding.bannerGenres.text = Helpers.getMovieGenreListFromIds(
                        _bannerMovie!!.genreIds
                    ).joinToString(" • ") { it.name }

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
            val action =
                HomeFragmentDirections.actionNavigationHomeToMovieListFragment(mediaCategory = TRENDING_MOVIES)
            navController.navigate(action)
        }

        tvShowsText.setOnClickListener {
            val action =
                HomeFragmentDirections.actionNavigationHomeToMovieListFragment(mediaCategory = TRENDING_TV_SHOWS)
            navController.navigate(action)
        }

        genresText.setOnClickListener {
            safeFragmentNavigation(
                navController = navController,
                currentFragmentId = R.id.navigation_home,
                actionId = R.id.action_navigation_home_to_selectGenresDialogFragment
            )
        }

        bannerInfoButton.setOnClickListener {
            _bannerMovie?.let { movie ->
                openMediaDetailsBSD(movie)
            }
        }

        addToListButton.setOnClickListener {
            _bannerMovie?.let { movie ->
                addToListButton.startAnimation(popingAnim)
                viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                    val sessionId = viewModel.getSessionId().first()
                    val accountId = viewModel.getAccountId().first()

                    if (sessionId != null && accountId != null) {
                        viewModel.addToWatchList(
                            accountId = accountId,
                            sessionId = sessionId,
                            addToWatchListRequest = AddToWatchListRequest(
                                mediaId = movie.id,
                                mediaType = MOVIE,
                                watchlist = true
                            )
                        ).let { response ->
                            when (response) {
                                is Resource.Error -> showSnackBar(
                                    response.message!!
                                )
                                // is Resource.Loading -> TODO()
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
        }

        playButton.setOnClickListener {
            _bannerMovie?.let { movie ->
                parentFragmentManager.setFragmentResult(
                    MEDIA_PLAY_REQUEST_KEY,
                    bundleOf(
                        MEDIA_ID_KEY to movie.id,
                        IS_IT_A_MOVIE_KEY to true
                    )
                )
                navController.navigate(R.id.action_navigation_home_to_playerFragment)
            }
        }
    }

    private fun openMediaDetailsBSD(media: MovieResult) {
        parentFragmentManager.setFragmentResult(
            MEDIA_SEND_REQUEST_KEY,
            bundleOf(
                MEDIA_ID_KEY to media.id,
                GENRES_ID_LIST_KEY to media.genreIds,
                MEDIA_TITLE_KEY to (media.title ?: media.tvShowName),
                MEDIA_OVERVIEW_KEY to media.overview,
                IS_IT_A_MOVIE_KEY to !media.title.isNullOrEmpty(),
                MEDIA_IMAGE_KEY to media.backdropPath,
                MEDIA_YEAR_KEY to (media.releaseDate ?: media.tvShowFirstAirDate),
                MEDIA_RATING_KEY to String.format("%.1f", media.voteAverage)
            )
        )

        safeFragmentNavigation(
            navController = navController,
            currentFragmentId = R.id.navigation_home,
            actionId = R.id.action_navigation_home_to_detailFragment
        )
    }

    private fun setUpRecyclerView() {
        homeAdapter = HomeAdapter(
            onPosterClick = {
                openMediaDetailsBSD(it)
            },
            onSeeAllBtnClick = {
                val action =
                    HomeFragmentDirections.actionNavigationHomeToMovieListFragment(mediaCategory = it)
                navController.navigate(action)
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
