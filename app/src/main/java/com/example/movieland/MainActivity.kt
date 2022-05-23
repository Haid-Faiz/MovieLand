package com.example.movieland

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.movieland.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navController = findNavController(R.id.nav_host_fragment_activity_main)
        binding.bottomNavView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { navContrl, destination, _ ->

            if (navContrl.previousBackStackEntry?.destination?.id == R.id.navigation_home ||
                navContrl.previousBackStackEntry?.destination?.id == R.id.navigation_search ||
                navContrl.previousBackStackEntry?.destination?.id == R.id.navigation_account
            ) {
                binding.bottomNavView.isVisible = destination.id != R.id.playerFragment &&
                        destination.id != R.id.movieListFragment &&
                        destination.id != R.id.castDetailsFragment
            } else {
                binding.bottomNavView.isVisible = destination.id != R.id.playerFragment &&
                        destination.id != R.id.movieListFragment &&
                        destination.id != R.id.detailFragment &&
                        destination.id != R.id.castDetailsFragment
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
