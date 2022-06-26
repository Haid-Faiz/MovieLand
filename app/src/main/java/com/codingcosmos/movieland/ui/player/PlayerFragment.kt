package com.codingcosmos.movieland.ui.player

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.codingcosmos.datasource.remote.models.requests.AddToFavouriteRequest
import com.codingcosmos.datasource.remote.models.requests.AddToWatchListRequest
import com.codingcosmos.datasource.remote.models.requests.MediaRatingRequest
import com.codingcosmos.datasource.remote.models.responses.Cast
import com.codingcosmos.datasource.remote.models.responses.MovieDetailResponse
import com.codingcosmos.datasource.remote.models.responses.Season
import com.codingcosmos.datasource.remote.models.responses.TvShowDetailsResponse
import com.codingcosmos.datasource.remote.models.responses.VideoResult
import com.codingcosmos.movieland.R
import com.codingcosmos.movieland.databinding.FragmentPlayerBinding
import com.codingcosmos.movieland.databinding.RatingDialogBinding
import com.codingcosmos.movieland.ui.cast.CastListAdapter
import com.codingcosmos.movieland.ui.home.HorizontalAdapter
import com.codingcosmos.movieland.ui.home.RecommendationsAdapter
import com.codingcosmos.movieland.ui.player.adapters.MoreVideosAdapter
import com.codingcosmos.movieland.ui.player.adapters.TvShowEpisodesAdapter
import com.codingcosmos.movieland.utils.Constants.CURRENT_SEASON_POSITION
import com.codingcosmos.movieland.utils.Constants.GENRES_ID_LIST_KEY
import com.codingcosmos.movieland.utils.Constants.IMDB_BASE_URL
import com.codingcosmos.movieland.utils.Constants.IS_IT_A_MOVIE_KEY
import com.codingcosmos.movieland.utils.Constants.MEDIA_ID_KEY
import com.codingcosmos.movieland.utils.Constants.MEDIA_IMAGE_KEY
import com.codingcosmos.movieland.utils.Constants.MEDIA_OVERVIEW_KEY
import com.codingcosmos.movieland.utils.Constants.MEDIA_PLAY_REQUEST_KEY
import com.codingcosmos.movieland.utils.Constants.MEDIA_RATING_KEY
import com.codingcosmos.movieland.utils.Constants.MEDIA_RE_PLAY_REQUEST_KEY
import com.codingcosmos.movieland.utils.Constants.MEDIA_SEND_REQUEST_KEY
import com.codingcosmos.movieland.utils.Constants.MEDIA_TITLE_KEY
import com.codingcosmos.movieland.utils.Constants.MEDIA_YEAR_KEY
import com.codingcosmos.movieland.utils.Constants.MOVIE
import com.codingcosmos.movieland.utils.Constants.SEASONS_LIST_REQUEST_KEY
import com.codingcosmos.movieland.utils.Constants.SEASON_LIST
import com.codingcosmos.movieland.utils.Constants.SEASON_NUMBER
import com.codingcosmos.movieland.utils.Constants.SELECTED_SEASON_POSITION
import com.codingcosmos.movieland.utils.Constants.TMDB_IMAGE_BASE_URL_W780
import com.codingcosmos.movieland.utils.Constants.TRAILER
import com.codingcosmos.movieland.utils.Constants.TV
import com.codingcosmos.movieland.utils.Constants.YOUTUBE
import com.codingcosmos.movieland.utils.Constants.YOUTUBE_VIDEO_URL
import com.codingcosmos.movieland.utils.ErrorType
import com.codingcosmos.movieland.utils.Resource
import com.codingcosmos.movieland.utils.formatMediaDate
import com.codingcosmos.movieland.utils.safeFragmentNavigation
import com.codingcosmos.movieland.utils.showSnackBar
import com.google.android.material.tabs.TabLayout
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlayerViewModel by viewModels()
    private var onTabSelectedListener: TabLayout.OnTabSelectedListener? = null
    private val customTabsIntent by lazy {
        CustomTabsIntent.Builder().setShowTitle(true).build()
    }

    // Adapters
    private lateinit var similarMediaAdapter: HorizontalAdapter
    private lateinit var recommendationsAdapter: RecommendationsAdapter
    private lateinit var tvShowEpisodesAdapter: TvShowEpisodesAdapter
    private lateinit var moreVideosAdapter: MoreVideosAdapter
    private lateinit var castAdapter: CastListAdapter

    // Global variables
    private var _youTubePlayer: YouTubePlayer? = null
    private var youTubePlayerListener: AbstractYouTubePlayerListener? = null
    private var _isItMovie: Boolean = true
    private var _mediaId: Int? = null
    private var _seasonList: List<Season>? = null
    private var _currentSeasonNumber: Int = 1
    private var _currentMovie: MovieDetailResponse? = null
    private var _currentTvShow: TvShowDetailsResponse? = null
    private lateinit var popingAnim: Animation
    private var _watchProviderUrl: String? = null
    private var isExpanded: Boolean = false
    private var youtubeVideoKey: String? = null

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

        backArrow.setOnClickListener { findNavController().popBackStack() }

        btnGetWatchProviders.setOnClickListener {
            _watchProviderUrl?.let { url ->
                customTabsIntent.launchUrl(requireContext(), url.toUri())
            } ?: kotlin.run {
                showSnackBar("Information not available")
            }
        }

        errorLayout.retryButton.setOnClickListener { fetchData() }

        pickSeasonBtn.setOnClickListener {

            _seasonList?.let { seasonList ->
                parentFragmentManager.setFragmentResult(
                    SEASONS_LIST_REQUEST_KEY,
                    bundleOf(
                        SEASON_LIST to seasonList,
                        // Sometimes, We are getting one extra season as 'Spacials' with season number 0
                        CURRENT_SEASON_POSITION to if (seasonList[0].seasonNumber == 0) _currentSeasonNumber else _currentSeasonNumber - 1
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
                                            showSnackBar(it.message!!)
                                        }
//                                is Resource.Loading -> TODO()
                                        is Resource.Success -> {
                                            // Dismiss dialog
                                            dialog.dismiss()
                                            showSnackBar("Rated successfully")
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
                            mediaType = if (_isItMovie) MOVIE else TV,
                            watchlist = true
                        )
                    ).let { response ->
                        when (response) {
                            is Resource.Error -> showSnackBar(
                                response.message!!
                            )
                            // is Resource.Loading -> TODO()
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
                            mediaType = if (_isItMovie) MOVIE else TV,
                            favorite = true
                        )
                    ).let { response ->
                        when (response) {
                            is Resource.Error -> showSnackBar(
                                response.message!!
                            )
                            // is Resource.Loading -> TODO()
                            is Resource.Success -> {
                                showSnackBar("Added to Favourites")
                            }
                        }
                    }
                } else showSnackBar("Please login to avail this feature")
            }
        }

        openWithButton.setOnClickListener {
            // This popup menu needs to be tied to a view, hence passing this view
            val popUpMenu = PopupMenu(requireContext(), openWithButton)
            popUpMenu.inflate(R.menu.open_with_menu)
            popUpMenu.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.menu_imdb -> {
                        Toast.makeText(requireContext(), "Opening on IMDB...", Toast.LENGTH_SHORT)
                            .show()

                        if (_isItMovie) customTabsIntent.launchUrl(
                            requireContext(),
                            IMDB_BASE_URL.plus(_currentMovie!!.imdbId).toUri()
                        )
                        else
                            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                                viewModel.getTvShowExternalIds(_mediaId!!).data?.imdbId?.let { imdbId ->
                                    customTabsIntent.launchUrl(
                                        requireContext(),
                                        IMDB_BASE_URL.plus(imdbId).toUri()
                                    )
                                }
                            }

                        true
                    }
                    R.id.menu_youtube -> {
                        youtubeVideoKey?.let { key ->
                            Toast.makeText(
                                requireContext(),
                                "Opening on YouTube...",
                                Toast.LENGTH_SHORT
                            ).show()

                            customTabsIntent.launchUrl(
                                requireContext(),
                                YOUTUBE_VIDEO_URL.plus(key).toUri()
                            )
                        } ?: kotlin.run {
                            showSnackBar("YouTube video not available")
                        }
                        true
                    }
                    else -> false
                }
            }
            popUpMenu.show()
        }
    }

    private fun setUpObservers() {

        viewModel.watchProviders.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> binding.apply {
                    _watchProviderUrl = it.data?.results?.iN?.link
                }
                is Resource.Loading -> binding.apply {}
                is Resource.Error -> binding.apply {}
            }
        }

        viewModel.movieDetail.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> binding.apply {
                    showSnackBar(it.message!!)
                    loadingBg.loadingStateLayout.isGone = true
                    if (it.errorType == ErrorType.NETWORK) {
                        errorLayout.statusTextTitle.text = "Connection Error"
                        errorLayout.statusTextDesc.text =
                            "Please check your internet/wifi connection"
                    } else {
                        errorLayout.statusTextTitle.text = "Oops..."
                        errorLayout.statusTextDesc.text = it.message
                    }
                    errorLayout.root.isGone = false
                    mainLayout.isGone = true
                }
                is Resource.Loading -> binding.apply {
                    errorLayout.root.isGone = true
                    loadingBg.loadingStateLayout.isGone = false
                    mainLayout.isGone = true
                }
                is Resource.Success -> binding.apply {
                    _currentMovie = it.data!!
                    val totalVideos = it.data.videos.videosList as ArrayList
                    val trailers: List<VideoResult> = totalVideos.filter { toFilter ->
                        toFilter.type == TRAILER && toFilter.site == YOUTUBE
                    }
                    if (totalVideos.isEmpty()) {
                        // No video to play, not initialing youtube player
                        emptyMoreVideosMsg.isGone = false
                        rvMoreVideosList.isGone = true
                    } else {
                        val trailer = if (trailers.isEmpty()) totalVideos[0] else trailers[0]
                        youtubeVideoKey = trailer.key

                        if (_youTubePlayer == null)
                            initializePlayer(trailer.key)
                        else _youTubePlayer!!.loadVideo(trailer.key, 0f)

                        // removing already played trailer from totalVideos
                        totalVideos.remove(trailer)
                        if (totalVideos.isNotEmpty()) {
                            rvMoreVideosList.scrollToPosition(0)
                            moreVideosAdapter.submitList(totalVideos)
                        } else {
                            emptyMoreVideosMsg.isGone = false
                            rvMoreVideosList.isGone = true
                        }
                    }
                    // Hiding loading bg code in updateDetails method
                    updateDetails(movieDetails = it.data)
                }
            }
        }

        viewModel.tvShowDetail.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> binding.apply {
                    showSnackBar(it.message!!)
                    loadingBg.loadingStateLayout.isGone = true
                    errorLayout.root.isGone = false
                    mainLayout.isGone = true
                }
                is Resource.Loading -> binding.apply {
                    errorLayout.root.isGone = true
                    loadingBg.loadingStateLayout.isGone = false
                    mainLayout.isGone = true
                }
                is Resource.Success -> binding.apply {
                    _currentTvShow = it.data!!
                    _seasonList = it.data.seasons

                    val totalVideos = it.data.videos.videosList as ArrayList
                    val trailers: List<VideoResult> = totalVideos.filter { toFilter ->
                        toFilter.type == TRAILER && toFilter.site == YOUTUBE
                    }
                    if (totalVideos.isEmpty()) {
                        // No video to play, not initialing youtube player
                        emptyMoreVideosMsg.isGone = false
                        rvMoreVideosList.isGone = true
                    } else {
                        val trailer = if (trailers.isEmpty()) totalVideos[0] else trailers[0]
                        youtubeVideoKey = trailer.key

                        if (_youTubePlayer == null)
                            initializePlayer(trailer.key)
                        else _youTubePlayer!!.loadVideo(trailer.key, 0f)

                        // removing already played trailer from totalVideos
                        totalVideos.remove(trailer)
                        if (totalVideos.isNotEmpty()) {
                            moreVideosAdapter.submitList(totalVideos)
                        } else {
                            emptyMoreVideosMsg.isGone = false
                            rvMoreVideosList.isGone = true
                        }
                    }
                    updateDetails(tvDetails = it.data)
                }
            }
        }

        viewModel.mediaCast.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> showSnackBar(it.message!!)
                is Resource.Loading -> binding.rvCrews.scrollToPosition(0)
                is Resource.Success -> {
                    binding.rvCrews.isGone = false
                    binding.castBg.isGone = true
                    castAdapter.submitList(it.data!!.cast)
                }
            }
        }

        viewModel.recommendedMedia.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> showSnackBar(it.message!!)
                is Resource.Loading -> binding.apply {
                    rvRecommendations.scrollToPosition(0)
                }
                is Resource.Success -> binding.apply {
                    recommendationsPlaceholder.isGone = true
                    if (it.data!!.movieResults.isNotEmpty()) {
                        recommendationsAdapter.submitList(it.data.movieResults)
                        rvRecommendations.isGone = false
                    } else {
                        // Show no recommendations msg
                        emptyRecommendationsMsg.isGone = false
                        rvRecommendations.isGone = true
                    }
                }
            }
        }

        viewModel.similarMedia.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> showSnackBar(it.message!!)
                is Resource.Loading -> binding.apply { rvSimilarList.scrollToPosition(0) }
                is Resource.Success -> {
                    similarMediaAdapter.submitList(it.data?.movieResults)
                }
            }
        }

        viewModel.tvSeasonDetail.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> showSnackBar(it.message!!)
                // is Resource.Loading -> { }
                is Resource.Success -> binding.apply {
                    tvEpisodesStatusMsg.isGone = true
                    if (it.data!!.episodes.isNotEmpty()) {
                        rvEpisodes.isGone = false
                        tvShowEpisodesAdapter.submitList(it.data.episodes)
                    } else {
                        rvEpisodes.isGone = true
                        tvEpisodesStatusMsg.isGone = false
                        tvEpisodesStatusMsg.text = "Oops, no episodes are present currently."
                    }
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
                _mediaId = mediaId
                fetchData()
            }
        }
    }

    private fun fetchData() {
        if (!_isItMovie) {
            viewModel.getTvShowDetail(tvId = _mediaId!!)
            viewModel.getTvWatchProviders(tvId = _mediaId!!)
            viewModel.getTvSeasonDetail(
                tvId = _mediaId!!,
                seasonNumber = _currentSeasonNumber
            )
        } else {
            viewModel.getMovieDetail(movieId = _mediaId!!)
            viewModel.getMovieWatchProviders(movieId = _mediaId!!)
        }
        viewModel.getMediaCast(_mediaId!!, _isItMovie)
        viewModel.getRecommendationsMedia(mediaId = _mediaId!!, _isItMovie)
        viewModel.getSimilarMedia(mediaId = _mediaId!!, _isItMovie)
    }

    override fun onResume() {
        super.onResume()

        parentFragmentManager.setFragmentResultListener(
            MEDIA_RE_PLAY_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            bundle.getInt(MEDIA_ID_KEY).let {
                _mediaId = it
                _isItMovie = bundle.getBoolean(IS_IT_A_MOVIE_KEY)
                binding.scrollView.scrollY = 0
                _youTubePlayer?.pause()
                fetchData()
            }
        }

        if (!_isItMovie) {
            parentFragmentManager.setFragmentResultListener(
                SELECTED_SEASON_POSITION,
                viewLifecycleOwner
            ) { _, bundle ->
                _currentSeasonNumber = bundle.getInt(SEASON_NUMBER, 1)
                binding.pickSeasonBtn.text = "Season $_currentSeasonNumber"
                _mediaId?.let {
                    viewModel.getTvSeasonDetail(tvId = it, seasonNumber = _currentSeasonNumber)
                }
            }
        }
    }

    private fun updateDetails(
        movieDetails: MovieDetailResponse? = null,
        tvDetails: TvShowDetailsResponse? = null
    ) = binding.apply {
        // Now, making loading state gone & main layout visible
        loadingBg.loadingStateLayout.isGone = true
        mainLayout.isGone = false

        if (_isItMovie && movieDetails != null) {
            thumbnailContainer.backdropImage.load(
                TMDB_IMAGE_BASE_URL_W780.plus(movieDetails.backdropPath)
            )
            titleText.text = movieDetails.title
            overviewText.text = movieDetails.overview
            yearText.formatMediaDate(movieDetails.releaseDate)
            ratingText.text = String.format("%.1f", movieDetails.voteAverage)
            tvGenres.text = movieDetails.genres.joinToString("  •  ") { it.name }
            runtimeText.isGone = false
            runTimeDivider.isGone = false
            runtimeText.text = "${movieDetails.runtime / 60} h ${movieDetails.runtime % 60} min"
        } else {
            thumbnailContainer.backdropImage.load(
                TMDB_IMAGE_BASE_URL_W780.plus(tvDetails!!.backdropPath)
            )
            titleText.text = tvDetails.name
            overviewText.text = tvDetails.overview
            yearText.formatMediaDate(tvDetails.firstAirDate)
            runtimeText.isGone = true
            runTimeDivider.isGone = true
            ratingText.text = String.format("%.1f", tvDetails.voteAverage)
            tvGenres.text = tvDetails.genres.joinToString("  •  ") { it.name }
        }
    }

    private fun setUpTabLayout() = binding.apply {
        onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    // Recommendations Tab
                    0 -> {
                        rvSimilarList.isGone = true
                        recommendationsContainer.isGone = false
                    }
                    // More like this tab
                    1 -> {
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
        // Setting up Cast Adapter
        castAdapter = CastListAdapter {
            openCastKnownForDialog(it)
        }
        rvCrews.setHasFixedSize(true)
        rvCrews.adapter = castAdapter

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
        recommendationsAdapter = RecommendationsAdapter {
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

    private fun openCastKnownForDialog(cast: Cast) {
        val action = PlayerFragmentDirections.actionPlayerFragmentToCastKnownForFragment(
            personId = cast.id,
            name = cast.name,
            profilePath = cast.profilePath,
            knownForDepartment = cast.knownForDepartment,
        )

        safeFragmentNavigation(
            navController = findNavController(),
            currentFragmentId = R.id.playerFragment,
            action = action
        )
    }

    private fun initializePlayer(videoKey: String?) = binding.apply {
        youTubePlayerListener = object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                videoKey?.let {
                    thumbnailContainer.container.isGone = true
                    youtubePlayerView.isGone = false
                    _youTubePlayer = youTubePlayer
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                        _youTubePlayer!!.loadVideo(it, 0f)
                    }
                }
            }
        }
        youtubePlayerView.addYouTubePlayerListener(youTubePlayerListener!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onTabSelectedListener?.let { binding.mediaTabLayout.removeOnTabSelectedListener(it) }
        youTubePlayerListener?.let { binding.youtubePlayerView.removeYouTubePlayerListener(it) }
        onTabSelectedListener = null
        _binding = null
    }
}
