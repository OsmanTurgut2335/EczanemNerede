package com.osman.eczanemnerede.screens

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import android.util.Log
import com.osman.eczanemnerede.BuildConfig


import com.google.android.gms.ads.MobileAds
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.osman.eczanemnerede.MainActivity
import com.osman.eczanemnerede.R

class Intro : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var appUpdateManager: AppUpdateManager

    private var intentLongitude: Double = 0.0
    private var intentLatitude: Double = 0.0

    private var navigated = false
    private var updateDialogShown = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        MobileAds.initialize(this) {}

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        appUpdateManager = AppUpdateManagerFactory.create(this)


        fetchLastLocation()

        checkUpdateAndProceed()
    }

    override fun onResume() {
        super.onResume()

        if (!navigated && !updateDialogShown) {
            checkUpdateAndProceed()
        }
    }

    private fun fetchLastLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    intentLatitude = it.latitude
                    intentLongitude = it.longitude
                }
            }
        }
    }

    private fun checkUpdateAndProceed() {
        val currentVc = BuildConfig.VERSION_CODE
        val currentVn = BuildConfig.VERSION_NAME
        Log.d("UpdateCheck", "Installed versionName=$currentVn versionCode=$currentVc")

        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { info ->
                val availability = info.updateAvailability()
                val availableVc = info.availableVersionCode() // Update varsa dolu
                val packageName = info.packageName()

                Log.d(
                    "UpdateCheck",
                    "Play info: pkg=$packageName availability=$availability availableVersionCode=$availableVc"
                )

                val updateAvailable = availability == UpdateAvailability.UPDATE_AVAILABLE
                Log.d("UpdateCheck", "updateAvailable=$updateAvailable")

                if (updateAvailable && !updateDialogShown) {
                    updateDialogShown = true
                    showOptionalUpdateDialog(
                        onUpdate = { openPlayStore() },
                        onSkip = { proceedToMainWithDelay() }
                    )
                } else {
                    proceedToMainWithDelay()
                }
            }
            .addOnFailureListener { e ->
                Log.e("UpdateCheck", "Update check failed: ${e.message}", e)
                proceedToMainWithDelay()
            }
    }


    private fun showOptionalUpdateDialog(onUpdate: () -> Unit, onSkip: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Güncelleme mevcut")
            .setMessage("Uygulamanın yeni bir sürümü var. Güncellemek ister misiniz?")
            .setCancelable(false)
            .setPositiveButton("Güncelle") { dialog, _ ->
                dialog.dismiss()
                onUpdate()

            }
            .setNegativeButton("Sonra") { dialog, _ ->
                dialog.dismiss()
                onSkip()
            }
            .show()
    }

    private fun openPlayStore() {
        val pkg = packageName
        val marketUri = Uri.parse("market://details?id=$pkg")
        val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$pkg")

        try {
            startActivity(Intent(Intent.ACTION_VIEW, marketUri))
        } catch (_: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }

    private fun proceedToMainWithDelay() {
        if (navigated) return
        navigated = true

        Handler(Looper.getMainLooper()).postDelayed({
            val intent1 = Intent(this, MainActivity::class.java).apply {
                putExtra("latitude", intentLatitude)
                putExtra("longitude", intentLongitude)
            }
            startActivity(intent1)
            finish()
        }, 3000)
    }
}
