package com.osman.eczanemnerede

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class Intro : AppCompatActivity() {

    lateinit var intent1: Intent // Declare intent1 as a property
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var intent_Longitude :Double = 0.0
    var intent_Latitude : Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
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


                    } ?: run {

                    }
                }
        }

        intent1 = Intent(this, MainActivity::class.java) // Initialize intent1 here
        intent1.putExtra("latitude", intent_Latitude)
        intent1.putExtra("longitude", intent_Longitude)

        Handler(Looper.getMainLooper()).postDelayed({


            startActivity(intent1)
            finish() // Finish the intro activity
        }, 3000) // Delay in milliseconds (e.g., 3000ms = 3 seconds)


    }
}

