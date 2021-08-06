package com.example.movieland.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.movieland.MainActivity
import com.example.movieland.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val authViewModel: AuthViewModel by viewModels()
    private var _requestToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            viewpagerAuth.adapter = IntroPagerAdapter()
            dotsIndicator.setupWithViewPager(binding.viewpagerAuth)
            buttonSignIn.setOnClickListener {

                binding.apply {
                    progressSignIn.isVisible = true
                    buttonSignIn.text = ""
                    buttonSignIn.isClickable = false
                }

                lifecycleScope.launchWhenCreated {

                    authViewModel.requestToken().let { requestToken ->
                        _requestToken = requestToken
                        val customTab: CustomTabsIntent = CustomTabsIntent.Builder()
                            .setShowTitle(true)
                            .build()
                        customTab.launchUrl(
                            this@AuthActivity,
                            Uri.parse("https://www.themoviedb.org/auth/access?request_token=$requestToken")
                        )
                    }
                }
                buttonSkip.setOnClickListener {
                    startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }

    // iff instance of this activity already exists
    // Any re-entry to this activity will be routed through onNewIntent()
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        binding.apply {
            progressSignIn.isVisible = true
            buttonSignIn.text = ""
            buttonSignIn.isClickable = false
        }

        intent?.data?.let {
            lifecycleScope.launchWhenCreated {
                Log.d("_request_token_h", "onNewIntent: $_requestToken")
                _requestToken?.let { token ->
                    authViewModel.requestUserAccessToken(token).let {
                        Log.d(
                            "UserAccessToken",
                            "onNewIntent: accessToken: ${it?.accessToken}  |  accountID: ${it?.accountId}"
                        )
                        startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }
}