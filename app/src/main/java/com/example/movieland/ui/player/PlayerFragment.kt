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
import com.example.movieland.utils.Constants.GENRES_ID_LIST_KEY
import com.example.movieland.utils.Constants.IS_IT_A_MOVIE_KEY
import com.example.movieland.utils.Constants.MEDIA_ID_KEY
import com.example.movieland.utils.Constants.MEDIA_IMAGE_KEY
import com.example.movieland.utils.Constants.MEDIA_OVERVIEW_KEY
import com.example.movieland.utils.Constants.MEDIA_SEND_REQUEST_KEY
import com.example.movieland.utils.Constants.MEDIA_TITLE_KEY
import com.example.movieland.utils.Constants.MEDIA_YEAR_KEY
import com.example.movieland.utils.Constants.MOVIE
import com.example.movieland.utils.Constants.MEDIA_PLAY_REQUEST_KEY
import com.example.movieland.utils.Constants.SEASONS_LIST_REQUEST_KEY
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
    private var _currentMovie: MovieDetailResponse? = null
    private var _currentTvShow: TvShowDetailsResponse? = null
    private lateinit var popingAnim: Animation
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
        popingAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.poping_anim)
        setUpRecyclerViewAndUi()
        setUpFragmentResultListeners()
        setUpObservers()
        setUpClickListeners()
    }

    private fun setUpClickListeners() = binding.apply {

        pickSeasonBtn.setOnClickListener {
            _seasonList?.let { seasonList ->
                parentFragmentManager.setFragmentResult(
                    SEASONS_LIST_REQUEST_KEY,
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
            val dialog = Dialog(requireContext(), R.style.RatingDialog).apply {
//                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setContentView(ratingDialog.root)
                setCancelable(true)
            }
            ratingDialog.apply {
                rateButton.setOnClickListener { }
                mediaName.text = _currentMovie?.title ?: _currentTvShow?.name
                ratingBar.setOnRatingBarChangeListener { ratingBar: RatingBar, ratingValue: Float, b ->

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

                        viewModel.rateMedia(
                            mediaId = (_currentMovie?.id ?: _currentTvShow?.id)!!,
                            isMovie = _isItMovie,
                            sessionId = viewModel.getSessionId().first()!!,
                            mediaRatingRequest = MediaRatingRequest(ratingBar.rating*2)
                        ).let {
                            when (it) {
                                is Resource.Error -> {
                                    progressBar.isGone = true
                                    rateButton.text = "Rate"
                                    rateButton.isEnabled = true
                                    showSnackBar(" Something went wrong")
                                }
//                                is Resource.Loading -> TODO()
                                is Resource.Success -> {
                                    // Dismiss dialog
                                    dialog.dismiss()
                                    showSnackBar("Success")
                                }
                            }
                        }
                    }
                }
            }

            dialog.show()
        }

        addToListButton.setOnClickListener {
            addToListButton.startAnimation(popingAnim)

            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                viewModel.addToWatchList(
                    accountId = viewModel.getAccountId().first()!!,
                    sessionId = viewModel.getSessionId().first()!!,
                    addToWatchListRequest = AddToWatchListRequest(
                        mediaId = (_currentMovie?.id ?: _currentTvShow?.id)!!,
                        mediaType = if (_isItMovie) "movie" else "tv",
                        watchlist = true
                    )
                ).let { response ->
                    when (response) {
                        is Resource.Error -> showSnackBar(
                            response.message ?: "Something went wrong"
                        )
//                        is Resource.Loading -> TODO()
                        is Resource.Success -> {
                            showSnackBar("Added to My List")
                        }
                    }
                }
            }
        }

        favouriteButton.setOnClickListener {
            favouriteButton.startAnimation(popingAnim)
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                viewModel.addToFavourites(
                    accountId = viewModel.getAccountId().first()!!,
                    sessionId = viewModel.getSessionId().first()!!,
                    addToFavouriteRequest = AddToFavouriteRequest(
                        mediaId = (_currentMovie?.id ?: _currentTvShow?.id)!!,
                        mediaType = if (_isItMovie) "movie" else "tv",
                        favorite = true
                    )
                ).let { response ->
                    when (response) {
                        is Resource.Error -> showSnackBar(
                            response.message ?: "Something went wrong"
                        )
//                        is Resource.Loading -> TODO()
                        is Resource.Success -> {
                            showSnackBar("Added to Favourites")
                        }
                    }
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
                    TODO()
                }
                is Resource.Loading -> binding.apply {
                    shimmerLayout.isGone = false
                    shimmerLayout.startShimmer()
                    mainLayout.isGone = true
                }
                is Resource.Success -> {

                    _currentMovie = it.data!!

                    Log.d("DDLJ", "setUpObservers: ${it.data.id}")

                    binding.shimmerLayout.isGone = true
                    binding.mainLayout.isGone = false
                    binding.shimmerLayout.stopShimmer()

                    val totalVideos = it.data.videos.videosList as ArrayList
                    if (totalVideos.isEmpty()) {
                        // No video to play
                        // initializePlayer(key)
                    } else {
                        val trailers: List<VideoResult> = totalVideos.filter { toFilter ->
                            toFilter.type == "Trailer" && toFilter.site == "YouTube"
                        }
                        val trailer = trailers[0]
                        val videoKey = if (trailers.isEmpty()) totalVideos[0].key else trailer.key
                        Log.d("VideoKey", "onViewCreated: $videoKey   | a: $trailers")
                        initializePlayer(videoKey)
                        // removing already played trailer from totalVideos
                        totalVideos.remove(trailer)
                    }
                    updateDetails(movieDetailResponse = it.data)
                    moreVideosAdapter.submitList(totalVideos)
                }
            }
        }

        viewModel.tvShowDetail.observe(viewLifecycleOwner) {
            Log.d("LiveObserver1", "setUpObservers: called")
            when (it) {
                is Resource.Error -> {
                    TODO()
                }
                is Resource.Loading -> binding.apply {
                    shimmerLayout.isGone = false
                    shimmerLayout.startShimmer()
                    mainLayout.isGone = true
                }
                is Resource.Success -> {
                    Log.d("isTvSuccess", "setUpObservers: Success")

                    _currentTvShow = it.data!!

                    binding.shimmerLayout.isGone = true
                    binding.mainLayout.isGone = false
                    binding.shimmerLayout.stopShimmer()

                    _seasonList = it.data.seasons
                    Log.d("CheckSize", "setUpObservers: ${it.data.videos.videosList}")
                    if (it.data.videos.videosList.isNotEmpty()) {
                        val videoKey = it.data.videos.videosList[0].key
                        Log.d("VideoKey", "onViewCreated: $videoKey")
                        initializePlayer(videoKey)
                    }
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
                    TODO()
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
            MEDIA_PLAY_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            _isItMovie = bundle.getBoolean(IS_IT_A_MOVIE_KEY)
            binding.apply {
                moviesTabLayout.isGone = !_isItMovie
//                showsTabLayout.isGone = _isItMovie
                rvSimilarList.isGone = !_isItMovie
                showsTabLl.isGone = _isItMovie
//                rvEpisodesList.isGone = _isItMovie
//                pickSeasonBtn.isGone = _isItMovie

                rvEpisodesList.isGone = _isItMovie
                rvEpisodesList.layoutMode = ViewGroup.LAYOUT_MODE_CLIP_BOUNDS
            }

            setUpTabLayout()

            bundle.getInt(MEDIA_ID_KEY).let { mediaId: Int ->
                _id = mediaId
                Log.d(
                    "CheckId", "media: $_id |" +
                            " currentSeasonNumber: $_currentSeasonNumber"
                )
                if (_isItMovie) {
                    viewModel.getMovieDetail(movieId = mediaId)
                    viewModel.getSimilarMovies(movieId = mediaId)
                } else {
                    viewModel.getTvShowDetail(tvId = mediaId) // using same name for movie and tv id
                    viewModel.getTvSeasonDetail(
                        tvId = mediaId,
                        seasonNumber = _currentSeasonNumber
                    )
                    viewModel.getSimilarShows(tvId = mediaId)
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

        if (_isItMovie && movieDetailResponse != null) {
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
                MEDIA_SEND_REQUEST_KEY,
                bundleOf(
                    GENRES_ID_LIST_KEY to it.genreIds,
                    MEDIA_TITLE_KEY to (it.title ?: it.tvShowName),
                    IS_IT_A_MOVIE_KEY to !it.title.isNullOrEmpty(),
                    MEDIA_OVERVIEW_KEY to it.overview,
                    MEDIA_IMAGE_KEY to it.backdropPath,
                    MEDIA_YEAR_KEY to (it.releaseDate ?: it.tvShowFirstAirDate),
                    MEDIA_ID_KEY to it.id
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
        tvShowEpisodesAdapter = TvShowEpisodesAdapter {
            Toast.makeText(
                requireContext(),
                "This feature will be present in future release.",
                Toast.LENGTH_SHORT
            ).show()
        }
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