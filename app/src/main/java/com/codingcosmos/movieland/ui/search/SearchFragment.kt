package com.codingcosmos.movieland.ui.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.codingcosmos.movieland.R
import com.codingcosmos.movieland.databinding.FragmentSearchBinding
import com.codingcosmos.movieland.ui.home.HorizontalAdapter
import com.codingcosmos.movieland.utils.Constants.GENRES_ID_LIST_KEY
import com.codingcosmos.movieland.utils.Constants.IS_IT_A_MOVIE_KEY
import com.codingcosmos.movieland.utils.Constants.MEDIA_ID_KEY
import com.codingcosmos.movieland.utils.Constants.MEDIA_IMAGE_KEY
import com.codingcosmos.movieland.utils.Constants.MEDIA_OVERVIEW_KEY
import com.codingcosmos.movieland.utils.Constants.MEDIA_RATING_KEY
import com.codingcosmos.movieland.utils.Constants.MEDIA_SEND_REQUEST_KEY
import com.codingcosmos.movieland.utils.Constants.MEDIA_TITLE_KEY
import com.codingcosmos.movieland.utils.Constants.MEDIA_YEAR_KEY
import com.codingcosmos.movieland.utils.ErrorType
import com.codingcosmos.movieland.utils.Resource
import com.codingcosmos.movieland.utils.safeFragmentNavigation
import com.codingcosmos.movieland.utils.showSnackBar
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var _query: String? = null
    private val searchViewModel: SearchViewModel by viewModels()
    private var _binding: FragmentSearchBinding? = null
    private lateinit var navController: NavController
    private lateinit var horizontalAdapter: HorizontalAdapter
    private lateinit var topSearchesAdapter: TopSearchesAdapter
    private var onTabSelectedListener: TabLayout.OnTabSelectedListener? = null
    private var job: Job? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerViewAndNav()
        setUpClickListeners()

        searchViewModel.trendingMovies(isMovie = searchViewModel.isMovie)
        searchViewModel.trendingMedia.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> binding.apply {
                    mainLayout.alpha = 1F
                    progressBarOuter.isGone = true
                    searchingProgressBar.isGone = true
                    showSnackBar(it.message!!)
                    searchedResultRv.isGone = true
                    topSearchesRv.isGone = true
                    if (it.errorType == ErrorType.NETWORK) {
                        errorLayout.statusTextTitle.text = "Connection Error"
                        errorLayout.statusTextDesc.text =
                            "Please check your internet/wifi connection"
                    } else {
                        errorLayout.statusTextTitle.text = "Oops..."
                        errorLayout.statusTextDesc.text = it.message
                    }
                    errorLayout.root.isGone = false
                    emptySearch.isGone = true
                }
                is Resource.Loading -> binding.apply {
                    progressBarOuter.isGone = false
                    mainLayout.alpha = 0.4F
                }
                is Resource.Success -> binding.apply {
                    mainLayout.alpha = 1F
                    progressBarOuter.isGone = true
                    searchingProgressBar.isGone = true
                    errorLayout.root.isGone = true
                    emptySearch.isGone = true
                    topSearchesRv.isGone = false
                    searchedResultRv.isGone = true
                    topSearchesAdapter.submitList(it.data!!.movieResults)
                }
            }
        }

        onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> binding.apply {
                        searchViewModel.isMovie = true
                        if (_query.isNullOrEmpty()) {
                            searchViewModel.trendingMovies(isMovie = searchViewModel.isMovie)
                        }
                        _query?.let {
                            if (it.isNotEmpty()) {
                                searchViewModel.searchMedia(
                                    isMovie = searchViewModel.isMovie,
                                    searchQuery = it
                                )
                                searchingProgressBar.isGone = false
                                searchedResultRv.isGone = true
                                errorLayout.root.isGone = true
                                emptySearch.isGone = true
                            }
                        }
                    }
                    1 -> binding.apply {
                        searchViewModel.isMovie = false
                        if (_query.isNullOrEmpty()) {
                            searchViewModel.trendingMovies(isMovie = searchViewModel.isMovie)
                        }
                        _query?.let {
                            if (it.isNotEmpty()) {
                                searchViewModel.searchMedia(
                                    isMovie = searchViewModel.isMovie,
                                    searchQuery = it
                                )
                                searchingProgressBar.isGone = false
                                searchedResultRv.isGone = true
                                errorLayout.root.isGone = true
                                emptySearch.isGone = true
                            }
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        }

        binding.tabLayout.addOnTabSelectedListener(onTabSelectedListener!!)

        binding.searchQueryEt.doOnTextChanged { text, _, _, _ ->
            text?.let {
                _query = it.trim().toString()
                binding.apply {
                    if (it.isNotEmpty() && it.isNotBlank()) {
                        eraseQueryBtn.isGone = false
                        querySearchIcon.isVisible = false
                        // Show searching progress bar
                        searchText.text = "Searched Results"
                        searchingProgressBar.isGone = false
                        emptySearch.isGone = true
                        errorLayout.root.isGone = true
                        topSearchesRv.isGone = true
                        searchedResultRv.isGone = true // Make it visible on getting results
                        performSearch(it.trim().toString())
                    } else {
                        searchViewModel.trendingMovies(isMovie = searchViewModel.isMovie)
                        eraseQueryBtn.isGone = true
                        querySearchIcon.isVisible = true
                        searchText.text = "Top Searches"
                        topSearchesRv.isGone = false
                        searchedResultRv.isGone = true
                        searchingProgressBar.isGone = true
                        emptySearch.isGone = true
                        // also cancel the job
                        job?.cancel()
                    }
                }
            }
        }

        searchViewModel.searchedMedia.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> binding.apply {
                    showSnackBar(it.message!!)
                    searchedResultRv.isGone = true
                    topSearchesRv.isGone = true
                    if (it.errorType == ErrorType.NETWORK) {
                        errorLayout.statusTextTitle.text = "Connection Error"
                        errorLayout.statusTextDesc.text =
                            "Please check your internet/wifi connection"
                    } else {
                        errorLayout.statusTextTitle.text = "Oops..."
                        errorLayout.statusTextDesc.text = it.message
                    }
                    errorLayout.root.isGone = false
                }
                // is Resource.Loading -> binding.apply { }
                is Resource.Success -> binding.apply {
                    searchingProgressBar.isGone = true
                    if (it.data!!.movieResults.isNotEmpty()) {
                        horizontalAdapter.submitList(it.data.movieResults)
                        searchedResultRv.isGone = false
                    } else {
                        emptySearch.isGone = false
                        searchedResultRv.isGone = true
                    }
                }
            }
        }

        binding.eraseQueryBtn.setOnClickListener {
            binding.searchQueryEt.text.clear()
            hideKeyboard()
        }
    }

    private fun setUpClickListeners() = binding.apply {
        toolbar.setNavigationOnClickListener {
            hideKeyboard()
            navController.popBackStack()
        }

        errorLayout.retryButton.setOnClickListener {
            binding.searchingProgressBar.isGone = false
            binding.emptySearch.isGone = true
            binding.errorLayout.root.isGone = true
            binding.searchedResultRv.isGone = true
            binding.topSearchesRv.isGone = true
            if (searchQueryEt.text.trim().toString().isNotEmpty()) {
                searchViewModel.searchMedia(
                    isMovie = searchViewModel.isMovie,
                    searchQuery = searchQueryEt.text.trim().toString()
                )
            } else searchViewModel.trendingMovies(isMovie = searchViewModel.isMovie)
        }
    }

    private fun performSearch(searchQuery: String) {
        job?.cancel()
        job = viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            // first let's wait for change in input search query
            delay(800)
            searchViewModel.searchMedia(
                isMovie = searchViewModel.isMovie,
                searchQuery = searchQuery
            )
        }
    }

    private fun hideKeyboard() {
        val imm: InputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun setUpRecyclerViewAndNav() {
        navController = findNavController()
        binding.toolbar.setNavigationOnClickListener {
            navController.popBackStack()
        }

        // Top Searches Adapter
        topSearchesAdapter = TopSearchesAdapter {

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
                currentFragmentId = R.id.navigation_search,
                actionId = R.id.action_navigation_search_to_detailFragment
            )
        }
        binding.topSearchesRv.setHasFixedSize(true)
        binding.topSearchesRv.adapter = topSearchesAdapter

        // Searched Results Adapter
        horizontalAdapter = HorizontalAdapter {

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
                currentFragmentId = R.id.navigation_search,
                actionId = R.id.action_navigation_search_to_detailFragment
            )
        }
        binding.searchedResultRv.setHasFixedSize(true)
        binding.searchedResultRv.adapter = horizontalAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        onTabSelectedListener = null
    }
}
