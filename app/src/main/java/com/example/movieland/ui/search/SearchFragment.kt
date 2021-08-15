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
import com.example.movieland.utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

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
                    binding.querySearchIcon.isGone = true
                    binding.emptySearchContent.isGone = true
                    binding.searchedResultLl.isGone = false
                    performSearch(it.trim().toString())
                } else {
                    binding.eraseQueryBtn.isGone = true
                    binding.querySearchIcon.isGone = false
                    binding.emptySearchContent.isGone = false
                    binding.searchedResultLl.isGone = true
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

        searchViewModel.searchedMovies.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> {
//                    TODO()
                }
                is Resource.Loading -> TODO()
                is Resource.Success -> {
                    horizontalAdapter.submitList(it.data?.movieResults)
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
            navController.navigate(R.id.action_navigation_search_to_detailFragment)
            parentFragmentManager.setFragmentResult(
                "home_movie_key",
                bundleOf(
                    "movie_title" to it.title,
                    "movie_overview" to it.overview,
                    "movie_image_url" to it.posterPath,
                    "movie_year" to it.releaseDate
                )
            )
        }
        binding.topSearchesRv.setHasFixedSize(true)
        binding.topSearchesRv.adapter = topSearchesAdapter
        horizontalAdapter = HorizontalAdapter {
            navController.navigate(R.id.action_navigation_search_to_detailFragment)
            parentFragmentManager.setFragmentResult(
                "home_movie_key",
                bundleOf(
                    "movie_title" to it.title,
                    "movie_overview" to it.overview,
                    "movie_image_url" to it.posterPath,
                    "movie_year" to it.releaseDate
                )
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