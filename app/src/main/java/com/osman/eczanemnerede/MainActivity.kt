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

    lateinit var mAdView : AdView

    lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

         progressBar  = findViewById(R.id.loadingProgressBar)


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
    private fun setRefreshLayout(){
        refreshLayout.setOnRefreshListener {
            if (LocationHelper.isLocationEnabled(this)) {
                // Location services are enabled

                val progressBar = findViewById<ProgressBar>(R.id.loadingProgressBar)
                progressBar.visibility = View.VISIBLE // Show the ProgressBar

                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                val locationRequest = LocationRequest.create().apply {
                    interval = 10000 // Interval for updates in milliseconds
                    fastestInterval = 5000 // Fastest interval for updates in milliseconds
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
                            progressBar.visibility = View.INVISIBLE // Show the ProgressBar
                            // Enable the textView here (if needed)
                            textView.isEnabled = true

                            // Handle other location updates or UI operations if necessary
                        }
                    }
                }


                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        null
                    )
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
            } else {
                Toast.makeText(this, getString(R.string.enable_location_services), Toast.LENGTH_LONG).show()

            }

            refreshLayout.isRefreshing = false
        }
    }



    private fun checkLocationPermission() {
        if (hasLocationPermissions()) {
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

    private fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
         val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
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
                    // Handle the case where the user denied permission
                    permissionDeniedCount++
                    showPermissionDeniedDialog()
                }
            }

        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }


    private fun showPermissionDeniedDialog() {
        val dialogBuilder = AlertDialog.Builder(this@MainActivity)
        dialogBuilder.setTitle(getString(R.string.permission_denied))
            .setMessage(getString(R.string.permission_message))
            .apply {
                if (permissionDeniedCount >= 2) {
                    setNegativeButton(getString(R.string.open_settings)) { dialog, _ ->
                        dialog.dismiss()
                        openAppSettings()
                    }
                } else {
                    setPositiveButton(getString(R.string.retry)) { _, _ ->
                        requestLocationPermissions()
                    }
                }
            }
            .setCancelable(false)
            .show()
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