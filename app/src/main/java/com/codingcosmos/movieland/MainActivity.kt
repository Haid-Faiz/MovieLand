package com.codingcosmos.movieland

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import coil.ImageLoader
import coil.request.ImageRequest
import com.codingcosmos.movieland.databinding.ActivityMainBinding
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    lateinit var imageLoader: ImageLoader
    lateinit var imageRequestBuilder: ImageRequest.Builder

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
        imageLoader = ImageLoader(this)
        imageRequestBuilder = ImageRequest.Builder(this)

        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener {
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (it != null) {
                    deepLink = it.link
                }

                // Handle the deep link. For example, open the linked content,
                // or apply promotional credit to the user's account.
            }
            .addOnFailureListener {
                Toast.makeText(this@MainActivity, it.message ?: "failure", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
