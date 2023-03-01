package com.example.mapsfencing.admin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
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
import com.example.mapsfencing.authentication.LoginActivity
import com.example.mapsfencing.databinding.ActivityAdminMapsBinding
import com.example.mapsfencing.models.LocationUser
import com.example.mapsfencing.notification_services.MyNotificationSender
import com.example.mapsfencing.preferences.PreferenceManager
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList


class AdminMapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleApiClient.OnConnectionFailedListener,
    GoogleApiClient.ConnectionCallbacks,
    LocationListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityAdminMapsBinding

    private val MY_PERMISSION_REQUEST_CODE = 1792
    private val PLAY_SERVICES_RESOLUTION_REQUEST = 300193

    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var apiClient: GoogleApiClient
    private var lastlocation: Location? = null
    private lateinit var firelocation: GeoFire
    private var marker: Marker? = null
    private var geoQuery: GeoQuery? = null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var preferenceManager: PreferenceManager

    private val UPDATE_INTERVAL = 5000
    private val FASTEST_INTERVAL = 3000
    private val DISPLACEMENT = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar!!.setDisplayShowCustomEnabled(true)
        supportActionBar!!.setCustomView(com.example.mapsfencing.R.layout.actionbar_layout)
        val view = supportActionBar!!.customView

        val logout = view.findViewById<AppCompatButton>(com.example.mapsfencing.R.id.logout)
        val userType = view.findViewById<AppCompatTextView>(com.example.mapsfencing.R.id.user_mode)

        userType.text = "Admin Maps"

        logout.setOnClickListener {
            preferenceManager.logout()

            startActivity(Intent(this@AdminMapsActivity, LoginActivity::class.java))
            finish()
        }


        val window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = this.resources.getColor(com.example.mapsfencing.R.color.icon_splash_background)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(com.example.mapsfencing.R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient = getFusedLocationProviderClient(this)

        databaseReference = FirebaseDatabase.getInstance().getReference("UserLocation")
        firelocation = GeoFire(databaseReference)

        preferenceManager = PreferenceManager(this)

        setupLocation()
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
                    lastlocation = it
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

            Toast.makeText(this@AdminMapsActivity, "Got Location!", Toast.LENGTH_SHORT).show()

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
            Toast.makeText(this@AdminMapsActivity, "Unable to get Location!", Toast.LENGTH_SHORT)
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


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        displayLocation()
        var circle: Circle? = null
        var latlong: LatLng? = null

        mMap.setOnMapLongClickListener {

            if (circle == null) {

                circle = mMap.addCircle(
                    CircleOptions()
                        .center(it)
                        .radius(500.0)
                        .strokeColor(0x220000FF)
                        .fillColor(0x220000FF)
                        .strokeWidth(5.0f)
                )

                latlong = it
            } else {
                circle!!.remove()
                circle = null

            }
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("UserLocation")
        databaseReference.keepSynced(true)

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {

                    val latlngref = dataSnapshot.child("l").ref

                    latlngref.addValueEventListener(object :ValueEventListener{

                        override fun onDataChange(latlngsnap: DataSnapshot) {

//                            val location = latlngsnap.getValue(ArrayList::class.java)

                            for (latlng in latlngsnap.children) {
                                //pass user latlng to the function
                                geoQuery = firelocation.queryAtLocation(
                                    GeoLocation(
                                        latlng.value as Double,
                                        latlng.value as Double
                                    ), 0.5)

                                Log.d("latitude", "onDataChange: "+latlng.value as Double)

                                mMap.addCircle(
                                    CircleOptions()
                                        .center(latlong!!)
                                        .radius(500.0)
                                        .strokeColor(0x220000FF)
                                        .fillColor(0x220000FF)
                                        .strokeWidth(5.0f)
                                )

                                geoQuery!!. addGeoQueryEventListener (object : GeoQueryEventListener {

                                    override fun onKeyEntered(key: String, location: GeoLocation) {
                                        //sending notification
                                        val notificationSender = MyNotificationSender(
                                            "/topics/all",
                                            "User",
                                            String.format("entered in your area"),
                                            this@AdminMapsActivity, this@AdminMapsActivity
                                        )

                                        notificationSender.send_notification()
                                    }

                                    override fun onKeyExited(key: String) {
                                        //sending notification
                                        val notificationSender = MyNotificationSender(
                                            "/topics/all",
                                            "User", String.format("exited in your area"),
                                            this@AdminMapsActivity, this@AdminMapsActivity
                                        )

                                        notificationSender.send_notification()
                                    }

                                    override fun onKeyMoved(key: String, location: GeoLocation) {
                                        //sending notification
                                        val notificationSender = MyNotificationSender(
                                            "/topics/all",
                                            "User",
                                            String.format("user moved in your area"),
                                            this@AdminMapsActivity, this@AdminMapsActivity
                                        )

                                        notificationSender.send_notification()
                                    }

                                    override fun onGeoQueryReady() {

                                    }

                                    override fun onGeoQueryError(error: DatabaseError) {
                                        Log.d("unableTogetUser", "onGeoQueryError: $error")

                                        Toast.makeText(this@AdminMapsActivity,"$error",Toast.LENGTH_SHORT).show()
                                    }
                                })


                            }



                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })

                }
            }

            override fun onCancelled(error: DatabaseError) {
                throw error.toException()
            }
        })
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
        getFusedLocationProviderClient(this).requestLocationUpdates(
            locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {

                    onLocationChanged(locationResult.lastLocation!!)
                }
            },
            Looper.myLooper()
        )
    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun onLocationChanged(locationChanged: Location) {
        lastlocation = locationChanged
        displayLocation()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

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