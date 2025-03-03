package com.osman.eczanemnerede


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
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
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
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
    var  versionCode : Int = 0
    lateinit var mAdView : AdView


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                getLocationOsman()
            } else {
                // Handle the case where the user denied permission
                permissionDeniedCount++
                showPermissionDeniedDialog()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        versionCode = getAppVersionCode(this)
        if(versionCode< 12){
            showUpdateNotification()
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

        if (!isLocationEnabled(this)) {
            textView.isEnabled = true
            checkLocationPermission()
        } else {

            // Continue with the location permission check
            checkLocationPermission()
        }
        intent2 = Intent(this, LocationBasedPharmacies::class.java)
        refreshLayout = findViewById(R.id.swipeRefreshLayout)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        refreshLayout.setOnRefreshListener {
            if (isLocationEnabled(this)) {
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
                Toast.makeText(
                    this,
                    "Lütfen konum servisinizi aktif hale getirin",
                    Toast.LENGTH_LONG
                ).show()
            }

            refreshLayout.isRefreshing = false
        }


            // Get the current installed version
            val currentVersion = getAppVersionCode(this)

            // Fetch the latest version from Play Store
            checkLatestVersion { latestVersion ->
                if (latestVersion > currentVersion) {
                    showUpdateNotification()
                }
            }

    }


    private fun checkLatestVersion(callback: (Int) -> Unit) {
        val appPackageName = packageName
        val url = "https://play.google.com/store/apps/details?id=$appPackageName&hl=en"

        Thread {
            try {
                val document = Jsoup.connect(url).get()
                val versionElement = document.select("div[itemprop=softwareVersion]").first()

                val latestVersion = versionElement?.text()?.trim()?.split(" ")?.last()?.toIntOrNull()

                if (latestVersion != null) {
                    runOnUiThread {
                        callback(latestVersion)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun checkLocationPermission() {
        if (hasLocationPermissions()) {
            getLocationOsman()

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
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    private fun getUserLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        intent_Latitude = latitude
                        intent_Longitude= longitude
                        textView.isEnabled=true


                    } ?: run {

                    }
                }
        }
    }




    private fun showPermissionDeniedDialog() {
        val dialogBuilder = AlertDialog.Builder(this@MainActivity)
        dialogBuilder.setTitle("Permission Denied")
            .setMessage("Location permission is required to use this feature. Please grant the permission in the settings.")
            .apply {
                if (permissionDeniedCount >= 2) {
                    setNegativeButton("Open Settings") { dialog, _ ->
                        dialog.dismiss()
                        openAppSettings()
                    }
                } else {
                    setPositiveButton("Retry") { _, _ ->
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
    private fun nobetciClicked(v: View){
        intentToNobetci()


    }




    private fun showEnableLocationDialog() {
        val dialogBuilder = AlertDialog.Builder(this@MainActivity)
        dialogBuilder.setTitle("Konum Bulunamadı")
            .setMessage("Lütfen konumunuzu aktif hale getirin ve sayfayı yenileyerek " +
                    "butonun basılabilir olmasını bekleyin ")
            .setPositiveButton("Tamam") { _, _ ->

            }

            .setCancelable(false)
            .show()
    }
    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


    fun getLocationOsman(){
        if (isLocationEnabled(this)) {
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
                    for (location in locationResult.locations) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        intent_Latitude=latitude
                        intent_Longitude=longitude
                        textView.isEnabled=true
                        progressBar.visibility = View.GONE
                        // Handle location updates here
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
            Toast.makeText(
                this,
                "Lütfen konum servisinizi aktif hale getirin",
                Toast.LENGTH_LONG
            ).show()
        }


    }


    private fun showUpdateNotification() {
        AlertDialog.Builder(this)
            .setTitle("Güncelleme Gerekli")
            .setMessage("Uygulamanın yeni bir versiyonu var. Lütfen güncelleyin.")
            .setPositiveButton("Güncelle") { _, _ ->
                openPlayStore()
            }
            .setNegativeButton("Daha Sonra", null)
            .setCancelable(false)
            .show()
    }

    private fun openPlayStore() {
        val appPackageName = packageName
        try {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName"))
            )
        } catch (e: Exception) {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName"))
            )
        }
    }

    private fun getAppVersionCode(context: Context): Int {
        return try {
            val pInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            -1
        }
    }

}