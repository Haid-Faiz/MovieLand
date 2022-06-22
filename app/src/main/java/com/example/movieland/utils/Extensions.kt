package com.example.movieland.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.google.android.material.snackbar.Snackbar
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun View.showSnackBar(
    message: String,
    length: Int = Snackbar.LENGTH_LONG,
    actionMsg: String? = null,
    action: (() -> Unit)? = null
) {
    Snackbar.make(this, message, length).apply {
        actionMsg?.let {
            setAction(actionMsg) {
                action?.invoke()
            }
        }
        show()
    }
}

fun Fragment.showSnackBar(
    message: String,
    length: Int = Snackbar.LENGTH_LONG,
    actionMsg: String? = null,
    action: (() -> Unit)? = null
) = requireView().showSnackBar(
    message = message,
    action = action,
    actionMsg = actionMsg,
    length = length
)

fun TextView.formatMediaDate(inputTime: String?) = if (!inputTime.isNullOrEmpty()) {
    // Making SDF object by giving input time patter
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // inputPatter: yyyy-MM-dd
    // Parsing inputTime
    val parsedDate: Date? = sdf.parse(inputTime)
    // Formatting parsed input time/date
    val formattedTime = SimpleDateFormat("yyyy", Locale.getDefault()).format(parsedDate)
    // Setting time to this textview
    this.text = formattedTime
} else text = "Unknown"

fun TextView.formatUpcomingDate(inputTime: String?) = if (!inputTime.isNullOrEmpty()) {
    // Making SDF object by giving input time pattern
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    // Parsing inputTime
    val parsedDate: Date? = sdf.parse(inputTime)
    // Formatting parsed input time/date
    val formattedTime = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(parsedDate)
    // Setting time to this textview
    this.text = "Coming on $formattedTime"
} else
    this.text = "Unknown"

fun Fragment.safeFragmentNavigation(
    navController: NavController,
    currentFragmentId: Int,
    actionId: Int? = null,
    action: NavDirections? = null
) {
    if (navController.currentDestination?.id == currentFragmentId) {
        if (actionId != null)
            navController.navigate(actionId)
        else
            navController.navigate(action!!)
    }
}

@Suppress("DEPRECATION")
fun Fragment.doVibrate(duration: Long) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android S = API 31
        val vibratorManager =
            requireActivity().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrator = vibratorManager.defaultVibrator
        vibrator.vibrate(
            VibrationEffect.createOneShot(
                duration,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    } else {
        val vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            vibrator.vibrate(duration)
        else
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    duration,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
    }

fun Fragment.handleExceptions(throwable: Throwable): ErrorType = when (throwable) {
    is HttpException -> {
        ErrorType.HTTP
    }
    is IOException -> {
        ErrorType.NETWORK
    }
    else -> {
        ErrorType.UNKNOWN
    }
}
