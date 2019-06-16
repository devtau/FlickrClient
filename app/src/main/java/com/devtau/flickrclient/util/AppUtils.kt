package com.devtau.flickrclient.util

import android.content.Context
import android.net.ConnectivityManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import android.view.WindowManager
import android.widget.Toast
import com.devtau.flickrclient.R

object AppUtils {

    private const val LOG_TAG = "AppUtils"


    fun resourceIdWith(context: Context, imageName: String): Int =
        context.resources.getIdentifier(imageName, "drawable", context.packageName)


    fun showToast(context: Context?, @StringRes msgId: Int) = showToast(context, context?.getString(msgId))
    fun showToast(context: Context?, msg: String?) {
        if (context == null || msg == null) return
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }


    fun showDialog(context: Context?, @StringRes msgId: Int) = showDialog(context, context?.getString(msgId))
    fun showDialog(context: Context?, msg: String?) {
        if (context == null || msg == null) return
        try {
            AlertDialog.Builder(context)
                .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                .setMessage(msg).show()
        } catch (e: WindowManager.BadTokenException) {
            Logger.e(LOG_TAG, "in showDialog. cannot show dialog")
            showToast(context, msg)
        }
    }

    fun checkConnection(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo ?: return false
        if (!networkInfo.isConnectedOrConnecting) AppUtils.showDialog(context, R.string.you_seem_to_be_offline)
        return networkInfo.isConnectedOrConnecting
    }
}