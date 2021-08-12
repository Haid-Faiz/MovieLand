package com.example.movieland.ui.home.lists

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.movieland.databinding.FragmentMovieListBinding
import com.example.movieland.ui.home.HomeViewModel
import com.example.movieland.ui.home.HorizontalAdapter
import com.example.movieland.utils.Resource

class MovieListFragment : Fragment() {

    private var _binding: FragmentMovieListBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var horizontalAdapter: HorizontalAdapter
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
        setUpRecyclerViewAndNav()

        parentFragmentManager.setFragmentResultListener(
            "media_key",
            viewLifecycleOwner
        ) { key, bundle ->
            Log.d("key_bundle", "onViewCreated: $key")
            val value = bundle.getString("movie")
            _binding!!.toolbar.title =
                if (value == "movies_mania") "Trending Movies" else "TV Programmes"
            if (value == "movies_mania") homeViewModel.getTrendingMovies()
            else homeViewModel.getTrendingTvShows()
        }

        homeViewModel.movieListUpcoming.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> TODO()
                is Resource.Loading -> TODO()
                is Resource.Success -> {
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
        horizontalAdapter = HorizontalAdapter()
        binding.listRecyclerview.setHasFixedSize(true)
        binding.listRecyclerview.adapter = horizontalAdapter
    }
}