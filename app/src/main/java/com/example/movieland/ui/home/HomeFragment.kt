package com.example.movieland.ui.home

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.datasource.remote.models.responses.MovieResult
import com.example.movieland.R
import com.example.movieland.data.models.HomeFeed
import com.example.movieland.databinding.FragmentHomeBinding
import com.example.movieland.utils.Constants.TMDB_IMAGE_BASE_URL_W780
import com.example.movieland.utils.Resource
import com.example.movieland.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.min

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null
    private lateinit var navController: NavController

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var homeAdapter: HomeAdapter
    private var feedList: ArrayList<HomeFeed>? = null
    private lateinit var bannerMovie: MovieResult

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
        setUpRecyclerView()
        setUpClickListeners()

        homeViewModel.getNowPlayingMovies()
        homeViewModel.movieListNowPlaying.observe(viewLifecycleOwner) {
            Log.d("TAGObserver1", "onViewCreated: Called")
            when (it) {
                is Resource.Error -> {
                }
                is Resource.Loading -> TODO()
                is Resource.Success -> {
                    bannerMovie = it.data!!.movieResults[0]
                    binding.bannerImage.load("${TMDB_IMAGE_BASE_URL_W780}${bannerMovie.posterPath}")

                    feedList = ArrayList()
                    feedList!!.add(HomeFeed("Now Playing Movies", it.data.movieResults))
                    homeViewModel.getTopRatedMovies()
                }
            }
        }

        homeViewModel.movieListTopRated.observe(viewLifecycleOwner) {
            Log.d("TAGObserver2", "onViewCreated: Called")
            when (it) {
                is Resource.Error -> {
                }
                is Resource.Loading -> TODO()
                is Resource.Success -> {
                    feedList?.add(HomeFeed("Top Rated Movies", it.data!!.movieResults))
                    homeViewModel.getPopularMovies()
                }
            }
        }

        homeViewModel.movieListPopular.observe(viewLifecycleOwner) {
            Log.d("TAGObserver3", "onViewCreated: Called")
            when (it) {
                is Resource.Error -> {
                }
                is Resource.Loading -> TODO()
                is Resource.Success -> {
                    feedList?.add(HomeFeed("Popular Movies", it.data!!.movieResults))
                    homeViewModel.getPopularTvShows()
                }
            }
        }

        homeViewModel.tvListPopular.observe(viewLifecycleOwner) {
            Log.d("TAGObserver4", "onViewCreated: Called")
            when (it) {
                is Resource.Error -> {
                }
                is Resource.Loading -> {
                }
                is Resource.Success -> {
                    feedList?.add(HomeFeed("Popular Tv Shows", it.data!!.movieResults))
                    homeViewModel.getTrendingMovies()
                }
            }
        }

        homeViewModel.movieListTrending.observe(viewLifecycleOwner) {
            Log.d("TAGObserver5", "onViewCreated: Called")
            when (it) {
                is Resource.Error -> {
                }
                is Resource.Loading -> TODO()
                is Resource.Success -> {
                    feedList?.add(HomeFeed("Trending Movies", it.data!!.movieResults))
                    homeViewModel.getTrendingTvShows()
                }
            }
        }

        homeViewModel.tvListTrending.observe(viewLifecycleOwner) {
            Log.d("TAGObserver6", "onViewCreated: Called")
            when (it) {
                is Resource.Error -> {
                }
                is Resource.Loading -> {
                }
                is Resource.Success -> {
                    feedList?.add(HomeFeed("Trending Tv Shows", it.data!!.movieResults))
                    homeViewModel.getUpcomingMovies()
                }
            }
        }

        homeViewModel.movieListUpcoming.observe(viewLifecycleOwner) {
            Log.d("TAGObserver7", "onViewCreated: Called")
            when (it) {
                is Resource.Error -> {
                    showSnackBar(it.message ?: "Something went wrong" )
                }
                is Resource.Loading -> {
                }
                is Resource.Success -> {
                    feedList?.add(HomeFeed("Upcoming Movies", it.data!!.movieResults))
                    homeAdapter.submitList(feedList)
                    feedList = null
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
                "media_key",
                bundleOf("movie" to "movies_mania")
            )
            navController.navigate(R.id.action_navigation_home_to_movieListFragment)
        }

        tvShowsText.setOnClickListener {
            parentFragmentManager.setFragmentResult("media_key", bundleOf("tv" to "tvShows"))
            navController.navigate(R.id.action_navigation_home_to_movieListFragment)
        }

        bannerInfoButton.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                "home_movie_key",
                bundleOf(
                    "movie_title" to (bannerMovie.title ?: bannerMovie.tvShowName),
                    "movie_overview" to bannerMovie.overview,
                    "movie_image_url" to bannerMovie.posterPath,
                    "movie_year" to (bannerMovie.releaseDate ?: bannerMovie.tvShowFirstAirDate)
                )
            )
            navController.navigate(R.id.action_navigation_home_to_detailFragment)
        }

        addToListButton.setOnClickListener {}

        playButton.setOnClickListener {}
    }

    private fun setUpRecyclerView() {
        homeAdapter = HomeAdapter(onPosterClick = {
            parentFragmentManager.setFragmentResult(
                "home_movie_key",
                bundleOf(
                    "movie_title" to (it.title ?: it.tvShowName),
                    "isMovie" to !it.title.isNullOrBlank(),
                    "movie_overview" to it.overview,
                    "movie_image_url" to it.posterPath,
                    "movie_year" to (it.releaseDate ?: it.tvShowFirstAirDate),
                    "movie_id" to it.id
                )
            )
            navController.navigate(R.id.action_navigation_home_to_detailFragment)
        })
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
        Log.d("isItCalled", "onStop: Called")
    }
}