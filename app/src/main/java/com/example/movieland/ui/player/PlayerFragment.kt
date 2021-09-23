package com.example.movieland.ui.player

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.RatingBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.movieland.databinding.FragmentPlayerBinding
import com.example.movieland.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer

import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.datasource.remote.models.requests.AddToFavouriteRequest
import com.example.datasource.remote.models.requests.AddToWatchListRequest
import com.example.datasource.remote.models.requests.MediaRatingRequest
import com.example.datasource.remote.models.responses.*
import com.example.movieland.R
import com.example.movieland.databinding.RatingDialogBinding
import com.example.movieland.ui.coming_soon.GenreAdapter
import com.example.movieland.ui.home.HorizontalAdapter
import com.example.movieland.ui.player.adapters.MoreVideosAdapter
import com.example.movieland.ui.player.adapters.TvShowEpisodesAdapter
import com.example.movieland.utils.Constants.KEY_IS_MOVIE
import com.example.movieland.utils.Constants.KEY_MOVIE_ID
import com.example.movieland.utils.Constants.KEY_VIDEO_REQUEST
import com.example.movieland.utils.Constants.TMDB_IMAGE_BASE_URL_W780
import com.example.movieland.utils.showSnackBar
import com.google.android.material.tabs.TabLayout

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import kotlinx.coroutines.flow.first

/*
 1) Create Exoplayer object
 2) Create a Media Item & add it to exoplayer
*/

@AndroidEntryPoint
class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlayerViewModel by viewModels()

    // Adapters
    private lateinit var similarMoviesAdapter: HorizontalAdapter
    private lateinit var tvShowEpisodesAdapter: TvShowEpisodesAdapter
    private lateinit var moreVideosAdapter: MoreVideosAdapter
    private lateinit var genreAdapter: GenreAdapter

    private var _isItMovie: Boolean = true
    private var _id: Int? = null
    private var onTabSelectedListener: TabLayout.OnTabSelectedListener? = null
    private var _seasonList: List<Season>? = null
    private var _currentSeasonNumber: Int = 1
    private lateinit var _currentMedia: MovieDetailResponse
    private lateinit var shakeAnim: Animation
//    private var videoKey: String? = null
    // Player state helper variables
