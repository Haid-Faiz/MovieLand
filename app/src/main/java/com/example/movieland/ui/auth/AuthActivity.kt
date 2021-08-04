package com.example.movieland.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.example.movieland.MainActivity
import com.example.movieland.R
import com.example.movieland.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            viewpagerAuth.adapter = IntroPagerAdapter()
            dotsIndicator.setupWithViewPager(binding.viewpagerAuth)
            buttonAuth.setOnClickListener {
                val customTab: CustomTabsIntent = CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .setColorScheme(ContextCompat.getColor(this@AuthActivity, R.color.black))
                    .build()

                customTab.launchUrl(this@AuthActivity, Uri.parse("url"))
            }
//            buttonSkip.setOnClickListener {
//                startActivity(Intent(this@AuthActivity, MainActivity::class.java))
//                finish()
//            }
        }
    }

}