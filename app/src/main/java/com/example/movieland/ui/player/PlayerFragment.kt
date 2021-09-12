package com.example.movieland.ui.player

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.movieland.databinding.FragmentPlayerBinding
import com.example.movieland.utils.Constants.YOUTUBE_VIDEO_URL
import com.example.movieland.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer

import androidx.annotation.NonNull
import androidx.core.view.isGone
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.datasource.remote.models.responses.VideoResult
import com.example.movieland.R
import com.example.movieland.ui.home.HorizontalAdapter
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
    private lateinit var similarMoviesAdapter: HorizontalAdapter
    private var isItMovie: Boolean = true
    private lateinit var onTabSelectedListener: TabLayout.OnTabSelectedListener
//    private var videoKey: String? = null
      // Player state helper variables
//    private var playWhenReady = true
//    private var currentWindow = 0
//    private var playbackPosition = 0L
    // Integrate

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
        setUpTabLayout()

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
                        ratingText.text = it.data.popularity.toString()
                    }
                }
            }
        }

        viewModel.similarMovies.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> TODO()
                is Resource.Loading -> {
                }
                is Resource.Success -> {
                    similarMoviesAdapter.submitList(it.data?.movieResults)
                }
            }
        }
    }

    private fun setUpFragmentResultListeners() {
        parentFragmentManager.setFragmentResultListener(
            KEY_VIDEO_REQUEST,
            viewLifecycleOwner
        ) { _, bundle ->
            isItMovie = bundle.getBoolean(KEY_IS_MOVIE)
            binding.apply {
                moviesTabLayout.isGone = !isItMovie
//                showsTabLayout.isGone = isMovie
                showsSeasonsBtn.isGone = isItMovie
            }
            bundle.getInt(KEY_MOVIE_ID).let { movieId: Int ->
                // Fetch movie detail
                viewModel.getMovieDetail(movieId)
                viewModel.getSimilarMovies(movieId)
            }
        }
    }

    private fun setUpTabLayout() = binding.apply {
        if (isItMovie) {
            // Removing TV shows Seasons tabs
            moviesTabLayout.removeTabAt(0)
            rvEpisodesList.isGone = isItMovie
        }

        onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (isItMovie) {
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
                        }
                        1 -> {
                            // Similar like this tab
                            rvEpisodesList.isGone = true
                            rvSimilarList.isGone = false
                            rvMoreVideosList.isGone = true
                        }
                        2 -> {
                            // Trailers and more tab
                            rvEpisodesList.isGone = true
                            rvSimilarList.isGone = true
                            rvMoreVideosList.isGone = false
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        }
        moviesTabLayout.addOnTabSelectedListener(onTabSelectedListener)
    }

    private fun setUpRecyclerViewAndUi() = binding.apply {
        rvSimilarList.setHasFixedSize(true)
        similarMoviesAdapter = HorizontalAdapter {
            findNavController().navigate(R.id.action_playerFragment_to_detailFragment)
        }
        rvSimilarList.adapter = similarMoviesAdapter
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
    }
}