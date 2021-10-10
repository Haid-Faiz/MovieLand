package com.example.movieland.ui.home.lists

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
import com.example.movieland.R
import com.example.movieland.databinding.FragmentMovieListBinding
import com.example.movieland.ui.home.HomeViewModel
import com.example.movieland.ui.home.HorizontalAdapter
import com.example.movieland.utils.Constants
import com.example.movieland.utils.Constants.GENRES_ID_LIST_KEY
import com.example.movieland.utils.Constants.IS_IT_A_MOVIE_KEY
import com.example.movieland.utils.Constants.MEDIA_CATEGORY_SEND_REQUEST_KEY
import com.example.movieland.utils.Constants.MEDIA_ID_KEY
import com.example.movieland.utils.Constants.MEDIA_IMAGE_KEY
import com.example.movieland.utils.Constants.MEDIA_KEY
import com.example.movieland.utils.Constants.MEDIA_OVERVIEW_KEY
import com.example.movieland.utils.Constants.MEDIA_RATING_KEY
import com.example.movieland.utils.Constants.MEDIA_SEND_REQUEST_KEY
import com.example.movieland.utils.Constants.MEDIA_TITLE_KEY
import com.example.movieland.utils.Constants.MEDIA_TYPE_KEY
import com.example.movieland.utils.Constants.MEDIA_YEAR_KEY
import com.example.movieland.utils.Resource
import com.example.movieland.utils.safeFragmentNavigation
import com.example.movieland.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieListFragment : Fragment() {

    private var _binding: FragmentMovieListBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var horizontalAdapter: HorizontalAdapter
    private lateinit var navController: NavController
    private var isItMovieList: Boolean = false

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
        setUpRecyclerViewAndNav()

        parentFragmentManager.setFragmentResultListener(
            MEDIA_CATEGORY_SEND_REQUEST_KEY,
            viewLifecycleOwner
        ) { key, bundle ->
            bundle.getBoolean(IS_IT_A_MOVIE_KEY, false).let {
                isItMovieList = it
                if (isItMovieList) {
                    binding.toolbar.title = "Trending Movies"
                    homeViewModel.getTrendingMovies()
                } else {
                    binding.toolbar.title = "TV Programmes"
                    homeViewModel.getTrendingTvShows()
                }
            }
        }

        homeViewModel.movieListTrending.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> binding.apply {
                    progressBar.isGone = true
                    listRecyclerview.isGone = false
                    showSnackBar(it.message ?: "Something went wrong")
                }
                is Resource.Loading -> binding.apply {
                    progressBar.isGone = false
                    listRecyclerview.isGone = true
                }
                is Resource.Success -> binding.apply {
                    progressBar.isGone = true
                    listRecyclerview.isGone = false
                    horizontalAdapter.submitList(it.data?.movieResults)
                }
            }
        }

        homeViewModel.tvListTrending.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> binding.apply {
                    progressBar.isGone = true
                    listRecyclerview.isGone = false
                    showSnackBar(it.message ?: "Something went wrong")
                }
                is Resource.Loading -> binding.apply {
                    progressBar.isGone = false
                    listRecyclerview.isGone = true
                }
                is Resource.Success -> binding.apply {
                    progressBar.isGone = true
                    listRecyclerview.isGone = false
                    horizontalAdapter.submitList(it.data?.movieResults)
                }
            }
        }
    }

    private fun setUpRecyclerViewAndNav() {
        navController = findNavController()
        binding.toolbar.setNavigationOnClickListener {
            navController.popBackStack()
        }
        horizontalAdapter = HorizontalAdapter {
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
        binding.listRecyclerview.setHasFixedSize(true)
        binding.listRecyclerview.adapter = horizontalAdapter
    }
}