package com.osman.eczanemnerede.core

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.osman.eczanemnerede.R
import org.jsoup.Jsoup
import java.util.concurrent.Executors

object VersionController {

    private fun openPlayStore(context: Context, packageName: String) {
        val intent = try {
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        } catch (e: Exception) {
            Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
        }
        context.startActivity(intent) // <-- startActivity EKLENDİ!
    }

     fun getAppVersionCode(context: Context): Int {
        return try {
            val pInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            -1
        }
    }

    fun showUpdateNotification(context: Context,packageName:String) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.update_required))
            .setMessage(context.getString(R.string.update_message))
            .setPositiveButton(context.getString(R.string.update_now)) { _, _ ->
                openPlayStore(context,packageName)
            }
            .setNegativeButton(context.getString(R.string.update_later), null)
            .setCancelable(false)
            .show()
    }

    fun checkLatestVersion(context: Context, callback: (Int) -> Unit) {
        val appPackageName = context.packageName
        val url = "https://play.google.com/store/apps/details?id=$appPackageName&hl=en"

        Executors.newSingleThreadExecutor().execute {
            try {
                val document = Jsoup.connect(url).get()
                val versionElement = document.select("div[itemprop=softwareVersion]").first()
                val latestVersion = versionElement?.text()?.trim()?.split(" ")?.last()?.toIntOrNull()

                if (latestVersion != null) {
                    Log.d("VersionChecker", "Latest Version: $latestVersion")
                    callback(latestVersion)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}