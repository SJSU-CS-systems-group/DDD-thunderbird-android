package net.discdd.k9.onboarding.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

fun showToast(context: Context, message: String?) {
    Handler(Looper.getMainLooper()).post(
        {
            Toast.makeText(
                context,
                message ?: "Unknown error",
                Toast.LENGTH_LONG
            ).show()
        })
}
