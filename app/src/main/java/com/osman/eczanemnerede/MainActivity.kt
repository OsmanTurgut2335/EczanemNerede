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
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.app.core.utils.LocationHelper
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.location.*
import com.osman.eczanemnerede.core.VersionController
import com.osman.eczanemnerede.screens.AllPharmacies
import com.osman.eczanemnerede.screens.LocationBasedPharmacies
import com.osman.eczanemnerede.screens.NobetciEczaneler
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 123
    private var permissionDeniedCount = 0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var textView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var mAdView: AdView
    private lateinit var intent2: Intent
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    var intent_Latitude: Double = 0.0
    var intent_Longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.loadingProgressBar)
        textView = findViewById(R.id.locationPharmaciesText)
        refreshLayout = findViewById(R.id.swipeRefreshLayout)
        mAdView = findViewById(R.id.adView)
        intent2 = Intent(this, LocationBasedPharmacies::class.java)

        textView.isEnabled = false

        setupAds()
        setupLocationClient()
        setupPermissionLauncher()
        setupRefreshLayout()
        checkInitialLocationState()
        checkAppVersion()
    }

    private fun setupAds() {
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        mAdView.adListener = object : AdListener() {
            override fun onAdClosed() {
                mAdView.visibility = View.INVISIBLE
            }
        }
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                if (LocationHelper.isLocationEnabled(this)) {
                    progressBar.visibility = View.VISIBLE
                }
                startLocationFetch()
            } else {
                permissionDeniedCount++
            }
        }
    }

    private fun checkInitialLocationState() {
        if (!LocationHelper.isLocationEnabled(this)) {
            textView.isEnabled = true
        }
        checkLocationPermission()
    }

    private fun checkAppVersion() {
        VersionController.checkLatestVersion(this) { latestVersion ->
            val currentVersion = VersionController.getAppVersionCode(this)
            if (latestVersion > currentVersion) {
                VersionController.showUpdateNotification(this, packageName)
            }
        }
    }

    private fun setupRefreshLayout() {
        refreshLayout.setOnRefreshListener {
            if (!LocationHelper.hasLocationPermissions(this)) {
                Toast.makeText(this, getString(R.string.permission_denied_message), Toast.LENGTH_LONG).show()
                if (permissionDeniedCount >= 2) showPermissionSettingsDialog()
                else requestLocationPermissions()
                refreshLayout.isRefreshing = false
                return@setOnRefreshListener
            }

            if (!LocationHelper.isLocationEnabled(this)) {
                Toast.makeText(this, getString(R.string.enable_location_services), Toast.LENGTH_LONG).show()
                refreshLayout.isRefreshing = false
                return@setOnRefreshListener
            }

            progressBar.visibility = View.VISIBLE

            val locationRequest = LocationRequest.create().apply {
                interval = 10000
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    if (locationResult.locations.isNotEmpty()) {
                        val location = locationResult.locations[0]
                        intent_Latitude = location.latitude
                        intent_Longitude = location.longitude
                        progressBar.visibility = View.GONE
                        textView.isEnabled = true
                    }
                }
            }

            try {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            } catch (e: SecurityException) {
                e.printStackTrace()
                progressBar.visibility = View.GONE
            }

            refreshLayout.isRefreshing = false
        }
    }

    private fun checkLocationPermission() {
        if (LocationHelper.hasLocationPermissions(this)) {
            startLocationFetch()
        } else {
            requestLocationPermissions()
        }
    }

    private fun startLocationFetch() {
        LocationHelper.getCurrentLocation(
            context = this,
            onLocationReceived = { latitude, longitude ->
                intent_Latitude = latitude
                intent_Longitude = longitude
                textView.isEnabled = true
                progressBar.visibility = View.GONE
            },
            onPermissionRequest = {
                requestLocationPermissions()
            }
        )
    }

    private fun requestLocationPermissions() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun showPermissionSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_required))
            .setMessage(getString(R.string.permission_settings_message))
            .setPositiveButton(getString(R.string.open_settings)) { _, _ -> openAppSettings() }
            .setNegativeButton(getString(R.string.cancel), null)
            .setCancelable(false)
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }

    fun allBtnClicked(v: View) {
        startActivity(Intent(this, AllPharmacies::class.java))
    }

    fun locationClicked(v: View) {
        if (intent_Latitude != 0.0) {
            intent2.putExtra("latitude", intent_Latitude)
            intent2.putExtra("longitude", intent_Longitude)
            startActivity(intent2)
        } else {
            textView.isEnabled = false
            showEnableLocationDialog()
        }
    }

    fun nobetciClicked(v: View) {
        intentToNobetci()
    }

    private fun intentToNobetci() {
        val progressView = layoutInflater.inflate(R.layout.dialog_loading, null)
        val loadingDialog = AlertDialog.Builder(this)
            .setView(progressView)
            .setCancelable(false)
            .create()
        loadingDialog.show()

        fetchLatestPdfUrl { pdfUrl ->
            runOnUiThread {
                loadingDialog.dismiss()
                val intent = Intent(this, NobetciEczaneler::class.java)
                if (pdfUrl != null) intent.putExtra("pdf_url", pdfUrl)
                else intent.putExtra("error_message", "PDF bağlantısı bulunamadı!")
                startActivity(intent)
            }
        }
    }

    private fun showEnableLocationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.location_not_found))
            .setMessage(getString(R.string.location_not_found_message))
            .setPositiveButton(getString(R.string.ok), null)
            .setCancelable(false)
            .show()
    }

    private fun fetchLatestPdfUrl(callback: (String?) -> Unit) {
        Thread {
            try {
                val doc = Jsoup.connect("https://keo.org.tr/kategori/nobetle-ilgili-462077/")
                    .userAgent("Mozilla/5.0")
                    .get()

                val currentMonth = SimpleDateFormat("MMMM", Locale("tr", "TR"))
                    .format(Date())
                    .lowercase(Locale("tr", "TR"))

                val selector = "a[href*=-$currentMonth][href*=-nobetci-eczane]"
                val latestAnnouncementElement = doc.select(selector).first()

                val rawHref = latestAnnouncementElement?.attr("href")
                val fullDetailUrl = when {
                    rawHref == null -> null
                    rawHref.startsWith("http") -> rawHref
                    rawHref.startsWith("/") -> "https://keo.org.tr$rawHref"
                    else -> "https://keo.org.tr/$rawHref"
                }

                if (fullDetailUrl == null) {
                    callback(null)
                    return@Thread
                }

                val detailDoc = Jsoup.connect(fullDetailUrl).get()
                val pdfElement = detailDoc.selectFirst("a[href$=.pdf]")
                val pdfUrl = pdfElement?.attr("href")

                callback(pdfUrl)
            } catch (e: Exception) {
                e.printStackTrace()
                callback(null)
            }
        }.start()
    }
}
