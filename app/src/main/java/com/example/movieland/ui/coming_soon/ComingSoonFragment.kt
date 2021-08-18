package com.example.movieland.ui.coming_soon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.movieland.R
import com.example.movieland.databinding.FragmentComingSoonBinding
import com.example.movieland.utils.Resource
import com.jackandphantom.carouselrecyclerview.CarouselLayoutManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ComingSoonFragment : Fragment() {

    private var _binding: FragmentComingSoonBinding? = null
    private lateinit var adapter: ComingSoonAdapter
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private val viewModel: ComingSoonViewModel by viewModels()
    private var isFirstPrinted: Boolean = false

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
        setUpRecyclerview()
        viewModel.getComingSoonMovies()
        viewModel.comingSoon.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> TODO()
                is Resource.Loading -> TODO()
                is Resource.Success -> {
                    adapter.submitList(it.data?.movieResults)
                }
            }
        }
    }

    private fun setUpRecyclerview() {
        val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_right)
        navController = findNavController()
        adapter = ComingSoonAdapter({
            // TODO
        }, onFirstLoad = {
            if (!isFirstPrinted) {
                isFirstPrinted = true
                binding.apply {
                    descriptionBox.startAnimation(anim)
                    title.text = it.title
                    overview.text = it.overview
                    releaseDate.text = "Coming on ${it.releaseDate}"
                }
            }
        })
        binding.apply {

            comingSoonRv.setHasFixedSize(true)
            comingSoonRv.adapter = adapter
            comingSoonRv.set3DItem(true)
            comingSoonRv.setAlpha(true)

            comingSoonRv.setItemSelectListener(object : CarouselLayoutManager.OnSelected {
                override fun onItemSelected(position: Int) {
                    val movieResult = adapter.getSelectedItem(position)
                    descriptionBox.startAnimation(anim)
                    title.text = movieResult.title
                    overview.text = movieResult.overview
                    releaseDate.text = "Coming on ${movieResult.releaseDate}"
                    likeBtn.setOnClickListener {

                    }
                }
            })

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