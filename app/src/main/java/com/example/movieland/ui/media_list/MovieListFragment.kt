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
import com.example.movieland.data.paging.PagingStateAdapter
import com.example.movieland.databinding.FragmentMovieListBinding
import com.example.movieland.ui.home.HorizontalPagerAdapter
import com.example.movieland.utils.Constants.BOLLYWOOD_MOVIES
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
import com.example.movieland.utils.ErrorType
import com.example.movieland.utils.handleExceptions
import com.example.movieland.utils.safeFragmentNavigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MovieListFragment : Fragment() {

    private var _binding: FragmentMovieListBinding? = null
    private val binding get() = _binding!!
    private lateinit var horizontalAdapter: HorizontalPagerAdapter
    private val args: MovieListFragmentArgs by navArgs()

    @Inject
    lateinit var movieListViewModelFactory: MovieListViewModel.TrendingViewModelFactory

    private val viewModel: MovieListViewModel by viewModels {
        MovieListViewModel.providesFactory(
            assistedFactory = movieListViewModelFactory,
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

        viewModel.categoryWiseMediaList.observe(viewLifecycleOwner) {
            horizontalAdapter.submitData(lifecycle, it)
        }

        binding.errorLayout.retryButton.setOnClickListener { horizontalAdapter.retry() }
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
                        MEDIA_PLAY_REQUEST_KEY,
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

        binding.listRecyclerview.adapter = horizontalAdapter.withLoadStateHeaderAndFooter(
            footer = PagingStateAdapter { horizontalAdapter.retry() },
            header = PagingStateAdapter { horizontalAdapter.retry() }
        )
        binding.listRecyclerview.setHasFixedSize(true)

        horizontalAdapter.addLoadStateListener {
            when (it.refresh) {
                is LoadState.NotLoading -> binding.apply {
                    progressBar.isGone = true
                    listRecyclerview.isGone = false
                }

                LoadState.Loading -> binding.apply {
                    errorLayout.root.isGone = true
                    progressBar.isGone = false
                    listRecyclerview.isGone = true
                }

                is LoadState.Error -> binding.apply {
                    progressBar.isGone = true
                    errorLayout.root.isGone = false
                    listRecyclerview.isGone = true
                    val errorType: ErrorType =
                        handleExceptions((it.refresh as LoadState.Error).error)
                    if (errorType == ErrorType.NETWORK) {
                        // Network problem
                        errorLayout.statusTextTitle.text = "Connection Error"
                        errorLayout.statusTextDesc.text = "Please check your internet connection"
                    } else {
                        // Http error or unknown
                        errorLayout.statusTextTitle.text = "Oops.. Something went wrong"
                        errorLayout.statusTextDesc.text = "Please try again"
                    }
                }
            }
        }
    }
}
