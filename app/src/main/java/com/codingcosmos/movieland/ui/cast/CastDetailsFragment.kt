package com.codingcosmos.movieland.ui.cast

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.codingcosmos.datasource.remote.models.responses.MovieResult
import com.codingcosmos.movieland.R
import com.codingcosmos.movieland.databinding.CastProfileDialogBinding
import com.codingcosmos.movieland.ui.home.HorizontalAdapter
import com.codingcosmos.movieland.utils.Constants
import com.codingcosmos.movieland.utils.Constants.TMDB_CAST_IMAGE_BASE_URL_W342
import com.codingcosmos.movieland.utils.Resource
import com.codingcosmos.movieland.utils.safeFragmentNavigation
import com.codingcosmos.movieland.utils.showSnackBar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CastDetailsFragment : BottomSheetDialogFragment() {

    private var _binding: CastProfileDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HorizontalAdapter
    private val args: CastDetailsFragmentArgs by navArgs()

    @Inject
    lateinit var castDetailsVMFactory: CastDetailsViewModel.Factory

    private val viewModel: CastDetailsViewModel by viewModels {
        CastDetailsViewModel.providesFactory(
            assistedFactory = castDetailsVMFactory,
            personId = args.personId
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CastProfileDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        updateDetail()

        viewModel.actorMoviesShows.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> showSnackBar(it.message!!)
                is Resource.Success -> {
                    val filteredList = it.data!!.cast.filter { movieResult ->
                        !movieResult.posterPath.isNullOrEmpty() && !movieResult.backdropPath.isNullOrEmpty()
                    }
                    binding.rvActorFilmography.isGone = false
                    binding.progressBar.isGone = true
                    adapter.submitList(filteredList)
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun updateDetail() = binding.apply {
        profilePicture.load(TMDB_CAST_IMAGE_BASE_URL_W342.plus(args.profilePath))
        name.text = args.name
        knowForDepartment.text = "Known for:  ${args.knownForDepartment ?: "NA"}"
        closeBtn.setOnClickListener { dismiss() }
    }

    private fun setUpRecyclerView() = binding.apply {
        adapter = HorizontalAdapter {
            openMediaDetailsBSD(it)
        }
        binding.rvActorFilmography.setHasFixedSize(true)
        binding.rvActorFilmography.adapter = adapter
    }

    private fun openMediaDetailsBSD(media: MovieResult) {
        parentFragmentManager.setFragmentResult(
            Constants.MEDIA_SEND_REQUEST_KEY,
            bundleOf(
                Constants.MEDIA_ID_KEY to media.id,
                Constants.GENRES_ID_LIST_KEY to media.genreIds,
                Constants.MEDIA_TITLE_KEY to (media.title ?: media.tvShowName),
                Constants.MEDIA_OVERVIEW_KEY to media.overview,
                Constants.IS_IT_A_MOVIE_KEY to !media.title.isNullOrEmpty(),
                Constants.MEDIA_IMAGE_KEY to media.backdropPath,
                Constants.MEDIA_YEAR_KEY to (media.releaseDate ?: media.tvShowFirstAirDate),
                Constants.MEDIA_RATING_KEY to String.format("%.1f", media.voteAverage)
            )
        )

        safeFragmentNavigation(
            navController = findNavController(),
            currentFragmentId = R.id.castDetailsFragment,
            actionId = R.id.action_castKnownForFragment_to_detailFragment
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
