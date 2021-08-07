package com.example.movieland.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.lifecycleScope
import com.example.datasource.remote.models.responses.RequestTokenResponse
import com.example.movieland.MainActivity
import com.example.movieland.databinding.ActivityAuthBinding
import com.example.movieland.utils.Resource
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val authViewModel: AuthViewModel by viewModels()
    private var _requestToken: String? = null
    private var isCustomTabOpened = false
    private var isOnNewInentCalled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            viewpagerAuth.adapter = IntroPagerAdapter()
            dotsIndicator.setupWithViewPager(binding.viewpagerAuth)

            buttonSignIn.setOnClickListener {
                // Start progress & disable button
                buttonSignIn.startLoading()
                buttonSignIn.isEnabled = false
                buttonSkip.isEnabled = false
                isCustomTabOpened = true

                val customTab: CustomTabsIntent = CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .build()

                lifecycleScope.launch(Dispatchers.IO) {
                    authViewModel.requestToken().let { result: Resource<RequestTokenResponse> ->
                        when (result) {
                            is Resource.Error -> {
                                buttonSignIn.loadingFailed()
                                buttonSignIn.isEnabled = true
                                buttonSkip.isEnabled = true
                                isCustomTabOpened = false
                            }
//                            is Resource.Loading -> TODO()
                            is Resource.Success -> {
                                _requestToken = result.data?.requestToken
                                customTab.launchUrl(
                                    this@AuthActivity,
                                    Uri.parse("https://www.themoviedb.org/auth/access?request_token=$_requestToken")
                                )
                            }
                        }
                    }
                }
            }

            buttonSkip.setOnClickListener {
                startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    // iff instance of this activity already exists
    // Any re-entry to this activity will be routed through onNewIntent()
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        isOnNewInentCalled = true
        Log.d("WhoCalled", "onNewIntent: called $_requestToken")

        intent?.data?.let {
            lifecycleScope.launch(Dispatchers.IO) {
                _requestToken?.let { token ->

                    authViewModel.requestUserAccessToken(token).let { result ->

                        when (result) {
                            is Resource.Error -> {
                                binding.buttonSignIn.loadingFailed()
                                binding.buttonSignIn.isEnabled = true
                                binding.buttonSkip.isEnabled = true
                                isCustomTabOpened = false
                            }
//                            is Resource.Loading -> TODO()
                            is Resource.Success -> {
                                Log.d(
                                    "UserAccessToken",
                                    "accessToken: ${result.data?.accessToken} | accountID: ${result.data?.accountId}"
                                )
                                withContext(Dispatchers.Main) {
                                    binding.buttonSignIn.loadingSuccessful()
                                    startActivity(
                                        Intent(
                                            this@AuthActivity, MainActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // if user has closed the custom tab
        Log.d("WhoCalled", "onResume: called $isCustomTabOpened")
        if (isCustomTabOpened && !isOnNewInentCalled) {
            // User has cancelled the approval. hence, onNewIntent hasn't called
            // Dismiss loading & show approval cancelled
            Toast.makeText(this, "Authentication approval denied", Toast.LENGTH_SHORT).show()
            binding.buttonSignIn.loadingFailed()
            binding.buttonSignIn.isEnabled = true
            binding.buttonSkip.isEnabled = true
            isCustomTabOpened = false
        }
    }
}