//    private var playWhenReady = true
//    private var currentWindow = 0
//    private var playbackPosition = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shakeAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.poping_anim)
        setUpRecyclerViewAndUi()
        setUpFragmentResultListeners()
        setUpObservers()
        setUpClickListeners()
    }

    private fun setUpClickListeners() = binding.apply {

        pickSeasonBtn.setOnClickListener {
            _seasonList?.let { seasonList ->
                parentFragmentManager.setFragmentResult(
                    "seasons_list_request_key",
                    bundleOf(
                        "season_list" to seasonList,
                        "selected_item_position" to _currentSeasonNumber
                    )
                )
                findNavController().navigate(R.id.action_playerFragment_to_seasonPickerDialogFragment)
            }
        }

        rateButton.setOnClickListener {
//            rateButton.startAnimation(shakeAnim)

            val ratingDialog = RatingDialogBinding.inflate(layoutInflater)
            ratingDialog.apply {
                rateButton.setOnClickListener { }
                mediaName.text = _currentMedia.title
                ratingBar.setOnRatingBarChangeListener { ratingBar: RatingBar, ratingValue: Float, b ->
                    Log.d("ratingBar", "setUpClickListeners: ${ratingBar.rating}")

                    if (ratingValue < 2) {
                        starImageView.animate().scaleX((ratingValue / 1.6).toFloat())
                        starImageView.animate().scaleY((ratingValue / 1.6).toFloat())
                    } else {
                        starImageView.animate().scaleX(ratingValue / 2)
                        starImageView.animate().scaleY(ratingValue / 2)
                    }
                }
                rateButton.setOnClickListener {
                    // start progress & call the api here
                    rateButton.text = ""
                    rateButton.isEnabled = false
                    progressBar.isGone = false

                    viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                        Log.d(
                            "SessionId",
                            "sessionId: ${viewModel.getSessionId().first()!!}  " +
                                    "id: ${_currentMedia.id}  " +
                                    "rating: ${ratingDialog.ratingBar.rating}"
                        )
                        if (_isItMovie)
                            viewModel.rateMovie(
                                movieId = _currentMedia.id,
                                sessionId = viewModel.getSessionId().first()!!,
                                mediaRatingRequest = MediaRatingRequest(5f)
                            ).let {
                                when (it) {
                                    is Resource.Error -> {
                                        progressBar.isGone = true
                                        rateButton.text = "Rate"
                                        rateButton.isEnabled = true
                                        showSnackBar(" Something went wrong")
                                    }
                                    is Resource.Loading -> TODO()
                                    is Resource.Success -> {
                                        // Dismiss dialog
                                        showSnackBar("Success")
                                    }
                                }
                            }
                        else
                            viewModel.rateTvShow(
                                tvId = _currentMedia.id,
                                sessionId = viewModel.getSessionId().first()!!,
                                mediaRatingRequest = MediaRatingRequest(ratingDialog.ratingBar.rating)
                            ).let {
                                when (it) {
                                    is Resource.Error -> {
                                        progressBar.isGone = true
                                        rateButton.text = "Rate"
                                        rateButton.isEnabled = true
                                        showSnackBar(it.message ?: "Something went wrong")
                                    }
                                    is Resource.Loading -> TODO()
                                    is Resource.Success -> {
                                        // Dismiss dialog
                                    }
                                }
                            }
                    }
                }
            }

            val dialog = Dialog(requireContext(), R.style.RatingDialog).apply {
//                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setContentView(ratingDialog.root)
                setCancelable(true)
            }
            dialog.show()
        }

        addToListButton.setOnClickListener {
            addToListButton.startAnimation(shakeAnim)
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                viewModel.addToWatchList(
                    accountId = 0,
                    sessionId = viewModel.getSessionId().first()!!,
                    addToWatchListRequest = AddToWatchListRequest(
                        mediaId = _currentMedia.id,
                        mediaType = "Movie",
                        watchlist = true
                    )
                ).let {
                    showSnackBar("Added to My List")
                }
            }
        }

        favouriteButton.setOnClickListener {
            favouriteButton.startAnimation(shakeAnim)
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                viewModel.addToFavourites(
                    accountId = 0,
                    sessionId = viewModel.getSessionId().first()!!,
                    addToFavouriteRequest = AddToFavouriteRequest(
                        mediaId = _currentMedia.id,
                        mediaType = "Movie",
                        favorite = true
                    )
                ).let {
                    showSnackBar("Added to Favourites")
                }
            }
        }

        shareButton.setOnClickListener {

        }

    }

    private fun setUpObservers() {
        viewModel.movieDetail.observe(viewLifecycleOwner) {
            Log.d("LiveObserver0", "setUpObservers: called")
            when (it) {
                is Resource.Error -> {

                }
                is Resource.Loading -> binding.apply {
                    shimmerLayout.isGone = false
                    shimmerLayout.startShimmer()
                    mainLayout.isGone = true
                }
                is Resource.Success -> {

                    binding.shimmerLayout.isGone = true
                    binding.mainLayout.isGone = false
                    binding.shimmerLayout.stopShimmer()

                    _currentMedia = it.data!!

                    val a: List<VideoResult> = it.data.videos.videosList.filter { toFilter ->
                        toFilter.type == "Trailer" && toFilter.site == "Youtube"
                    }
                    val videoKey = it.data.videos.videosList[0].key
                    Log.d("VideoKey", "onViewCreated: $videoKey   | a: $a")
                    initializePlayer(videoKey)
                    updateDetails(movieDetailResponse = it.data)
                    moreVideosAdapter.submitList(it.data.videos.videosList)
                }
            }
        }

        viewModel.tvShowDetail.observe(viewLifecycleOwner) {
            Log.d("LiveObserver1", "setUpObservers: called")
            when (it) {
                is Resource.Error -> {
                }
                is Resource.Loading -> binding.apply {
                    shimmerLayout.isGone = false
                    shimmerLayout.startShimmer()
                    mainLayout.isGone = true
                }
                is Resource.Success -> {
                    Log.d("isTvSuccess", "setUpObservers: Success")

                    binding.shimmerLayout.isGone = true
                    binding.mainLayout.isGone = false
                    binding.shimmerLayout.stopShimmer()
                    _seasonList = it.data!!.seasons

                    val videoKey = it.data.videos.videosList[0].key
                    Log.d("VideoKey", "onViewCreated: $videoKey")
                    initializePlayer(videoKey)
                    updateDetails(tvDetailResponse = it.data)
                    moreVideosAdapter.submitList(it.data.videos.videosList)
                }
            }
        }

        viewModel.similarMovies.observe(viewLifecycleOwner) {
            Log.d("LiveObserver2", "setUpObservers: called")
            when (it) {
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_LONG).show()
                }
                is Resource.Loading -> {
                }
                is Resource.Success -> {
                    Log.d("SuccessObserver2", " success ")
                    similarMoviesAdapter.submitList(it.data?.movieResults)
                }
            }
        }

        viewModel.tvSeasonDetail.observe(viewLifecycleOwner) {
            Log.d("LiveObserver3", "setUpObservers: called")
            when (it) {
                is Resource.Error -> {
                }
                is Resource.Loading -> {
                }
                is Resource.Success -> {
                    Log.d("SuccessObserver3", " success ")
                    tvShowEpisodesAdapter.submitList(it.data?.episodes)
                }
            }
        }
    }

    private fun setUpFragmentResultListeners() {
        parentFragmentManager.setFragmentResultListener(
            KEY_VIDEO_REQUEST,
            viewLifecycleOwner
        ) { _, bundle ->
            _isItMovie = bundle.getBoolean(KEY_IS_MOVIE)
            binding.apply {
                pickSeasonBtn.isGone = _isItMovie
                moviesTabLayout.isGone = !_isItMovie
                showsTabLayout.isGone = _isItMovie
                rvEpisodesList.isGone = _isItMovie
                rvSimilarList.isGone = !_isItMovie
                rvSimilarList.isVisible = _isItMovie
                rvEpisodesList.isVisible = !_isItMovie
            }

            setUpTabLayout()

            bundle.getInt(KEY_MOVIE_ID).let { movieId: Int ->
                _id = movieId
                if (_isItMovie) {
                    viewModel.getMovieDetail(movieId)
                    viewModel.getSimilarMovies(movieId)
                } else {
                    viewModel.getTvShowDetail(tvId = movieId) // using same name for movie and tv id
                    viewModel.getTvSeasonDetail(
                        tvId = movieId,
                        seasonNumber = _currentSeasonNumber
                    )
                    viewModel.getSimilarMovies(movieId)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("isItMovie_Resume", "onResume: $_isItMovie")
        if (!_isItMovie) {
            parentFragmentManager.setFragmentResultListener(
                "options_selected_item_position",
                viewLifecycleOwner
            ) { _, bundle ->
                _currentSeasonNumber = bundle.getInt("season_number", 1)
                binding.pickSeasonBtn.text = "Season $_currentSeasonNumber"

                Log.d("isitwprking", "onResume: yes")
                _id?.let {
                    viewModel.getTvSeasonDetail(
                        tvId = it,
                        seasonNumber = _currentSeasonNumber
                    )
                }
            }
        }
    }

    private fun updateDetails(
        movieDetailResponse: MovieDetailResponse? = null,
        tvDetailResponse: TvShowDetailsResponse? = null
    ) = binding.apply {
        Log.d("updateDetailsIsMovie", "updateDetails: $_isItMovie")

        if (_isItMovie) {
            thumbnailContainer.backdropImage.load(
                TMDB_IMAGE_BASE_URL_W780.plus(movieDetailResponse!!.backdropPath)
            )
            titleText.text = movieDetailResponse.title
            overviewText.text = movieDetailResponse.overview
            yearText.text = movieDetailResponse.releaseDate
            runtimeText.text = movieDetailResponse.runtime.toString()
            ratingText.text = String.format("%.1f", movieDetailResponse.voteAverage)
            // Setting up Genre Adapter
            genreAdapter.submitList(movieDetailResponse.genres)
        } else {
            thumbnailContainer.backdropImage.load(
                TMDB_IMAGE_BASE_URL_W780.plus(tvDetailResponse!!.backdropPath)
            )
            titleText.text = tvDetailResponse.name
            overviewText.text = tvDetailResponse.overview
            yearText.text = tvDetailResponse.firstAirDate
//            runtimeText.text = tvDetailResponse.runtime
            ratingText.text = String.format("%.1f", tvDetailResponse.voteAverage)
            // Setting up Genre Adapter
            genreAdapter.submitList(tvDetailResponse.genres)
        }
    }

    private fun setUpTabLayout() = binding.apply {

        onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                Log.d("isItaMovie2", "setUpFragmentResultListeners: $_isItMovie")

                if (_isItMovie) {
                    when (tab?.position) {
                        0 -> {
                            // Similar like this Tab
                            rvSimilarList.isGone = false
                            rvMoreVideosList.isGone = true
                        }
                        1 -> {
                            // Trailers and more
                            rvSimilarList.isGone = true
                            rvMoreVideosList.isGone = false
                        }
                    }
                } else {
                    when (tab?.position) {
                        0 -> {
                            // Tv shows episodes tab
                            rvEpisodesList.isGone = false
                            rvSimilarList.isGone = true
                            rvMoreVideosList.isGone = true
                            pickSeasonBtn.isGone = false
                        }
                        1 -> {
                            // Similar like this tab
                            rvEpisodesList.isGone = true
                            rvSimilarList.isGone = false
                            rvMoreVideosList.isGone = true
                            pickSeasonBtn.isGone = true
                        }
                        2 -> {
                            // Trailers and more tab
                            rvEpisodesList.isGone = true
                            rvSimilarList.isGone = true
                            rvMoreVideosList.isGone = false
                            pickSeasonBtn.isGone = true
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        }
        if (_isItMovie) moviesTabLayout.addOnTabSelectedListener(onTabSelectedListener!!)
        else showsTabLayout.addOnTabSelectedListener(onTabSelectedListener!!)
    }

    private fun setUpRecyclerViewAndUi() = binding.apply {
        // Setting up Genre Adapter
        genreAdapter = GenreAdapter()
        binding.rvGenres.setHasFixedSize(true)
        binding.rvGenres.adapter = genreAdapter

        // Setting up Similar list Adapter
        rvSimilarList.setHasFixedSize(true)
        similarMoviesAdapter = HorizontalAdapter {
            parentFragmentManager.setFragmentResult(
                "home_movie_key",
                bundleOf(
                    "movie_title" to (it.title ?: it.tvShowName),
                    "isMovie" to !it.title.isNullOrBlank(),  // title field will be null in case of a movie
                    "movie_overview" to it.overview,
                    "movie_image_url" to it.posterPath,
                    "movie_year" to (it.releaseDate ?: it.tvShowFirstAirDate),
                    "movie_id" to it.id
                )
            )
            findNavController().navigate(R.id.action_playerFragment_to_detailFragment)
        }
        rvSimilarList.adapter = similarMoviesAdapter

        // Setting up More Videos Adapter
        moreVideosAdapter = MoreVideosAdapter()
        rvMoreVideosList.setHasFixedSize(true)
        rvMoreVideosList.adapter = moreVideosAdapter

        // Setting up TvShowsEpisodeAdapter
        tvShowEpisodesAdapter = TvShowEpisodesAdapter()
        rvEpisodesList.setHasFixedSize(true)
        rvEpisodesList.adapter = tvShowEpisodesAdapter
    }

    private fun initializePlayer(videoKey: String?) {
        viewLifecycleOwner.lifecycle.addObserver(binding.youtubePlayerView)
        binding.youtubePlayerView
            .addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    videoKey?.let {
                        binding.thumbnailContainer.container.isGone = true
                        binding.youtubePlayerView.isGone = false
                        youTubePlayer.loadVideo(it, 0f)
                    }
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onTabSelectedListener?.let {
            binding.moviesTabLayout.removeOnTabSelectedListener(it)
            binding.showsTabLayout.removeOnTabSelectedListener(it)
        }
        onTabSelectedListener = null
    }
}