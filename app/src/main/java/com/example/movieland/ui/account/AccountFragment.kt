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
import androidx.navigation.fragment.findNavController
import com.example.datasource.remote.models.responses.MovieResult
import com.example.movieland.R
import com.example.movieland.databinding.FragmentAccountBinding
import com.example.movieland.ui.auth.AuthActivity
import com.example.movieland.ui.auth.AuthViewModel
import com.example.movieland.ui.home.HorizontalAdapter
import com.example.movieland.utils.Resource
import com.example.movieland.utils.showSnackBar
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()
    private var _sessionId: String? = null
    private var _accountId: Int? = null
    private lateinit var watchListAdapter: HorizontalAdapter
    private lateinit var ratingsAdapter: HorizontalAdapter
    private lateinit var favouritesAdapter: HorizontalAdapter

    // Lists
    private lateinit var allWatchList: ArrayList<MovieResult>
    private lateinit var allRatingList: ArrayList<MovieResult>
    private lateinit var allfavouriteList: ArrayList<MovieResult>


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
        checkLoginStatus()
        setUpClickListeners()
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

        Log.d("checkIsChecked", "onViewCreated: ${binding.watchlistMoviesCard.isChecked}")
        notSignInLayout.accountSignInBtn.setOnClickListener {
            startActivity(Intent(requireActivity(), AuthActivity::class.java))
            requireActivity().finish()
        }

        watchlistMoviesCard.setOnClickListener {
            updateCardBackground(watchlistMoviesCard, watchlistMoviesText)
        }

        watchlistShowsCard.setOnClickListener {
            updateCardBackground(watchlistShowsCard, watchlistShowsText)
        }

        ratingMoviesCard.setOnClickListener {
            updateCardBackground(ratingMoviesCard, ratingMoviesText)
        }

        ratingShowsCard.setOnClickListener {
            updateCardBackground(ratingShowsCard, ratingShowsText)
        }

        favouriteMoviesCard.setOnClickListener {
            updateCardBackground(favouriteMoviesCard, favouriteMoviesText)
        }

        favouriteShowsCard.setOnClickListener {
            updateCardBackground(favouriteShowsCard, favouriteShowsText)
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
        watchListAdapter = HorizontalAdapter {
            parentFragmentManager.setFragmentResult(
                "home_movie_key",
                bundleOf(
                    "genre_ids_list" to it.genreIds,
                    "movie_title" to (it.title ?: it.tvShowName),
                    "isMovie" to !it.title.isNullOrBlank(),
                    "movie_overview" to it.overview,
                    "movie_image_url" to it.backdropPath,
                    "movie_year" to (it.releaseDate ?: it.tvShowFirstAirDate),
                    "movie_id" to it.id,
                )
            )

            findNavController().navigate(R.id.action_navigation_account_to_detailFragment)
        }
        rvWatchlist.setHasFixedSize(true)
        rvWatchlist.adapter = watchListAdapter

        // Setting Rating RV
        ratingsAdapter = HorizontalAdapter {

        }
        rvRatings.setHasFixedSize(true)
        rvRatings.adapter = ratingsAdapter

        // Setting Favourites RV
        favouritesAdapter = HorizontalAdapter {

        }
        rvFavourites.setHasFixedSize(true)
        rvFavourites.adapter = favouritesAdapter
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

        Log.d(
            "accountId&userName",
            "getUserDetailsFromPrefs: accountId: $_accountId | userName: $userName"
        )

        if (_accountId != null && userName != null) {
            binding.userName.text = userName
            // Now fetch Watchlist, rated, favourite lis ts
            fetchLists(accountId = _accountId!!, sessionId = _sessionId!!)
        }
    }

    private fun fetchLists(accountId: Int, sessionId: String) {

        authViewModel.getWatchList(accountId, sessionId)
        authViewModel.watchList.observe(viewLifecycleOwner) {
            (it is Resource.Loading).let { isLoading ->
                binding.watchlistPlaceholder.isGone = !isLoading
                binding.rvWatchlist.isGone = isLoading
            }
            when (it) {
                is Resource.Error -> TODO()
                is Resource.Loading -> binding.apply {}
                is Resource.Success -> {
                    allWatchList = it.data!! as ArrayList
                    watchListAdapter.submitList(it.data!!)
                }
            }
        }

        authViewModel.getRatedMovies(accountId, sessionId)
        authViewModel.ratingList.observe(viewLifecycleOwner) {
            (it is Resource.Loading).let { isLoading ->
                binding.ratingsPlaceholder.isGone = !isLoading
                binding.rvRatings.isGone = isLoading
            }
            when (it) {
                is Resource.Error -> TODO()
                is Resource.Loading -> {
                }
                is Resource.Success -> {
                    allRatingList = it.data!! as ArrayList
                    ratingsAdapter.submitList(it.data!!)
                }
            }
        }

        authViewModel.getFavouriteList(accountId, sessionId)
        authViewModel.favouriteList.observe(viewLifecycleOwner) {
            (it is Resource.Loading).let { isLoading ->
                binding.favouritePlaceholder.isGone = !isLoading
                binding.rvFavourites.isGone = isLoading
            }
            when (it) {
                is Resource.Error -> TODO()
                is Resource.Loading -> {
                }
                is Resource.Success -> {
                    allfavouriteList = it.data!! as ArrayList
                    favouritesAdapter.submitList(it.data!!)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}