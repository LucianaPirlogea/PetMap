package com.example.petmap

import MyPetsFragment
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.example.petmap.databinding.ActivityMapsBinding
import com.example.petmap.models.Pet
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.json.JSONObject

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    AddPetFragment.AddPetFragmentListener{

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private var locationPermissionGranted = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null
    private lateinit var fabAddMarker: FloatingActionButton
    private lateinit var storageRef: StorageReference
    private val petIcon: BitmapDescriptor by lazy {
        val color = ContextCompat.getColor(this, R.color.black)
        BitmapHelper.vectorToBitmap(this, R.drawable.baseline_pets_24, color)
    }

    override fun onAddPetCompleted() {
        // Switch to MyPetsFragment
        val addPetFragment = supportFragmentManager.findFragmentByTag("AddPetFragment")
        addPetFragment?.let {
            supportFragmentManager.beginTransaction()
                .remove(it)
                .commit()
        }

        // Add the MyPetsFragment
        val myPetsFragment = MyPetsFragment()
        supportFragmentManager.beginTransaction()
            .add(R.id.coordinator_layout, myPetsFragment, "MyPetsFragment")
            .addToBackStack(null)
            .commit()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_container) as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.map_container, it)
                    .commit()
            }
        mapFragment.getMapAsync(this)

        fabAddMarker = findViewById(R.id.fab_add_marker)
        fabAddMarker.setOnClickListener {
            val bundle = Bundle()
            val currentLocation = lastKnownLocation
            bundle.putParcelable("current_location", currentLocation)
            val fragment = AddPetFragment()
            fragment.arguments = bundle
            fragment.listener = this
            if (currentLocation != null) {
                supportFragmentManager.beginTransaction()
                    .hide(mapFragment)
                    .add(R.id.coordinator_layout, fragment, "AddPetFragment")
                    .addToBackStack(null)
                    .commit()
                Toast.makeText(this, "Current location added", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setInfoWindowAdapter(MarkerInfoWindowAdapter(this))
        retrieveDataFromFirebaseStorage()
        mMap.setOnMarkerClickListener { marker ->
            // Create a blue circle with a radius of 100 meters at the marker's position
            val circleOptions = CircleOptions()
                .center(marker.position)
                .radius(700.0) // the radius is in meters
                .strokeColor(Color.BLUE)
                .fillColor(Color.argb(70, 0, 0, 255)) // semi-transparent blue

            // Add the circle to the map
            mMap.addCircle(circleOptions)

            false
        }

        // Request location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
            updateLocationUI()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionGranted = false
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        if (locationPermissionGranted) {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true

            // Get the current location
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    lastKnownLocation = location
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    mMap.addMarker(
                        MarkerOptions().position(currentLatLng).title("Current Location")
                    )
                }
            }
        } else {
            mMap.isMyLocationEnabled = false
            mMap.uiSettings.isMyLocationButtonEnabled = false
            lastKnownLocation = null
        }
    }

    private fun retrieveDataFromFirebaseStorage() {
        storageRef = FirebaseStorage.getInstance().reference.child("pets")
        storageRef.listAll().addOnSuccessListener { result ->
            for (reference in result.items) {
                // Retrieve the download URL
                val jsonFileRef = reference.parent?.child(reference.name)
                jsonFileRef?.getBytes(Long.MAX_VALUE)?.addOnSuccessListener { bytes ->
                    val json = String(bytes)
                    // Parse the JSON and update the corresponding Pet object with the retrieved information
                    val petInfo = parseJson(json)
                    val latLng = LatLng(petInfo.latitude!!.toDouble(), petInfo.longitude!!.toDouble())
                    mMap.addMarker(MarkerOptions().position(latLng).title(petInfo.animal).icon(petIcon)).apply {
                        this!!.tag = petInfo
                    }
                }
            }
        }.addOnFailureListener { exception ->
            // Handle the exception
        }
    }

    private fun parseJson(json: String): Pet {
        val jsonObject = JSONObject(json)
        val latitude = jsonObject.getString("latitude")
        val longitude = jsonObject.getString("longitude")
        val date = jsonObject.getString("date")
        val animal = jsonObject.getString("animal")

        // Create and return a PetInfo object with the extracted information
        return Pet(latitude, longitude, date, animal)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
