package com.example.movieland.ui.coming_soon

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
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
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.datasource.remote.models.requests.AddToWatchListRequest
import com.example.datasource.remote.models.responses.MovieResult
import com.example.movieland.MainActivity
import com.example.movieland.R
import com.example.movieland.databinding.FragmentComingSoonBinding
import com.example.movieland.ui.genres.GenreAdapter
import com.example.movieland.utils.Constants
import com.example.movieland.utils.Constants.MOVIE
import com.example.movieland.utils.Constants.TMDB_CAST_IMAGE_BASE_URL_W185
import com.example.movieland.utils.Helpers
import com.example.movieland.utils.Resource
import com.example.movieland.utils.formatUpcomingDate
import com.example.movieland.utils.showSnackBar
import com.jackandphantom.carouselrecyclerview.CarouselLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    private lateinit var fadeInAnim: Animation
    private lateinit var imageLoader: ImageLoader
    private lateinit var imageRequestBuilder: ImageRequest.Builder

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
        imageLoader = ImageLoader(requireContext())
        imageRequestBuilder = ImageRequest.Builder(requireContext())
        setUpRecyclerview()

        fadeInAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)

        viewModel.comingSoon.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> binding.apply {
                    errorLayout.root.isGone = false
                    progressBar.isGone = true
                    mainLayout.isGone = true
                }
                is Resource.Loading -> binding.apply {
                    errorLayout.root.isGone = true
                    progressBar.isGone = false
                    mainLayout.isGone = true
                }
                is Resource.Success -> binding.apply {
                    errorLayout.root.isGone = true
                    progressBar.isGone = true
                    mainLayout.isGone = false
                    adapter.submitList(it.data?.movieResults)
                }
            }
        }

        binding.errorLayout.retryButton.setOnClickListener {
            viewModel.retry()
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
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            val imgBitmap = getBitmapFromUrl(it.posterPath!!)
                            val dominantColor = calculateDominantColor(imgBitmap)

                            withContext(Dispatchers.Main) {
                                rootLayout.setBackgroundColor(dominantColor)
                                rootLayout.startAnimation(fadeInAnim)
                                (requireActivity() as MainActivity).binding.bottomNavView.setBackgroundColor(
                                    dominantColor
                                )
                            }
                        }

                        descriptionBox.startAnimation(popingAnim)
                        _movieResult = it
                        title.text = it.title
                        overview.text = it.overview
                        ratingText.text = String.format("%.1f", it.voteAverage)
                        releaseDate.formatUpcomingDate(it.releaseDate)
                        genreAdapter.submitList(Helpers.getMovieGenreListFromIds(it.genreIds))
                        setUpWatchListClick(it)
                    }
                }
            }
        )
        binding.apply {

            comingSoonRv.setHasFixedSize(true)
            comingSoonRv.adapter = adapter
            comingSoonRv.set3DItem(true)
            comingSoonRv.setAlpha(true)

            // Setting Genre Recyclerview
            genreAdapter =
                GenreAdapter(resources.getColor(R.color.transparent, resources.newTheme()))
            rvGenres.setHasFixedSize(true)
            rvGenres.adapter = genreAdapter

            comingSoonRv.setItemSelectListener(object : CarouselLayoutManager.OnSelected {
                override fun onItemSelected(position: Int) {
                    val movieResult = adapter.getSelectedItem(position)

                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        val imgBitmap = getBitmapFromUrl(movieResult.posterPath!!)
                        val dominantColor = calculateDominantColor(imgBitmap)

                        withContext(Dispatchers.Main) {
                            rootLayout.setBackgroundColor(dominantColor)
                            rootLayout.startAnimation(fadeInAnim)
                            (requireActivity() as MainActivity).binding.bottomNavView.setBackgroundColor(
                                dominantColor
                            )
                        }
                    }

                    descriptionBox.startAnimation(popingAnim)
                    _movieResult = movieResult
                    title.text = movieResult.title
                    // Setting the Genre List
                    genreAdapter.submitList(Helpers.getMovieGenreListFromIds(movieResult.genreIds))
                    overview.text = movieResult.overview
                    ratingText.text = String.format("%.1f", movieResult.voteAverage)
                    releaseDate.formatUpcomingDate(movieResult.releaseDate)
                    rvGenres.scrollToPosition(0)
                    setUpWatchListClick(movieResult)
                }
            })
        }
    }

    private fun calculateDominantColor(bitmap: Bitmap): Int = Palette
        .from(bitmap).generate().getDarkMutedColor(R.color.black)

    // good: darkVibrantSwatch,
    // fine: darkMutedSwatch

    private suspend fun getBitmapFromUrl(imgUrl: String): Bitmap {
        val requestBuilder = imageRequestBuilder
        val drawable = (imageLoader.execute(
            requestBuilder.data(TMDB_CAST_IMAGE_BASE_URL_W185.plus(imgUrl)).build()
        ) as SuccessResult).drawable
        return (drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)
    }

    private fun setUpWatchListClick(movieResult: MovieResult) = binding.apply {
        addToWatchlist.setOnClickListener {
            addToWatchlist.startAnimation(popInAnim)
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                val sessionId = viewModel.getSessionId().first()
                val accountId = viewModel.getAccountId().first()

                if (sessionId != null && accountId != null) {
                    viewModel.addToWatchList(
                        accountId = accountId,
                        sessionId = sessionId,
                        addToWatchListRequest = AddToWatchListRequest(
                            mediaId = movieResult.id,
                            mediaType = MOVIE,
                            watchlist = true
                        )
                    ).let {
                        when (it) {
                            is Resource.Error -> withContext(Dispatchers.Main) {
                                showSnackBar(it.message ?: "Something went wrong")
                            }
                            // is Resource.Loading -> { }
                            is Resource.Success -> withContext(Dispatchers.Main) {
                                showSnackBar("${movieResult.title} added to watchlist")
                            }
                        }
                    }
                } else showSnackBar("Please login to avail this feature")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as MainActivity).binding.bottomNavView.setBackgroundColor(R.color.black)
        _binding = null
    }
}
