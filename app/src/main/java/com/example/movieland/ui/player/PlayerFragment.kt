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
import dagger.hilt.android.AndroidEntryPoint
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.datasource.remote.models.requests.AddToFavouriteRequest
import com.example.datasource.remote.models.requests.AddToWatchListRequest
import com.example.datasource.remote.models.requests.MediaRatingRequest
import com.example.datasource.remote.models.responses.*
import com.example.movieland.R
import com.example.movieland.databinding.RatingDialogBinding
import com.example.movieland.ui.CastListAdapter
import com.example.movieland.ui.genres.GenreAdapter
import com.example.movieland.ui.home.HorizontalAdapter
import com.example.movieland.ui.player.adapters.MoreVideosAdapter
import com.example.movieland.ui.player.adapters.TvShowEpisodesAdapter
import com.example.movieland.utils.*
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
import com.example.movieland.utils.Constants.MOVIE
import com.example.movieland.utils.Constants.SEASONS_LIST_REQUEST_KEY
import com.example.movieland.utils.Constants.TMDB_IMAGE_BASE_URL_W780
import com.google.android.material.tabs.TabLayout

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
    private lateinit var similarMediaAdapter: HorizontalAdapter
    private lateinit var recommendationsAdapter: HorizontalAdapter
    private lateinit var tvShowEpisodesAdapter: TvShowEpisodesAdapter
    private lateinit var moreVideosAdapter: MoreVideosAdapter
    private lateinit var genreAdapter: GenreAdapter
    private lateinit var castAdapter: CastListAdapter

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
        viewLifecycleOwner.lifecycle.addObserver(binding.youtubePlayerView)
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
                safeFragmentNavigation(
                    navController = findNavController(),
                    currentFragmentId = R.id.playerFragment,
                    actionId = R.id.action_playerFragment_to_seasonPickerDialogFragment
                )
            }
        }

        rateButton.setOnClickListener {

            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                val sessionId = viewModel.getSessionId().first()
                val accountId = viewModel.getAccountId().first()

                if (sessionId != null && accountId != null) {
                    // Now user can rate
                    val ratingDialog = RatingDialogBinding.inflate(layoutInflater)
                    val dialog = Dialog(requireContext(), R.style.RatingDialog).apply {
//                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        setContentView(ratingDialog.root)
                        setCancelable(true)
                    }
                    ratingDialog.apply {
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
                                    sessionId = sessionId,
                                    mediaRatingRequest = MediaRatingRequest(ratingBar.rating * 2)
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
                } else showSnackBar("Please login to avail this feature")

            }
        }

        addToListButton.setOnClickListener {
            addToListButton.startAnimation(popingAnim)

            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                val sessionId = viewModel.getSessionId().first()
                val accountId = viewModel.getAccountId().first()

                if (sessionId != null && accountId != null) {

                    viewModel.addToWatchList(
                        accountId = accountId,
                        sessionId = sessionId,
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


                } else showSnackBar("Please login to avail this feature")
            }
        }

        favouriteButton.setOnClickListener {
            favouriteButton.startAnimation(popingAnim)
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                val sessionId = viewModel.getSessionId().first()
                val accountId = viewModel.getAccountId().first()

                if (sessionId != null && accountId != null) {

                    viewModel.addToFavourites(
                        accountId = accountId,
                        sessionId = sessionId,
                        addToFavouriteRequest = AddToFavouriteRequest(
                            mediaId = (_currentMovie?.id ?: _currentTvShow?.id)!!,
                            mediaType = if (_isItMovie) MOVIE else "tv",
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

                } else showSnackBar("Please login to avail this feature")
            }
        }

        shareButton.setOnClickListener {
            Toast.makeText(requireContext(), "Not yet implemented", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpObservers() {
        viewModel.movieDetail.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> {
                    TODO()
                }
                is Resource.Loading -> binding.apply {
                    shimmerLayout.isGone = false
                    shimmerLayout.startShimmer()
                    mainLayout.isGone = true
                }
                is Resource.Success -> binding.apply {

                    _currentMovie = it.data!!
                    val totalVideos = it.data.videos.videosList as ArrayList

//                    binding.shimmerLayout.isGone = true
//                    binding.mainLayout.isGone = false
//                    binding.shimmerLayout.stopShimmer()

                    val trailers: List<VideoResult> = totalVideos.filter { toFilter ->
                        toFilter.type == "Trailer" && toFilter.site == "YouTube"
                    }

                    if (totalVideos.isEmpty()) {
                        // No video to play
                        // initializePlayer(key)
                        emptyMoreVideosMsg.isGone = false
                        rvMoreVideosList.isGone = true
                    } else {

                        val videoKey =
                            if (trailers.isEmpty()) totalVideos[0].key else trailers[0].key
                        Log.d("VideoKey", "onViewCreated: $videoKey   | a: $trailers")
                        initializePlayer(videoKey)
                        // removing already played trailer from totalVideos
                        if (trailers.isNotEmpty())
                            totalVideos.remove(trailers[0])
                        moreVideosAdapter.submitList(totalVideos)
                    }
                    updateDetails(movieDetails = it.data)
                }
            }
        }

        viewModel.tvShowDetail.observe(viewLifecycleOwner) {
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

                    _currentTvShow = it.data!!
                    _seasonList = it.data.seasons

//                    binding.shimmerLayout.isGone = true
//                    binding.mainLayout.isGone = false
//                    binding.shimmerLayout.stopShimmer()

                    Log.d("CheckSize", "setUpObservers: ${it.data.videos.videosList}")
                    if (it.data.videos.videosList.isNotEmpty()) {
                        val videoKey = it.data.videos.videosList[0].key
                        Log.d("VideoKey", "onViewCreated: $videoKey")
                        initializePlayer(videoKey)
                    }
                    updateDetails(tvDetails = it.data)
                    moreVideosAdapter.submitList(it.data.videos.videosList)
                }
            }
        }

        viewModel.mediaCast.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> TODO()
                is Resource.Loading -> TODO()
                is Resource.Success -> {
                    binding.rvCrews.isGone = false
                    binding.castBg.isGone = true
                    castAdapter.submitList(it.data!!.cast)
                }
            }
        }

        viewModel.recommendedMedia.observe(viewLifecycleOwner) {

            when (it) {
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_LONG).show()
                }
                is Resource.Loading -> {
                }
                is Resource.Success -> binding.apply {

                    Log.d("recommendationsSize", "setUpObservers: ${it.data!!.movieResults.size}")

                    if (it.data!!.movieResults.isNotEmpty()) {
                        recommendationsAdapter.submitList(it.data.movieResults)
                    } else {
                        // Show no recommendations msg
                        emptyRecommendationsMsg.isGone = false
                        rvRecommendations.isGone = true
                    }
                }
            }
        }

        viewModel.similarMedia.observe(viewLifecycleOwner) {
            Log.d("LiveObserver2", "setUpObservers: called")
            when (it) {
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_LONG).show()
                }
                is Resource.Loading -> {
                }
                is Resource.Success -> {
                    Log.d("SuccessObserver2", " success ")
                    similarMediaAdapter.submitList(it.data?.movieResults)
                }
            }
        }

        viewModel.tvSeasonDetail.observe(viewLifecycleOwner) {

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
            binding.episodesLayout.isGone = _isItMovie
            setUpTabLayout()
            bundle.getInt(MEDIA_ID_KEY).let { mediaId: Int ->
                _id = mediaId

                if (!_isItMovie) {
                    viewModel.getTvShowDetail(tvId = mediaId)
                    viewModel.getTvSeasonDetail(
                        tvId = mediaId,
                        seasonNumber = _currentSeasonNumber
                    )
                } else {
                    viewModel.getMovieDetail(movieId = mediaId)
                }
                viewModel.getMediaCast(mediaId, _isItMovie)
                viewModel.getRecommendationsMedia(mediaId = mediaId, _isItMovie)
                viewModel.getSimilarMedia(mediaId = mediaId, _isItMovie)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (!_isItMovie) {
            parentFragmentManager.setFragmentResultListener(
                "options_selected_item_position",
                viewLifecycleOwner
            ) { _, bundle ->
                _currentSeasonNumber = bundle.getInt("season_number", 1)
                binding.pickSeasonBtn.text = "Season $_currentSeasonNumber"

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
        movieDetails: MovieDetailResponse? = null,
        tvDetails: TvShowDetailsResponse? = null
    ) = binding.apply {

        binding.shimmerLayout.isGone = true
        binding.mainLayout.isGone = false
        binding.shimmerLayout.stopShimmer()

        if (_isItMovie && movieDetails != null) {
            thumbnailContainer.backdropImage.load(
                TMDB_IMAGE_BASE_URL_W780.plus(movieDetails.backdropPath)
            )
            titleText.text = movieDetails.title
            overviewText.text = movieDetails.overview
            yearText.formatMediaDate(movieDetails.releaseDate)
            runtimeText.text = "${movieDetails.runtime / 60} h ${movieDetails.runtime % 60} min"
            ratingText.text = String.format("%.1f", movieDetails.voteAverage)
            // Setting up Genre Adapter
            genreAdapter.submitList(movieDetails.genres)
        } else {
            thumbnailContainer.backdropImage.load(
                TMDB_IMAGE_BASE_URL_W780.plus(tvDetails!!.backdropPath)
            )
            titleText.text = tvDetails.name
            overviewText.text = tvDetails.overview
            yearText.formatMediaDate(tvDetails.firstAirDate)
//            runtimeText.text = tvDetails.episodeRunTime
            ratingText.text = String.format("%.1f", tvDetails.voteAverage)
            // Setting up Genre Adapter
            genreAdapter.submitList(tvDetails.genres)
        }

//        binding.shimmerLayout.isGone = true
//        binding.mainLayout.isGone = false
//        binding.shimmerLayout.stopShimmer()
    }

    private fun setUpTabLayout() = binding.apply {

        onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                when (tab?.position) {
                    0 -> {
                        // Recommendations Tab
                        rvSimilarList.isGone = true
                        recommendationsContainer.isGone = false
                    }
                    1 -> {
                        // More like this tab
                        rvSimilarList.isGone = false
                        recommendationsContainer.isGone = true
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        }
        mediaTabLayout.addOnTabSelectedListener(onTabSelectedListener!!)
    }

    private fun setUpRecyclerViewAndUi() = binding.apply {
        // Setting up Genre Adapter
        genreAdapter = GenreAdapter(
            cardBackColor = ContextCompat.getColor(requireContext(), R.color.black_light),
            cardStrokeColor = ContextCompat.getColor(requireContext(), R.color.red)
        )
        binding.rvGenres.setHasFixedSize(true)
        binding.rvGenres.adapter = genreAdapter

        // Setting up Crew Adapter
        castAdapter = CastListAdapter()
        binding.rvCrews.setHasFixedSize(true)
        binding.rvCrews.adapter = castAdapter

        // Setting up Similar list Adapter
        rvSimilarList.setHasFixedSize(true)
        similarMediaAdapter = HorizontalAdapter {
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
                navController = findNavController(),
                currentFragmentId = R.id.playerFragment,
                actionId = R.id.action_playerFragment_to_detailFragment
            )
        }
        rvSimilarList.adapter = similarMediaAdapter

        // Setting up Recommendations list Adapter
        rvRecommendations.setHasFixedSize(true)
        recommendationsAdapter = HorizontalAdapter {
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
                navController = findNavController(),
                currentFragmentId = R.id.playerFragment,
                actionId = R.id.action_playerFragment_to_detailFragment
            )
        }
        rvRecommendations.adapter = recommendationsAdapter

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
        rvEpisodes.setHasFixedSize(true)
        rvEpisodes.adapter = tvShowEpisodesAdapter
    }

    private fun initializePlayer(videoKey: String?) = binding.apply {

        youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                videoKey?.let {
                    thumbnailContainer.container.isGone = true
                    youtubePlayerView.isGone = false
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                        youTubePlayer.loadVideo(it, 0f)
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onTabSelectedListener?.let {
            binding.mediaTabLayout.removeOnTabSelectedListener(it)
        }
        onTabSelectedListener = null
        _binding = null
    }
}