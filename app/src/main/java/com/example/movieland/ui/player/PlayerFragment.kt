package com.example.movieland.ui.player

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.movieland.databinding.FragmentPlayerBinding
import com.example.movieland.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer

import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.datasource.remote.models.responses.Season
import com.example.datasource.remote.models.responses.VideoResult
import com.example.movieland.R
import com.example.movieland.ui.home.HorizontalAdapter
import com.example.movieland.ui.player.adapters.MoreVideosAdapter
import com.example.movieland.ui.player.adapters.TvShowEpisodesAdapter
import com.example.movieland.utils.Constants.KEY_IS_MOVIE
import com.example.movieland.utils.Constants.KEY_MOVIE_ID
import com.example.movieland.utils.Constants.KEY_VIDEO_REQUEST
import com.example.movieland.utils.Constants.TMDB_IMAGE_BASE_URL_W780
import com.google.android.material.tabs.TabLayout

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener

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
    private var _isItMovie: Boolean = true
    private var _id: Int? = null
    private lateinit var onTabSelectedListener: TabLayout.OnTabSelectedListener
    private var _seasonList: List<Season>? = null
    private var _currentSeasonNumber: Int = 1
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
            val shakeAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.shake_anim)
            rateButton.startAnimation(shakeAnim)
        }
    }

    private fun setUpObservers() {
        viewModel.movieDetail.observe(viewLifecycleOwner) {
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
                    _id?.let { id ->
                        viewModel.getSimilarMovies(id)
                    }
                    moreVideosAdapter.submitList(it.data!!.videos.videosList)

                    val a: List<VideoResult> = it.data!!.videos.videosList.filter { toFilter ->
                        toFilter.type == "Trailer" && toFilter.site == "Youtube"
                    }
                    val videoKey = it.data.videos.videosList[0].key
                    Log.d("VideoKey", "onViewCreated: $videoKey   | a: $a")
                    initializePlayer(videoKey)

                    binding.apply {
                        binding.thumbnailContainer.backdropImage.load(
                            TMDB_IMAGE_BASE_URL_W780.plus(it.data.backdropPath)
                        )
                        titleText.text = it.data.title
                        overviewText.text = it.data.overview
                        yearText.text = it.data.releaseDate
                        runtimeText.text = it.data.runtime.toString()
                        ratingText.text = String.format("%.2f", it.data.voteAverage)
                    }
                }
            }
        }

        viewModel.tvShowDetail.observe(viewLifecycleOwner) {
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

                    _id?.let { id ->
                        viewModel.getTvSeasonDetail(
                            tvId = id,
                            seasonNumber = _currentSeasonNumber
                        )
                        viewModel.getSimilarMovies(id)
                    }
                    moreVideosAdapter.submitList(it.data.videos.videosList)

                    val videoKey = it.data.videos.videosList[0].key
                    Log.d("VideoKey", "onViewCreated: $videoKey")
                    initializePlayer(videoKey)


                    binding.apply {
                        binding.thumbnailContainer.backdropImage.load(
                            TMDB_IMAGE_BASE_URL_W780.plus(it.data.backdropPath)
                        )
                        titleText.text = it.data.name
                        overviewText.text = it.data.overview
                        yearText.text = it.data.firstAirDate
//                        runtimeText.text = it.data.runtime.toString()
                        ratingText.text = String.format("%.1f", it.data.voteAverage)
                    }
                }
            }
        }

        viewModel.similarMovies.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "${it?.message}", Toast.LENGTH_LONG).show()
                }
                is Resource.Loading -> {
                }
                is Resource.Success -> {
                    similarMoviesAdapter.submitList(it.data?.movieResults)
                }
            }
        }

        viewModel.tvSeasonDetail.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> {
                }
                is Resource.Loading -> {
                }
                is Resource.Success -> {
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
            Log.d("isItaMovie", "setUpFragmentResultListeners: $_isItMovie")
            binding.apply {
                pickSeasonBtn.isGone = _isItMovie
                moviesTabLayout.isGone = !_isItMovie
                showsTabLayout.isGone = _isItMovie
                rvEpisodesList.isGone = _isItMovie
                rvSimilarList.isGone = !_isItMovie
            }

            setUpTabLayout()

            bundle.getInt(KEY_MOVIE_ID).let { movieId: Int ->
                _id = movieId
                if (_isItMovie)
                // Fetch movie detail
                    viewModel.getMovieDetail(movieId)
                else
                    viewModel.getTvShowDetail(tvId = movieId) // using same name for movie and tv id
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
        if (_isItMovie) moviesTabLayout.addOnTabSelectedListener(onTabSelectedListener)
        else showsTabLayout.addOnTabSelectedListener(onTabSelectedListener)
    }

    private fun setUpRecyclerViewAndUi() = binding.apply {
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
        binding.moviesTabLayout.removeOnTabSelectedListener(onTabSelectedListener)
        binding.showsTabLayout.removeOnTabSelectedListener(onTabSelectedListener)
    }
}