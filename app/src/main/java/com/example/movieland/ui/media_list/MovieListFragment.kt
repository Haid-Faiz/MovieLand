package com.example.movieland.ui.media_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import com.example.movieland.R
import com.example.movieland.databinding.FragmentMovieListBinding
import com.example.movieland.ui.home.HorizontalPagerAdapter
import com.example.movieland.utils.Constants
import com.example.movieland.utils.Constants.BOLLYWOOD_MOVIES
import com.example.movieland.utils.Constants.GENRES_ID_LIST_KEY
import com.example.movieland.utils.Constants.IS_IT_A_MOVIE_KEY
import com.example.movieland.utils.Constants.MEDIA_ID_KEY
import com.example.movieland.utils.Constants.MEDIA_IMAGE_KEY
import com.example.movieland.utils.Constants.MEDIA_OVERVIEW_KEY
import com.example.movieland.utils.Constants.MEDIA_RATING_KEY
import com.example.movieland.utils.Constants.MEDIA_SEND_REQUEST_KEY
import com.example.movieland.utils.Constants.MEDIA_TITLE_KEY
import com.example.movieland.utils.Constants.MEDIA_YEAR_KEY
import com.example.movieland.utils.safeFragmentNavigation
import com.example.movieland.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MovieListFragment : Fragment() {

    private var _binding: FragmentMovieListBinding? = null
    private val binding get() = _binding!!
    private lateinit var horizontalAdapter: HorizontalPagerAdapter
    private val args: MovieListFragmentArgs by navArgs()

    @Inject
    lateinit var trendingViewModelFactory: TrendingViewModel.TrendingViewModelFactory

    private val trendingViewModel: TrendingViewModel by viewModels {
        TrendingViewModel.providesFactory(
            assistedFactory = trendingViewModelFactory,
            mediaCategory = args.mediaCategory
        )
    }

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        binding.toolbar.title = args.mediaCategory
        setUpRecyclerViewAndNav()

        trendingViewModel.categoryMedia.observe(viewLifecycleOwner) {
            horizontalAdapter.submitData(lifecycle, it)
        }
    }

    private fun setUpRecyclerViewAndNav() {
        binding.toolbar.setNavigationOnClickListener {
            navController.popBackStack()
        }

        horizontalAdapter = HorizontalPagerAdapter(
            onPosterClick = if (args.mediaCategory != BOLLYWOOD_MOVIES) {
                {
                    // callback of Poster click
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
                        currentFragmentId = R.id.movieListFragment,
                        actionId = R.id.action_movieListFragment_to_detailFragment
                    )
                }
            } else {

                // BollyWood item click
                {
                    parentFragmentManager.setFragmentResult(
                        Constants.MEDIA_PLAY_REQUEST_KEY,
                        bundleOf(
                            MEDIA_ID_KEY to it.id,
                            IS_IT_A_MOVIE_KEY to true
                        )
                    )
                    safeFragmentNavigation(
                        navController = navController,
                        currentFragmentId = R.id.movieListFragment,
                        actionId = R.id.action_movieListFragment_to_playerFragment
                    )
                }

            },
        )
//        horizontalAdapter.withLoadStateHeaderAndFooter(
//            footer = PagingStateAdapter { horizontalAdapter },
//            header = PagingStateAdapter { horizontalAdapter }
//        )

        horizontalAdapter.addLoadStateListener {
            when (it.refresh) {
                is LoadState.NotLoading -> binding.apply {
                    progressBar.isGone = true
                    listRecyclerview.isGone = false
                }

                LoadState.Loading -> binding.apply {
                    progressBar.isGone = false
                    listRecyclerview.isGone = true
                }

                is LoadState.Error -> binding.apply {
                    progressBar.isGone = true
                    listRecyclerview.isGone = false
//                    handleExceptions((it.refresh as LoadState.Error).error)
                    showSnackBar("Something went wrong")
                }
            }
        }

        binding.listRecyclerview.setHasFixedSize(true)
        binding.listRecyclerview.adapter = horizontalAdapter
    }
}