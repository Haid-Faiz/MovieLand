package com.example.movieland.ui.genres

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.datasource.remote.models.responses.Genre
import com.example.movieland.R
import com.example.movieland.databinding.FragmentGenresBsdBinding
import com.example.movieland.ui.home.HorizontalAdapter
import com.example.movieland.utils.Constants
import com.example.movieland.utils.Constants.GENRES_ID_LIST_KEY
import com.example.movieland.utils.Constants.IS_IT_A_MOVIE_KEY
import com.example.movieland.utils.Constants.MEDIA_ID_KEY
import com.example.movieland.utils.Constants.MEDIA_IMAGE_KEY
import com.example.movieland.utils.Constants.MEDIA_OVERVIEW_KEY
import com.example.movieland.utils.Constants.MEDIA_RATING_KEY
import com.example.movieland.utils.Constants.MEDIA_TITLE_KEY
import com.example.movieland.utils.Constants.MEDIA_YEAR_KEY
import com.example.movieland.utils.Helpers
import com.example.movieland.utils.Resource
import com.example.movieland.utils.showSnackBar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GenresDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentGenresBsdBinding? = null
    private var _isMovie: Boolean = true
    private val binding get() = _binding!!
    private lateinit var genresAdapter: GenresOptionAdapter
    private lateinit var horizontalAdapter: HorizontalAdapter
    private val viewModel: GenresViewModel by viewModels()
    private var selectedGenresList: ArrayList<Genre> = ArrayList()
    private var onTabSelectedListener: TabLayout.OnTabSelectedListener? = null
    private val allMovieGeneresList get() = Helpers.getAllMovieGenreList()
    private val allTvGeneresList get() = Helpers.getAllTvGenreList()
    private lateinit var shakeAnim: Animation

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
                viewModel.getMediaByGenres(genresIds = stringBuilder.toString(), isMovie = _isMovie)
            } else
                binding.rvGenres.startAnimation(shakeAnim)
        }

        viewModel.genresMedia.observe(viewLifecycleOwner) {
            (it is Resource.Loading).let {
                binding.progressBar.isGone = !it
                binding.rvMedia.isGone = it
            }
            when (it) {
                is Resource.Error -> TODO()
                is Resource.Success -> {
                    horizontalAdapter.submitList(it.data!!.movieResults)
                }
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


        horizontalAdapter = HorizontalAdapter {
            parentFragmentManager.setFragmentResult(
                Constants.MEDIA_SEND_REQUEST_KEY,
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
        rvMedia.setHasFixedSize(true)
        rvMedia.adapter = horizontalAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        onTabSelectedListener = null
    }
}