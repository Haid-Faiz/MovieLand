package com.example.movieland.utils

import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun View.showSnackBar(message: String, action: (() -> Unit)? = null, actionMsg: String? = null) {
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).apply {
        setAction(actionMsg) {
            action?.invoke()
        }
        show()
    }
}

fun Fragment.showSnackBar(message: String, action: (() -> Unit)? = null, actionMsg: String? = null) {
    requireView().showSnackBar(message, action, actionMsg)
}