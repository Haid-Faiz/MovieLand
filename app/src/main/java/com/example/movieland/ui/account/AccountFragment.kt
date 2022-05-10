package com.example.movieland.ui.account

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.navigation.fragment.findNavController
import com.example.datasource.remote.models.requests.AddToFavouriteRequest
import com.example.datasource.remote.models.requests.AddToWatchListRequest
import com.example.datasource.remote.models.responses.MovieResult
import com.example.movieland.R
import com.example.movieland.databinding.FragmentAccountBinding
import com.example.movieland.ui.auth.AuthActivity
import com.example.movieland.ui.auth.AuthViewModel
import com.example.movieland.ui.home.HorizontalAdapter
import com.example.movieland.ui.home.AccountMediaAdapter
import com.example.movieland.ui.home.DeletedState
import com.example.movieland.utils.Constants.GENRES_ID_LIST_KEY
import com.example.movieland.utils.Constants.IS_IT_A_MOVIE_KEY
import com.example.movieland.utils.Constants.MEDIA_ID_KEY
import com.example.movieland.utils.Constants.MEDIA_IMAGE_KEY
import com.example.movieland.utils.Constants.MEDIA_OVERVIEW_KEY
import com.example.movieland.utils.Constants.MEDIA_RATING_KEY
import com.example.movieland.utils.Constants.MEDIA_SEND_REQUEST_KEY
import com.example.movieland.utils.Constants.MEDIA_TITLE_KEY
import com.example.movieland.utils.Constants.MEDIA_YEAR_KEY
import com.example.movieland.utils.Constants.MOVIE
import com.example.movieland.utils.Constants.TV
import com.example.movieland.utils.Resource
import com.example.movieland.utils.safeFragmentNavigation
import com.example.movieland.utils.showSnackBar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()
    private var _sessionId: String? = null
    private var _accountId: Int? = null
    private lateinit var watchListAdapter: AccountMediaAdapter
    private lateinit var ratingsAdapter: AccountMediaAdapter
    private lateinit var favouritesAdapter: AccountMediaAdapter

    // Lists
    private var _allWatchList: ArrayList<MovieResult>? = null
    private var _allRatingList: ArrayList<MovieResult>? = null
    private var _allFavouriteList: ArrayList<MovieResult>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        checkLoginStatus() // This method will also fetch the data
        setUpClickListeners()

        authViewModel.watchList.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> {
                    showSnackBar(
                        message = it.message!!,
                        length = Snackbar.LENGTH_INDEFINITE,
                        actionMsg = "Retry"
                    ) { fetchLists(accountId = _accountId!!, sessionId = _sessionId!!) }
                }
                is Resource.Loading -> binding.apply {
                    watchlistPlaceholder.isGone = false
                    rvWatchlist.isGone = true
                    emptyWatchlistMsg.isGone = true
                }
                is Resource.Success -> binding.apply {
                    binding.watchlistPlaceholder.isGone = true
                    _allWatchList = it.data!! as ArrayList
                    if (_allWatchList!!.isNotEmpty()) {
                        rvWatchlist.isGone = false
                        emptyWatchlistMsg.isGone = true
                        watchListAdapter.submitList(it.data)
                    } else {
                        rvWatchlist.isGone = true
                        emptyWatchlistMsg.isGone = false
                    }
                }
            }
        }

        authViewModel.ratingList.observe(viewLifecycleOwner) {
            when (it) {
                // is Resource.Error -> { showSnackBar(it.message!!) }
                is Resource.Loading -> binding.apply {
                    ratingsPlaceholder.isGone = false
                    rvRatings.isGone = true
                    emptyRatingsMsg.isGone = true
                }
                is Resource.Success -> binding.apply {
                    binding.ratingsPlaceholder.isGone = true
                    _allRatingList = it.data!! as ArrayList
                    if (_allRatingList!!.isNotEmpty()) {
                        rvRatings.isGone = false
                        emptyRatingsMsg.isGone = true
                        ratingsAdapter.submitList(it.data)
                    } else {
                        rvRatings.isGone = true
                        emptyRatingsMsg.isGone = false
                    }
                }
            }
        }

        authViewModel.favouriteList.observe(viewLifecycleOwner) {
            when (it) {
                // is Resource.Error -> { showSnackBar(it.message!!) }
                is Resource.Loading -> binding.apply {
                    favouritePlaceholder.isGone = false
                    rvFavourites.isGone = true
                    emptyFavouritesMsg.isGone = true
                }
                is Resource.Success -> binding.apply {
                    binding.favouritePlaceholder.isGone = true
                    _allFavouriteList = it.data!! as ArrayList

                    if (_allFavouriteList!!.isNotEmpty()) {
                        rvFavourites.isGone = false
                        emptyFavouritesMsg.isGone = true
                        favouritesAdapter.submitList(it.data)
                    } else {
                        rvFavourites.isGone = true
                        emptyFavouritesMsg.isGone = false
                    }
                }
            }
        }
    }

    private fun setUpClickListeners() = binding.apply {
        logOutBtn.setOnClickListener {
            // Clear Session Preferences
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                authViewModel.clearSessionPrefs()
                // Hide account main layout & show signIn layout
                binding.mainLoggedInLayout.isGone = true
                binding.notSignInLayout.notSignInRoot.isGone = false
                showSnackBar("Successfully logged out")
            }
        }

        notSignInLayout.accountSignInBtn.setOnClickListener {
            startActivity(Intent(requireActivity(), AuthActivity::class.java))
            requireActivity().finish()
        }

        watchlistMoviesCard.setOnClickListener {
            // First check if list is null or not
            _allWatchList?.let { allWatchList ->
                // Check if already shows card is checked or not
                if (watchlistShowsCard.isChecked) {
                    // Then uncheck shows card & check movie card
                    updateCardBackground(
                        watchlistShowsCard,
                        watchlistShowsText
                    ) // this will uncheck shows card

                    // Now check movie card
                    updateCardBackground(watchlistMoviesCard, watchlistMoviesText)
                    // Now submit the filtered list
                    val filteredList: List<MovieResult> = allWatchList.filter {
                        !it.title.isNullOrEmpty()
                    }
                    watchListAdapter.submitList(filteredList)
                } else {
                    // Now check movie card
                    updateCardBackground(watchlistMoviesCard, watchlistMoviesText)
                    // Now submit the filtered list
                    if (watchlistMoviesCard.isChecked) {
                        val filteredList: List<MovieResult> = allWatchList.filter {
                            !it.title.isNullOrEmpty()
                        }
                        watchListAdapter.submitList(filteredList)
                    } else watchListAdapter.submitList(_allWatchList)
                }
            }
        }

        watchlistShowsCard.setOnClickListener {

            _allWatchList?.let { allWatchList ->
                // Check if already movie card is checked or not
                if (watchlistMoviesCard.isChecked) {
                    // Then uncheck movie card & check shows card
                    updateCardBackground(
                        watchlistMoviesCard,
                        watchlistMoviesText
                    ) // this will uncheck movie card

                    // Now check show card
                    updateCardBackground(watchlistShowsCard, watchlistShowsText)
                    // Now submit the filtered list
                    val filteredList: List<MovieResult> = allWatchList.filter {
                        it.title.isNullOrEmpty()
                    }
                    watchListAdapter.submitList(filteredList)
                } else {
                    // check show card
                    updateCardBackground(watchlistShowsCard, watchlistShowsText)
                    // Now submit the filtered list
                    if (watchlistShowsCard.isChecked) {
                        val filteredList: List<MovieResult> = allWatchList.filter {
                            it.title.isNullOrEmpty()
                        }
                        watchListAdapter.submitList(filteredList)
                    } else watchListAdapter.submitList(_allWatchList)
                }
            }
        }

        ratingMoviesCard.setOnClickListener {

            _allRatingList?.let { allRatingList ->
                // Check if already shows card is checked or not
                if (ratingShowsCard.isChecked) {
                    // Then uncheck shows card & check movie card
                    updateCardBackground(
                        ratingShowsCard,
                        ratingShowsText
                    ) // this will uncheck shows card

                    // Now check movie card
                    updateCardBackground(ratingMoviesCard, ratingMoviesText)
                    // Now submit the filtered list
                    val filteredList: List<MovieResult> = allRatingList.filter {
                        !it.title.isNullOrEmpty()
                    }
                    ratingsAdapter.submitList(filteredList)
                } else {
                    // Now check movie card
                    updateCardBackground(ratingMoviesCard, ratingMoviesText)
                    // Now submit the filtered list
                    if (ratingMoviesCard.isChecked) {
                        val filteredList: List<MovieResult> = allRatingList.filter {
                            !it.title.isNullOrEmpty()
                        }
                        ratingsAdapter.submitList(filteredList)
                    } else ratingsAdapter.submitList(_allRatingList)
                }
            }
        }

        ratingShowsCard.setOnClickListener {
            _allRatingList?.let { allRatingList ->
                // Check if already movie card is checked or not
                if (ratingMoviesCard.isChecked) {
                    // Then uncheck movie card & check show card
                    updateCardBackground(
                        ratingMoviesCard,
                        ratingMoviesText
                    ) // this will uncheck movie card

                    // Now check shows card
                    updateCardBackground(ratingShowsCard, ratingShowsText)
                    // Now submit the filtered list
                    val filteredList: List<MovieResult> = allRatingList.filter {
                        it.title.isNullOrEmpty()
                    }
                    ratingsAdapter.submitList(filteredList)
                } else {
                    // Now check movie card
                    updateCardBackground(ratingShowsCard, ratingShowsText)
                    // Now submit the filtered list
                    if (ratingShowsCard.isChecked) {
                        val filteredList: List<MovieResult> = allRatingList.filter {
                            it.title.isNullOrEmpty()
                        }
                        ratingsAdapter.submitList(filteredList)
                    } else ratingsAdapter.submitList(_allRatingList)
                }
            }
        }

        favouriteMoviesCard.setOnClickListener {

            _allFavouriteList?.let { allFavouriteList ->
                // Check if already shows card is checked or not
                if (favouriteShowsCard.isChecked) {
                    // Then uncheck shows card & check movie card
                    updateCardBackground(
                        favouriteShowsCard,
                        favouriteShowsText
                    ) // this will uncheck shows card

                    // Now check movie card
                    updateCardBackground(favouriteMoviesCard, favouriteMoviesText)
                    // Now submit the filtered list
                    val filteredList: List<MovieResult> = allFavouriteList.filter {
                        !it.title.isNullOrEmpty()
                    }
                    favouritesAdapter.submitList(filteredList)
                } else {
                    // Now check movie card
                    updateCardBackground(favouriteMoviesCard, favouriteMoviesText)
                    // Now submit the filtered list
                    if (favouriteMoviesCard.isChecked) {
                        val filteredList: List<MovieResult> = allFavouriteList.filter {
                            !it.title.isNullOrEmpty()
                        }
                        favouritesAdapter.submitList(filteredList)
                    } else {
                        favouritesAdapter.submitList(_allFavouriteList)
                    }
                }
            }
        }

        favouriteShowsCard.setOnClickListener {

            _allFavouriteList?.let { allFavouriteList ->
                // Check if already movie card is checked or not
                if (favouriteMoviesCard.isChecked) {
                    // Then uncheck movie card & check shows card
                    updateCardBackground(
                        favouriteMoviesCard,
                        favouriteMoviesText
                    ) // this will uncheck movie card

                    // Now check show card
                    updateCardBackground(favouriteShowsCard, favouriteShowsText)
                    // Now submit the filtered list
                    val filteredList: List<MovieResult> = allFavouriteList.filter {
                        it.title.isNullOrEmpty()
                    }
                    favouritesAdapter.submitList(filteredList)
                } else {
                    // check show card
                    updateCardBackground(favouriteShowsCard, favouriteShowsText)
                    // Now submit the filtered list
                    if (favouriteShowsCard.isChecked) {
                        val filteredList: List<MovieResult> = allFavouriteList.filter {
                            it.title.isNullOrEmpty()
                        }
                        favouritesAdapter.submitList(filteredList)
                    } else
                        favouritesAdapter.submitList(_allFavouriteList)
                }
            }
        }
    }

    private fun updateCardBackground(
        materialCardView: MaterialCardView,
        textView: TextView
    ) {
        if (!materialCardView.isChecked) {
            // Card is not checked
            materialCardView.isChecked = true
            materialCardView.setCardBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.black)
            )
            materialCardView.strokeColor =
                ContextCompat.getColor(requireContext(), R.color.red)
            textView.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.red)
            )
        } else {
            materialCardView.isChecked = false
            materialCardView.setCardBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.dark_gray)
            )
            materialCardView.strokeColor =
                ContextCompat.getColor(requireContext(), R.color.text_secondary)
            textView.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.text_secondary)
            )
        }
    }

    private fun setUpRecyclerView() = binding.apply {

        // Setting WatchList RV
        watchListAdapter = AccountMediaAdapter(
            onPosterClick = {
                openDetailBottomSheet(it)
            },
            onDeleteClick = { item: MovieResult, deleteCallback ->
                // Delete a media from watchlist
                viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                    authViewModel.removeFromWatchList(
                        accountId = _accountId!!,
                        sessionId = _sessionId!!,
                        addToWatchListRequest = AddToWatchListRequest(
                            mediaId = item.id,
                            mediaType = if (item.tvShowName.isNullOrEmpty()) MOVIE else TV,
                            watchlist = false
                        )
                    ).collectLatest {
                        when (it) {
                            is Resource.Loading -> {
                                deleteCallback(DeletedState(isLoading = true))
                            }
                            is Resource.Success -> {
                                _allWatchList?.remove(item)
                                watchListAdapter.submitList(_allWatchList)
                                watchListAdapter.notifyDataSetChanged()
                                deleteCallback(DeletedState(isSuccess = true))
                                if (_allWatchList!!.isEmpty()) {
                                    rvWatchlist.isGone = true
                                    emptyWatchlistMsg.isGone = false
                                }
                            }
                            is Resource.Error -> {
                                deleteCallback(
                                    DeletedState(
                                        errorMessage = it.message ?: "Something went wrong"
                                    )
                                )
                            }
                        }
                    }
                }
            }
        )
        rvWatchlist.setHasFixedSize(true)
        rvWatchlist.adapter = watchListAdapter

        // Setting Rating RV
        ratingsAdapter = AccountMediaAdapter(
            onPosterClick = {
                openDetailBottomSheet(it)
            },
            onDeleteClick = { item: MovieResult, deleteCallback ->
                // Delete a media from ratings
                viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                    authViewModel.deleteRating(
                        mediaId = item.id,
                        isMovie = if (item.tvShowName.isNullOrEmpty()) true else false,
                        sessionId = _sessionId!!
                    ).collectLatest {
                        when (it) {
                            is Resource.Loading -> {
                                deleteCallback(DeletedState(isLoading = true))
                            }
                            is Resource.Success -> {
                                _allRatingList?.remove(item)
                                ratingsAdapter.submitList(_allRatingList)
                                ratingsAdapter.notifyDataSetChanged()
                                deleteCallback(DeletedState(isSuccess = true))
                                if (_allRatingList!!.isEmpty()) {
                                    rvRatings.isGone = true
                                    emptyRatingsMsg.isGone = false
                                }
                            }
                            is Resource.Error -> {
                                deleteCallback(
                                    DeletedState(
                                        errorMessage = it.message ?: "Something went wrong"
                                    )
                                )
                            }
                        }
                    }
                }
            },
            showRated = true
        )
        rvRatings.setHasFixedSize(true)
        rvRatings.adapter = ratingsAdapter

        // Setting Favourites RV
        favouritesAdapter = AccountMediaAdapter(
            onPosterClick = {
                openDetailBottomSheet(it)
            },
            onDeleteClick = { item: MovieResult, deleteCallback ->
                // Delete a media from favourite list
                viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                    authViewModel.removeFromFavourites(
                        accountId = _accountId!!,
                        sessionId = _sessionId!!,
                        addToFavouriteRequest = AddToFavouriteRequest(
                            mediaId = item.id,
                            mediaType = if (item.tvShowName.isNullOrEmpty()) MOVIE else TV,
                            favorite = false
                        )
                    ).collectLatest {
                        when (it) {
                            is Resource.Loading -> {
                                deleteCallback(DeletedState(isLoading = true))
                            }
                            is Resource.Success -> {
                                _allFavouriteList?.remove(item)
                                favouritesAdapter.submitList(_allFavouriteList)
                                favouritesAdapter.notifyDataSetChanged()
                                deleteCallback(DeletedState(isSuccess = true))
                                if (_allFavouriteList!!.isEmpty()) {
                                    rvFavourites.isGone = true
                                    emptyFavouritesMsg.isGone = false
                                }
                            }
                            is Resource.Error -> {
                                deleteCallback(
                                    DeletedState(
                                        errorMessage = it.message ?: "Something went wrong"
                                    )
                                )
                            }
                        }
                    }
                }
            }
        )
        rvFavourites.setHasFixedSize(true)
        rvFavourites.adapter = favouritesAdapter
    }

    private fun openDetailBottomSheet(movieResult: MovieResult) {
        parentFragmentManager.setFragmentResult(
            MEDIA_SEND_REQUEST_KEY,
            bundleOf(
                GENRES_ID_LIST_KEY to movieResult.genreIds,
                MEDIA_TITLE_KEY to (movieResult.title ?: movieResult.tvShowName),
                IS_IT_A_MOVIE_KEY to !movieResult.title.isNullOrEmpty(),
                MEDIA_OVERVIEW_KEY to movieResult.overview,
                MEDIA_IMAGE_KEY to movieResult.backdropPath,
                MEDIA_YEAR_KEY to (movieResult.releaseDate ?: movieResult.tvShowFirstAirDate),
                MEDIA_ID_KEY to movieResult.id,
                MEDIA_RATING_KEY to String.format("%.1f", movieResult.voteAverage)
            )
        )
        safeFragmentNavigation(
            navController = findNavController(),
            currentFragmentId = R.id.navigation_account,
            actionId = R.id.action_navigation_account_to_detailFragment
        )
    }

    private fun checkLoginStatus() = viewLifecycleOwner.lifecycleScope.launchWhenCreated {
        authViewModel.getSessionId().collect { sessionId: String? ->
            withContext(Dispatchers.Main) {
                updateUI(sessionId)
            }
        }
    }

    private fun updateUI(sessionId: String?) = sessionId?.let {
        // User is Logged in
        _sessionId = sessionId
        binding.apply {
            notSignInLayout.notSignInRoot.isGone = true
            mainLoggedInLayout.isGone = false
        }
        getUserDetailsFromPrefs()
    } ?: run {
        // User isn't logged in
        binding.apply {
            notSignInLayout.notSignInRoot.isGone = false
            mainLoggedInLayout.isGone = true
        }
    }

    private fun getUserDetailsFromPrefs() = viewLifecycleOwner.lifecycleScope.launchWhenCreated {
        _accountId = authViewModel.getAccountId().first()
        val userName = authViewModel.getUserName().first()

        if (_accountId != null && userName != null) {
            binding.userName.text = userName
            // Now fetch Watchlist, rated, favourite lis ts
            fetchLists(accountId = _accountId!!, sessionId = _sessionId!!)
        }
    }

    private fun fetchLists(accountId: Int, sessionId: String) {
        authViewModel.getWatchList(accountId, sessionId)
        authViewModel.getRatedMovies(accountId, sessionId)
        authViewModel.getFavouriteList(accountId, sessionId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
