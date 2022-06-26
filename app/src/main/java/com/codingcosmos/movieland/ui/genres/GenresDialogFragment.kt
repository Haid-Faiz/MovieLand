package com.codingcosmos.movieland.ui.genres

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.codingcosmos.datasource.remote.models.responses.Genre
import com.codingcosmos.movieland.R
import com.codingcosmos.movieland.data.paging.PagingStateAdapter
import com.codingcosmos.movieland.databinding.FragmentGenresBsdBinding
import com.codingcosmos.movieland.ui.home.HorizontalPagerAdapter
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
import com.codingcosmos.movieland.utils.Helpers
import com.codingcosmos.movieland.utils.doVibrate
import com.codingcosmos.movieland.utils.handleExceptions
import com.codingcosmos.movieland.utils.showSnackBar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GenresDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentGenresBsdBinding? = null
    private val binding get() = _binding!!
    private lateinit var genresAdapter: GenresOptionAdapter
    private lateinit var horizontalAdapter: HorizontalPagerAdapter
    private val viewModel: GenresViewModel by viewModels()
    private var selectedGenresList: ArrayList<Genre> = ArrayList()
    private var onTabSelectedListener: TabLayout.OnTabSelectedListener? = null
    private val allMovieGeneresList get() = Helpers.getAllMovieGenreList()
    private val allTvGeneresList get() = Helpers.getAllTvGenreList()
    private lateinit var shakeAnim: Animation
    private var _isMovie: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGenresBsdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shakeAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.shake_anim)
        setUpRecyclerView()
        setUpTabLayout()

        binding.closeDetailBtn.setOnClickListener { dismiss() }

        binding.getResultsButton.setOnClickListener {
            if (selectedGenresList.isNotEmpty()) {
                val stringBuilder = StringBuilder()
                selectedGenresList.forEach { stringBuilder.append("${it.id},") }
                stringBuilder.removeSuffix(",")
                viewModel.getMediaByGenres(
                    genresIds = stringBuilder.toString(),
                    isMovie = _isMovie
                ).observe(viewLifecycleOwner) {
                    horizontalAdapter.submitData(lifecycle, it)
                }
            } else {
                binding.rvGenres.startAnimation(shakeAnim)
                doVibrate(duration = 300)
            }
        }
    }

    private fun setUpTabLayout() {
        onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectedGenresList.clear()
                if (tab?.position == 0) {
                    _isMovie = true
                    binding.rvGenres.adapter = genresAdapter
                    genresAdapter.submitList(allMovieGeneresList)
                } else {
                    _isMovie = false
                    binding.rvGenres.adapter = genresAdapter
                    genresAdapter.submitList(allTvGeneresList)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        }
        binding.tabLayout.addOnTabSelectedListener(onTabSelectedListener!!)
    }

    private fun setUpRecyclerView() = binding.apply {
        // Setting up genres option list Recyclerview
        genresAdapter = GenresOptionAdapter(
            selectedStrokeColor = ContextCompat.getColor(requireContext(), R.color.red),
            unSelectedStrokeColor = ContextCompat.getColor(requireContext(), R.color.divider),
            cardBgColor = ContextCompat.getColor(requireContext(), R.color.black),
            selectGenreItemClick = { genre: Genre ->
                if (!selectedGenresList.contains(genre))
                    selectedGenresList.add(genre)
            },
            removeGenreItemClick = { genre: Genre -> selectedGenresList.remove(genre) }
        )
        rvGenres.setHasFixedSize(true)
        rvGenres.adapter = genresAdapter
        genresAdapter.submitList(allMovieGeneresList)

        // Setting up genres option list Recyclerview
        horizontalAdapter = HorizontalPagerAdapter {
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
            findNavController().navigate(R.id.action_selectGenresDialogFragment_to_detailFragment)
        }

        rvMedia.adapter = horizontalAdapter.withLoadStateHeaderAndFooter(
            footer = PagingStateAdapter { horizontalAdapter.retry() },
            header = PagingStateAdapter { horizontalAdapter.retry() }
        )

        horizontalAdapter.addLoadStateListener {
            when (it.refresh) {
                is LoadState.NotLoading -> {
                    binding.progressBar.isGone = true
                    binding.rvMedia.isGone = false
                }
                LoadState.Loading -> {
                    binding.progressBar.isGone = false
                    binding.rvMedia.isGone = true
                }
                is LoadState.Error -> {
                    binding.progressBar.isGone = true
                    binding.rvMedia.isGone = false
                    val errorType = handleExceptions((it.refresh as LoadState.Error).error)
                    requireView().showSnackBar(
                        message = if (errorType == ErrorType.NETWORK)
                            "Please check your internet connection"
                        else "Oops.. Something went wrong",
                        actionMsg = "Retry"
                    ) { horizontalAdapter.retry() }
                }
            }
        }
        rvMedia.setHasFixedSize(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        onTabSelectedListener = null
    }
}
