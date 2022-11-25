package com.codingcosmos.movieland.ui.auth

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.codingcosmos.datasource.remote.models.responses.RequestTokenResponse
import com.codingcosmos.datasource.remote.models.responses.SessionResponse
import com.codingcosmos.movieland.MainActivity
import com.codingcosmos.movieland.databinding.ActivityAuthBinding
import com.codingcosmos.movieland.utils.Constants.TMDB_AUTHENTICATION_WEB_PAGE_REDIRECT_URL
import com.codingcosmos.movieland.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/*** Steps for generating valid session id for TMDB
 * 1.) Generate request token
 * 2.) Send user to this url to verify generated request token -> https://www.themoviedb.org/auth/access?request_token=$_requestToken
 * 3.) On getting approval from user, onNewIntent() method will be called & we'll get our request token verified there
 * 4.) From verified request token, Generate new User Access Token
 * 5.) Finally, now we need to generate SESSION ID, use generated Access Token to generate Session ID. */

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val authViewModel: AuthViewModel by viewModels()
    private var _requestToken: String? = null
    private var isCustomTabOpened = false
    private var isOnNewIntentCalled = false
    private val customTab: CustomTabsIntent by lazy {
        CustomTabsIntent.Builder().setShowTitle(true).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        isUserLoggedIn()
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    private fun initUI() = binding.apply {
        viewpagerAuth.adapter = IntroPagerAdapter()
        dotsIndicator.setupWithViewPager(binding.viewpagerAuth)
        buttonSignIn.setOnClickListener { authViewModel.requestToken() }

        authViewModel.requestToken.observe(this@AuthActivity) { result: Resource<RequestTokenResponse> ->
            when (result) {
                is Resource.Error -> {
                    resetAfterError()
                }
                is Resource.Loading -> {
                    buttonSignIn.startLoading()
                    buttonSignIn.isEnabled = false
                    buttonSkip.isEnabled = false
                    isCustomTabOpened = true
                }
                is Resource.Success -> {
                    _requestToken = result.data?.requestToken
                    try {
                        customTab.launchUrl(
                            this@AuthActivity,
                            Uri.parse(TMDB_AUTHENTICATION_WEB_PAGE_REDIRECT_URL.plus(_requestToken))
                        )
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(
                            this@AuthActivity,
                            "Your phone doesn't have supported browser to open this",
                            Toast.LENGTH_SHORT
                        ).show()
                        resetAfterError()
                    }
                }
            }
        }

        buttonSkip.setOnClickListener {
            finish()
        }
    }

    private fun isUserLoggedIn() {
        // Check if user Session ID & Account ID is saved or not
        runBlocking {
            if (authViewModel.getSessionId().first() != null &&
                authViewModel.getAccountId().first() != null
            ) {
                // User is in logged state
                // Finish this activity & goto MainActivity
                startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun resetAfterError() = binding.apply {
        buttonSignIn.loadingFailed()
        buttonSignIn.isEnabled = true
        buttonSkip.isEnabled = true
        isCustomTabOpened = false
    }

    // iff instance of this activity already exists
    // Any re-entry to this activity will be routed through onNewIntent()
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        isOnNewIntentCalled = true

        intent?.data?.let {
            // Here we have got request token verified by user
            _requestToken?.let { token ->
                // Now we need to generate User Access Token
                authViewModel.requestUserAccessToken(token)
            }
        }

        authViewModel.accessToken.observe(this@AuthActivity) { result ->
            when (result) {
                is Resource.Error -> {
                    resetAfterError()
                }
                // is Resource.Loading -> TODO()
                is Resource.Success -> {
                    // Save AccessToken
                    authViewModel.saveAccessToken(result.data!!.accessToken)
                    /** Now we need to generate SESSION ID */
                    authViewModel.createSessionId(accessToken = result.data.accessToken)
                    authViewModel.sessionId.observe(this@AuthActivity) { sessionResponse: Resource<SessionResponse> ->
                        // Save the sessionToken/UserAccessToken and User Account ID
                        when (sessionResponse) {
                            is Resource.Error -> {
                                resetAfterError()
                            }
                            is Resource.Success -> {
                                authViewModel.saveSessionId(sessionId = sessionResponse.data!!.sessionId)
                                // Fetching user details
                                authViewModel.getUserDetail(sessionId = sessionResponse.data.sessionId)
                                authViewModel.userAccount.observe(this@AuthActivity) {
                                    when (it) {
                                        is Resource.Error -> Toast.makeText(
                                            this, "Something went wrong",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        is Resource.Success -> {
                                            authViewModel.saveAccountId(accountId = it.data!!.id)
                                            authViewModel.saveUserName(username = it.data.username)
                                            binding.buttonSignIn.loadingSuccessful()
                                            // Sign-in finish, now simply going back to MainActivity
                                            startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                                            finishAffinity()
                                        }
                                        is Resource.Loading -> {}
                                    }
                                }
                            }
                            is Resource.Loading -> {}
                        }
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // If user has closed the custom tab
        if (isCustomTabOpened && !isOnNewIntentCalled) {
            // User has cancelled the approval. hence, onNewIntent hasn't called
            // Dismiss loading & show approval cancelled
            Toast.makeText(this, "Authentication approval denied", Toast.LENGTH_SHORT).show()
            resetAfterError()
        }
    }
}
