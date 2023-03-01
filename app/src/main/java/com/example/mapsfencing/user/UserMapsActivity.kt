package com.example.mapsfencing.user

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import com.example.mapsfencing.R
import com.example.mapsfencing.authentication.LoginActivity
import com.example.mapsfencing.databinding.ActivityUserMapsBinding
import com.example.mapsfencing.preferences.PreferenceManager
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class UserMapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleApiClient.OnConnectionFailedListener,
    GoogleApiClient.ConnectionCallbacks,
    LocationListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityUserMapsBinding

    private val MY_PERMISSION_REQUEST_CODE = 1792
    private val PLAY_SERVICES_RESOLUTION_REQUEST = 300193

    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var apiClient: GoogleApiClient
    private var lastlocation: Location? = null
    private lateinit var firelocation: GeoFire
    private  var marker: Marker?=null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var preferenceManager: PreferenceManager

    private val UPDATE_INTERVAL = 5000
    private val FASTEST_INTERVAL = 3000
    private val DISPLACEMENT = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)

        this.supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar!!.setDisplayShowCustomEnabled(true)
        supportActionBar!!.setCustomView(R.layout.actionbar_layout)
        val view = supportActionBar!!.customView

        val logout = view.findViewById<AppCompatButton>(com.example.mapsfencing.R.id.logout)
        val userType = view.findViewById<AppCompatTextView>(R.id.user_mode)

        userType.text = "User Maps"

        logout.setOnClickListener {
            preferenceManager.logout()
            FirebaseAuth.getInstance().signOut()

            startActivity(Intent(this@UserMapsActivity, LoginActivity::class.java))
            finish()
        }

        val window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = this.resources.getColor(R.color.icon_splash_background)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        databaseReference = FirebaseDatabase.getInstance().getReference("UserLocation")
        firelocation = GeoFire(databaseReference)

        setupLocation()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

//        // Add a marker in Sydney and move the camera
//        val location = LatLng(Lastlocation.latitude, Lastlocation.longitude)
//        mMap.addMarker(MarkerOptions().position(location).title("You"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
    }

    private fun setupLocation() {

        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                MY_PERMISSION_REQUEST_CODE
            )

        } else {

            if (checkPlayServices()) {

                buildGoogleMapsApiClient()
                createLocationRequest()
//                lastlocation = LocationServices.FusedLocationApi.getLastLocation(apiClient)
               fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                   lastlocation=it
                   displayLocation()
                }
            }
        }

    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.setInterval(UPDATE_INTERVAL.toLong())
        locationRequest.setFastestInterval(FASTEST_INTERVAL.toLong())
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setSmallestDisplacement(DISPLACEMENT.toFloat())
    }

    private fun displayLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }

        if (lastlocation != null) {
            Log.d(
                "locationCurrent",
                "displayLocationUpdated: ${lastlocation!!.latitude} ${lastlocation!!.longitude}"
            )

            Toast.makeText(this@UserMapsActivity, "Got Location!", Toast.LENGTH_SHORT).show()

            firelocation.setLocation(
                "${preferenceManager.getuser().id}", GeoLocation(
                    lastlocation!!.latitude,
                    lastlocation!!.longitude
                )
            )
            { key, error ->

                if (marker != null)
                    marker!!.remove()

                mMap.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            lastlocation!!.latitude,
                            lastlocation!!.longitude
                        )
                    ).title("You")
                )!!

                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(lastlocation!!.latitude, lastlocation!!.longitude), 15.0f
                    )
                )
            }
        } else {
            Toast.makeText(this@UserMapsActivity, "Unable to get Location!", Toast.LENGTH_SHORT)
                .show()
        }

    }


    private fun buildGoogleMapsApiClient() {
        apiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
        apiClient.connect()
    }

    private fun checkPlayServices(): Boolean {
        val resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) Objects.requireNonNull(
                GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this,
                    PLAY_SERVICES_RESOLUTION_REQUEST
                )
            )?.show() else {
                Toast.makeText(this, "Device is not supported", Toast.LENGTH_LONG).show()
                finish()
            }
            return false
        }
        return true
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("Not yet implemented")
    }

    override fun onConnected(p0: Bundle?) {
        displayLocation()
        startLocationUpdates()
    }

    private fun startLocationUpdates() {

        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(
            locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {

                    onLocationChanged(locationResult.lastLocation!!)
                }
            },
            Looper.myLooper()
        )
    }

    override fun onConnectionSuspended(p0: Int) {
        apiClient.connect()
    }

    override fun onLocationChanged(locationChanged: Location) {
        lastlocation = locationChanged
        displayLocation()
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {
            MY_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (checkPlayServices()) {

                        buildGoogleMapsApiClient()
                        createLocationRequest()
                        displayLocation()

                    }
                }
            }
            else -> throw IllegalStateException("Unexpected value: $requestCode")
        }
    }
}