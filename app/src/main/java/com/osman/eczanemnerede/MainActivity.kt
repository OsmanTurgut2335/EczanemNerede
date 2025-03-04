package com.osman.eczanemnerede
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.app.core.utils.LocationHelper
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.osman.eczanemnerede.core.VersionController
import com.osman.eczanemnerede.screens.AllPharmacies
import com.osman.eczanemnerede.screens.LocationBasedPharmacies
import com.osman.eczanemnerede.screens.NobetciEczaneler
import org.jsoup.Jsoup


class MainActivity : ComponentActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 123
    private var permissionDeniedCount = 0

    var intent_Latitude : Double = 0.0
    private lateinit var textView: TextView
    var intent_Longitude :Double = 0.0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var refreshLayout:SwipeRefreshLayout
    lateinit var intent2: Intent
    // Declare this as a global variable
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    lateinit var mAdView : AdView

    lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

         progressBar  = findViewById(R.id.loadingProgressBar)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Show ProgressBar when permission is granted
                progressBar.visibility = View.VISIBLE

                LocationHelper.getCurrentLocation(
                    context = this,
                    onLocationReceived = { latitude, longitude ->
                        // Location successfully received
                        intent_Latitude = latitude
                        intent_Longitude = longitude
                        textView.isEnabled = true

                        // Hide ProgressBar
                        progressBar.visibility = View.GONE
                    },
                    onPermissionRequest = {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            1001
                        )
                    }
                )
            } else {
                // Permission denied, increase count
                permissionDeniedCount++
            }
        }


        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        mAdView.adListener = object: AdListener() {
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
                mAdView.visibility = View.INVISIBLE
            }


        }

        textView = findViewById(R.id.locationPharmaciesText)
        textView.isEnabled=false

        if (!LocationHelper.isLocationEnabled(this)) {
            textView.isEnabled = true
            checkLocationPermission()
        } else {

            // Continue with the location permission check
            checkLocationPermission()
        }
        intent2 = Intent(this, LocationBasedPharmacies::class.java)
        refreshLayout = findViewById(R.id.swipeRefreshLayout)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setRefreshLayout()



        VersionController.checkLatestVersion(this) { latestVersion ->
            val currentVersion = VersionController.getAppVersionCode(this)
            if (latestVersion > currentVersion) {
                VersionController.showUpdateNotification(this, packageName)
            }
        }


    }
    private fun setRefreshLayout() {
        refreshLayout.setOnRefreshListener {
            // Always show the permission denied toast if there's no permission
            if (!LocationHelper.hasLocationPermissions(this)) {
                Toast.makeText(this, getString(R.string.permission_denied_message), Toast.LENGTH_LONG).show()

                // If permission is denied 2+ times, show a popup instead of asking again
                if (permissionDeniedCount >= 2) {
                    showPermissionSettingsDialog()
                } else {
                    requestLocationPermissions()
                }
                refreshLayout.isRefreshing = false
                return@setOnRefreshListener
            }

            if (!LocationHelper.isLocationEnabled(this)) {
                // Location services are disabled, show toast message
                Toast.makeText(this, getString(R.string.enable_location_services), Toast.LENGTH_LONG).show()
                refreshLayout.isRefreshing = false
                return@setOnRefreshListener
            }

            // Show ProgressBar when refresh starts
            progressBar.visibility = View.VISIBLE

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            val locationRequest = LocationRequest.create().apply {
                interval = 10000
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)

                    if (locationResult.locations.isNotEmpty()) {
                        val location = locationResult.locations[0] // Get the latest location

                        val latitude = location.latitude
                        val longitude = location.longitude

                        // Update the intent_Latitude and intent_Longitude values
                        intent_Latitude = latitude
                        intent_Longitude = longitude

                        // Hide ProgressBar once location is received
                        progressBar.visibility = View.GONE
                        textView.isEnabled = true
                    }
                }
            }

            if (LocationHelper.hasLocationPermissions(this)) {
                try {
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        null
                    )
                } catch (e: SecurityException) {
                    e.printStackTrace() // Log the error
                    progressBar.visibility = View.GONE // Hide ProgressBar on error
                }
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }

            refreshLayout.isRefreshing = false
        }
    }







    private fun checkLocationPermission() {
        if (LocationHelper.hasLocationPermissions(this)) {
            LocationHelper.getCurrentLocation(
                context = this,
                onLocationReceived = { latitude, longitude ->
                    intent_Latitude = latitude
                    intent_Longitude = longitude
                    textView.isEnabled = true
                    progressBar.visibility = View.GONE
                },
                onPermissionRequest = {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        1001
                    )
                }
            )


        } else {
            requestLocationPermissions()
        }
    }


    private fun requestLocationPermissions() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun showPermissionSettingsDialog() {
        val dialogBuilder = AlertDialog.Builder(this@MainActivity)
        dialogBuilder.setTitle(getString(R.string.permission_required))
            .setMessage(getString(R.string.permission_settings_message))
            .setPositiveButton(getString(R.string.open_settings)) { _, _ ->
                openAppSettings()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .setCancelable(false)
            .show()
    }



    private fun showPermissionDeniedDialog() {
        Toast.makeText(this, getString(R.string.permission_denied_message), Toast.LENGTH_LONG).show()
    }


    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }



    fun allBtnClicked(v : View){
        val intent = Intent(this, AllPharmacies::class.java)
        startActivity(intent)

    }

    fun locationClicked(v: View){

        if(!(intent_Latitude.equals(0.0))) {

            intent2.putExtra("latitude", intent_Latitude)
            intent2.putExtra("longitude", intent_Longitude)

            startActivity(intent2)

        }else{
            textView.isEnabled = false
            showEnableLocationDialog()
        }
    }


    private fun intentToNobetci(){
        val intent = Intent(this, NobetciEczaneler::class.java)
        startActivity(intent)
    }
     fun nobetciClicked(v: View){
        intentToNobetci()
    }

    private fun showEnableLocationDialog() {
        val dialogBuilder = AlertDialog.Builder(this@MainActivity)
        dialogBuilder.setTitle(getString(R.string.location_not_found))
            .setMessage(getString(R.string.location_not_found_message))
            .setPositiveButton(getString(R.string.ok)) { _, _ -> }
            .setCancelable(false)
            .show()
    }


}