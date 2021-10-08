package com.example.movieland.ui.coming_soon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.datasource.remote.models.requests.AddToWatchListRequest
import com.example.datasource.remote.models.responses.MovieResult
import com.example.movieland.R
import com.example.movieland.databinding.FragmentComingSoonBinding
import com.example.movieland.ui.genres.GenreAdapter
import com.example.movieland.utils.Constants
import com.example.movieland.utils.Helpers
import com.example.movieland.utils.Resource
import com.example.movieland.utils.showSnackBar
import com.jackandphantom.carouselrecyclerview.CarouselLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ComingSoonFragment : Fragment() {

    private var _binding: FragmentComingSoonBinding? = null
    private lateinit var adapter: ComingSoonAdapter
    private lateinit var genreAdapter: GenreAdapter
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private val viewModel: ComingSoonViewModel by viewModels()
    private var isFirstPrinted: Boolean = false
    private lateinit var popInAnim: Animation
    private var _movieResult: MovieResult? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComingSoonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        popInAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.poping_anim)
        setUpRecyclerview()

        viewModel.getComingSoonMovies()
        viewModel.comingSoon.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> binding.apply {
                    showSnackBar(it.message ?: "Something went wrong")
                }
                is Resource.Loading -> binding.apply {
                    progressBar.isGone = false
                    mainLayout.isGone = true
                }
                is Resource.Success -> binding.apply {
                    progressBar.isGone = true
                    mainLayout.isGone = false
                    adapter.submitList(it.data?.movieResults)
                }
            }
        }
    }

    private fun setUpRecyclerview() {
        val popingAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.poping_anim)
        navController = findNavController()
        adapter = ComingSoonAdapter(
            onImageClick = {
                // Goto Player Fragment
                parentFragmentManager.setFragmentResult(
                    Constants.MEDIA_PLAY_REQUEST_KEY,
                    bundleOf(
                        Constants.MEDIA_ID_KEY to it.id,
                        Constants.IS_IT_A_MOVIE_KEY to true
                    )
                )
                navController.navigate(R.id.action_navigation_coming_soon_to_playerFragment)
            },
            onFirstLoad = {
                if (!isFirstPrinted) {
                    isFirstPrinted = true
                    binding.apply {
                        descriptionBox.startAnimation(popingAnim)
                        _movieResult = it
                        title.text = it.title
                        overview.text = it.overview
                        releaseDate.text = "Coming on ${it.releaseDate}"
                        genreAdapter.submitList(Helpers.getMovieGenreListFromIds(it.genreIds))
                        setUpWatchListClick(it)
                    }
                }
            })
        binding.apply {

            comingSoonRv.setHasFixedSize(true)
            comingSoonRv.adapter = adapter
            comingSoonRv.set3DItem(true)
            comingSoonRv.setAlpha(true)

            // Setting Genre Recyclerview
            genreAdapter = GenreAdapter(resources.getColor(R.color.black, resources.newTheme()))
            rvGenres.setHasFixedSize(true)
            rvGenres.adapter = genreAdapter

            comingSoonRv.setItemSelectListener(object : CarouselLayoutManager.OnSelected {
                override fun onItemSelected(position: Int) {
                    val movieResult = adapter.getSelectedItem(position)
                    descriptionBox.startAnimation(popingAnim)
                    _movieResult = movieResult
                    title.text = movieResult.title
                    // Setting the Genre List
                    genreAdapter.submitList(Helpers.getMovieGenreListFromIds(movieResult.genreIds))
                    overview.text = movieResult.overview
                    releaseDate.text = "Coming on ${movieResult.releaseDate}"
                    setUpWatchListClick(movieResult)
                }
            })
        }
    }

    private fun setUpWatchListClick(movieResult: MovieResult) = binding.apply {

        addToWatchlist.setOnClickListener {
            addToWatchlist.startAnimation(popInAnim)
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {

                viewModel.addToWatchList(
                    accountId = viewModel.getAccountId().first()!!,
                    sessionId = viewModel.getSessionId().first()!!,
                    addToWatchListRequest = AddToWatchListRequest(
                        mediaId = movieResult.id,
                        mediaType = "movie",
                        watchlist = true
                    )
                ).let {
                    when (it) {
                        is Resource.Error -> withContext(Dispatchers.Main) {
                            showSnackBar(it.message ?: "Something went wrong")
                        }
//                        is Resource.Loading -> { }
                        is Resource.Success -> withContext(Dispatchers.Main) {
                            showSnackBar("${movieResult.title} added to watchlist")
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


//    private lateinit var binding: FragmentComingSoonBinding
//    private lateinit var viewModel: MediaViewModel
//    private lateinit var upcomingMoviesAdapter: UpcomingMoviesAdapter
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        setupUI()
//        setupViewModel()
//    }
//
//    override fun onFirstDisplay() {
//        fetchData()
//    }
//
//    private fun setupUI() {
//        upcomingMoviesAdapter = UpcomingMoviesAdapter()
//        binding.upcomingMoviesList.adapter = upcomingMoviesAdapter
//
//
//        val snapHelper = GravitySnapHelper(Gravity.CENTER)
//        snapHelper.scrollMsPerInch = 40.0f
//        snapHelper.maxFlingSizeFraction = 0.2f
//        snapHelper.attachToRecyclerView(binding.upcomingMoviesList)
//
//        addListScrollListener()
//    }
//
//    private fun addListScrollListener() {
//        binding.upcomingMoviesList.addOnScrollListener(object : OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                synchronized(this) {
//                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                        val newPosition =
//                            (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
//                        if (newPosition == -1) {
//                            return
//                        }
//                        val oldPosition = upcomingMoviesAdapter.firstVisibleItemPosition
//                        Log.v(
//                            "__SCROLL_STATE_IDLE",
//                            "oldPosition: $oldPosition, newPosition: $newPosition"
//                        )
//
//                        if (newPosition != oldPosition) {
//                            val oldPositionItem =
//                                (recyclerView.findViewHolderForAdapterPosition(oldPosition) as UpcomingMovieViewHolder?)
//                            val newPositionItem =
//                                (recyclerView.findViewHolderForAdapterPosition(newPosition) as UpcomingMovieViewHolder?)
//
//                            oldPositionItem?.binding?.overlay?.show()
//                            newPositionItem?.binding?.overlay?.hide()
//                            upcomingMoviesAdapter.firstVisibleItemPosition = newPosition
//                        }
//                    }
//                }
//            }
//        })
//
//    }
//
//    private fun fetchData() {
//        lifecycleScope.launchWhenCreated {
//            try {
//                viewModel.getUpcomingMovies().collectLatest {
//                    upcomingMoviesAdapter.submitData(it)
//                }
//            } catch (e: Exception) {
//            }
//        }
//    }
//}