package com.example.movieland.utils

import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

fun View.showSnackBar(message: String, action: (() -> Unit)? = null, actionMsg: String? = null) {
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).apply {
        setAction(actionMsg) {
            action?.invoke()
        }
        show()
    }
}

fun Fragment.showSnackBar(
    message: String,
    action: (() -> Unit)? = null,
    actionMsg: String? = null
) {
    requireView().showSnackBar(message, action, actionMsg)
}

fun TextView.formatMediaDate(inputTime: String?) {
    // Making SDF object by giving input time patter
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())  // inputPatter: yyyy-MM-dd
    // Parsing inputTime
    val parsedDate: Date? = sdf.parse(inputTime)
    // Formatting parsed input time/date
    val formattedTime = SimpleDateFormat("yyyy", Locale.getDefault()).format(parsedDate)
    // Setting time to this textview
    this.text = formattedTime
}

fun TextView.formatUpcomingDate(inputTime: String?) {
    // Making SDF object by giving input time patter
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    // Parsing inputTime
    val parsedDate: Date? = sdf.parse(inputTime)
    // Formatting parsed input time/date
    val formattedTime = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(parsedDate)
    // Setting time to this textview
    this.text = "Coming on $formattedTime"
}

fun Fragment.safeFragmentNavigation(navController: NavController, currentFragmentId: Int, actionId: Int) {
    if (navController.currentDestination?.id == currentFragmentId) {
        navController.navigate(actionId)
    }
}