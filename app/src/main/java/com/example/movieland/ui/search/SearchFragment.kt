package com.example.movieland.ui.search

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.movieland.R
import com.example.movieland.databinding.FragmentSearchBinding
import com.example.movieland.ui.home.HorizontalAdapter
import com.example.movieland.utils.Constants
import com.example.movieland.utils.Constants.GENRES_ID_LIST_KEY
import com.example.movieland.utils.Constants.IS_IT_A_MOVIE_KEY
import com.example.movieland.utils.Constants.MEDIA_ID_KEY
import com.example.movieland.utils.Constants.MEDIA_IMAGE_KEY
import com.example.movieland.utils.Constants.MEDIA_OVERVIEW_KEY
import com.example.movieland.utils.Constants.MEDIA_RATING_KEY
import com.example.movieland.utils.Constants.MEDIA_SEND_REQUEST_KEY
import com.example.movieland.utils.Constants.MEDIA_TITLE_KEY
import com.example.movieland.utils.Constants.MEDIA_YEAR_KEY
import com.example.movieland.utils.Resource
import com.example.movieland.utils.safeFragmentNavigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private val searchViewModel: SearchViewModel by viewModels()
    private var _binding: FragmentSearchBinding? = null
    private lateinit var navController: NavController
    private lateinit var horizontalAdapter: HorizontalAdapter
    private lateinit var topSearchesAdapter: TopSearchesAdapter
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

        binding.searchQueryEt.doOnTextChanged { text, start, before, count ->
            text?.let {
                if (it.isNotEmpty() && it.isNotBlank()) {
                    binding.eraseQueryBtn.isGone = false
                    binding.querySearchIcon.isVisible = false
                    binding.emptySearchContent.isGone = true
                    binding.searchedResultLl.isGone = false
                    // Show searching progress bar
                    binding.searchingProgressBar.isGone = false
                    binding.searchedResultRv.isGone = true
                    performSearch(it.trim().toString())
                } else {
                    binding.eraseQueryBtn.isGone = true
                    binding.querySearchIcon.isVisible = true
                    binding.emptySearchContent.isGone = false
                    binding.searchedResultLl.isGone = true
                }
            }
        }

        searchViewModel.searchedMovies.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> {
                    TODO()
                }
                is Resource.Loading -> binding.apply {}
                is Resource.Success -> binding.apply {
                    searchingProgressBar.isGone = true
                    searchedResultRv.isGone = false
                    horizontalAdapter.submitList(it.data?.movieResults)
                }
            }
        }

        searchViewModel.trendingMovies()
        searchViewModel.trendingMovies.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> TODO()
                is Resource.Loading -> TODO()
                is Resource.Success -> {
                    topSearchesAdapter.submitList(it.data?.movieResults)
                }
            }
        }

        binding.eraseQueryBtn.setOnClickListener {
            binding.searchQueryEt.text.clear()
            hideKeyboard()
        }
    }

    private fun performSearch(searchQuery: String) {
        job?.cancel()
        job = viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            // first let's wait for change in input search query
            delay(800)
            searchViewModel.searchedMovies(searchQuery)
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
    }
}