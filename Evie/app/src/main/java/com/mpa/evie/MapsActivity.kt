package com.mpa.evie

import android.Manifest.permission
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.mpa.evie.api.ocm.OpenChargeMapAPI
import com.mpa.evie.api.ocm.models.Charger
import com.mpa.evie.chargers.ChargerActivity
import com.mpa.evie.chargers.FilterFragment
import com.mpa.evie.chargers.Filters
import com.mpa.evie.chargers.FiltersData
import com.mpa.evie.databinding.ActivityMapsBinding
import com.mpa.evie.info.CarActivity
import com.mpa.evie.models.User
import com.mpa.evie.utils.getCostPerKwhFromString
import com.mpa.evie.utils.vectorToBitmap
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, Filters {

    companion object {
        private val PERMISSIONS =
            arrayOf(permission.ACCESS_FINE_LOCATION, permission.ACCESS_COARSE_LOCATION)
        private const val ALL_PERMISSIONS_RESULT = 101
        private const val TAG = "MapsActivity"
        const val OCM_BASE_URL = "https://api.openchargemap.io/v3/"
        const val OCM_API_KEY = BuildConfig.OCM_API_KEY
    }

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private var user: User? = null
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private lateinit var retrofit: Retrofit
    private lateinit var api: OpenChargeMapAPI

    private lateinit var btnFilters: FloatingActionButton

    private lateinit var filters: FiltersData

    private lateinit var lvSearchChargers: ListView
    private lateinit var svChargers: SearchView
    private lateinit var chargerListAdapter: ArrayAdapter<Charger>
    private var searchChargerList = mutableListOf<Charger?>()

    private var permissionsToRequest: Array<String>? = null
    private val permissionsRejected = mutableListOf<String>()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var longitude = 0.0
    private var latitude = 0.0

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var filterFragment: FilterFragment

    private var mapReady = false
    private var locationReady = false
    private var setupCalled = false

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        getSupportActionBar()?.hide()

        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

//        filter fragment (hide)
        filterFragment = supportFragmentManager.findFragmentById(R.id.filters) as FilterFragment
        supportFragmentManager.beginTransaction().hide(filterFragment).commit()

        btnFilters = findViewById(R.id.btnFilters)
        btnFilters.setOnClickListener {
            btnFilters.visibility = View.GONE
            svChargers.visibility = View.GONE
            supportFragmentManager.beginTransaction()
                .hide(mapFragment)
                .show(filterFragment)
                .commit()
        }
        filters = FiltersData(1, 5.0)

        lvSearchChargers = findViewById(R.id.lvSearchChargers)
        svChargers = findViewById(R.id.svChargers)
        chargerListAdapter = ArrayAdapter<Charger>(
            this,
            android.R.layout.simple_list_item_1,
            searchChargerList
        )

        lvSearchChargers.adapter = chargerListAdapter
        lvSearchChargers.setOnItemClickListener { _, _, position, _ ->
            startActivity(
                Intent(applicationContext, ChargerActivity::class.java).putExtra(
                    "charger",
                    chargerListAdapter.getItem(position)
                )
            )
        }

        svChargers.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                chargerListAdapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    lvSearchChargers.visibility = View.GONE
                    btnFilters.visibility = View.VISIBLE
                } else {
                    lvSearchChargers.visibility = View.VISIBLE
                    btnFilters.visibility = View.GONE
                }
                chargerListAdapter.filter.filter(newText)
                return false
            }
        })

        permissionsToRequest = findUnAskedPermissions(PERMISSIONS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissionsToRequest!!.isNotEmpty()) requestPermissions(
            permissionsToRequest!!,
            ALL_PERMISSIONS_RESULT
        )

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                longitude = location!!.longitude
                latitude = location.latitude
                locationReady = true
                if (mapReady && !setupCalled) {
                    setupCalled = true
                    setupMap()
                }
            }
            .addOnFailureListener { exception: Exception ->
                Log.e(TAG, exception.message!!)
                Toast.makeText(this, "Fetching location failed", Toast.LENGTH_SHORT).show()
            }

        retrofit = Retrofit.Builder()
            .baseUrl(OCM_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(OpenChargeMapAPI::class.java)

//        Setup bottom navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavigationView.selectedItemId = R.id.navigation_maps // home activity selected

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(applicationContext, HomeActivity::class.java))
                    overridePendingTransition(0, 0)
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(applicationContext, ProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                }
            }
            false
        }

        val query = db.collection("users").document(auth.currentUser!!.uid)

        query.get().addOnSuccessListener { document ->
            if (document != null) {
                user = document.toObject<User>()
                processUser(user)
            } else {
                Log.d(TAG, "User not found (Firestore)")
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "Get user failed with ", exception)
        }
    }

    private fun processUser(user: User?) {
        if (user?.car?.make.isNullOrEmpty() || user?.networks!!.isEmpty()) {
//            if user has not added their car or networks, launch info flow
            startActivity(
                Intent(
                    applicationContext,
                    CarActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            finish()
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap // set mMap
        mMap.setOnMarkerClickListener(this@MapsActivity::onMarkerClick)
        mMap.setOnCameraIdleListener(this@MapsActivity::getChargers)
        mapReady = true
        if (locationReady && !setupCalled) {
            setupMap()
            setupCalled = true
        }
    }

    private fun setupMap() {
//        set location of map and get initial chargers to show
        val userLoc = LatLng(latitude, longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLoc, 11F))
        getChargers()
    }

    private fun getChargers() {
        if (user?.car?.connectors?.isEmpty() ?: true) return
        if (user?.networks?.isEmpty() ?: true) return

        val bounds = mMap.projection.visibleRegion.latLngBounds
        val boundingBox = listOf(
            Pair(bounds.southwest.latitude, bounds.northeast.longitude),
            Pair(bounds.northeast.latitude, bounds.southwest.longitude)
        ).toString().replace("[", "").replace("]", "").replace(" ", "")
        val connectorIDs = user?.car?.connectors?.joinToString()?.replace(" ", "")
        val operatorIDs = user?.networks?.map { it.ocmID }?.joinToString()?.replace(" ", "")

        api.getChargers(
            OCM_API_KEY,
            boundingBox,
            connectiontypeid = connectorIDs ?: "",
            operatorid = operatorIDs ?: ""
        )
            ?.enqueue(object : Callback<List<Charger?>?> {
                @RequiresApi(Build.VERSION_CODES.M)
                override fun onResponse(
                    call: Call<List<Charger?>?>,
                    response: Response<List<Charger?>?>
                ) {
                    mMap.clear() // clear markers
                    var chargerList: List<Charger?> = response.body() ?: emptyList()

                    chargerListAdapter.clear()
                    chargerListAdapter.addAll(chargerList)
                    chargerListAdapter.notifyDataSetChanged()

//                filter for max cost and min charging points
                    chargerList = chargerList.filter {
                        (it!!.NumberOfPoints ?: 0) >= (filters.minPorts!!) &&
                                (getCostPerKwhFromString(it.UsageCost)
                                    ?: 0.0) <= filters.maxCostPerKwh!!
                    }
//                    filter for charge speed
                    if (filters.speedSlow && !filters.speedFast) {
                        chargerList = chargerList.filter {
                            !it!!.Connections.map { m -> m.Level!!.IsFastChargeCapable }
                                .contains(true)
                        }
                    } else if (!filters.speedSlow && filters.speedFast) {
                        chargerList = chargerList.filter {
                            it!!.Connections.map { m -> m.Level!!.IsFastChargeCapable }
                                .contains(true)
                        }
                    } // if slow and fast both selected, leave as-is

                    chargerList.forEach {
                        val marker = mMap.addMarker(
                            MarkerOptions().position(
                                LatLng(
                                    it?.AddressInfo?.Latitude!!,
                                    it.AddressInfo?.Longitude!!
                                )
                            ).title(it.AddressInfo?.Title)
                                .icon(
                                    vectorToBitmap(
                                        ResourcesCompat.getDrawable(
                                            resources,
                                            R.drawable.ic_map_marker,
                                            null
                                        ), resources.getColor(R.color.orange, null)
                                    )
                                )
                        )
                        marker!!.tag = it // set marker tag to charger (formerly it.ID)
                    }
                }

                override fun onFailure(call: Call<List<Charger?>?>, t: Throwable) {
                    Log.e(TAG, t.message ?: "")
                }
            })
    }

    private fun findUnAskedPermissions(wanted: Array<String>): Array<String> {
        var result = arrayOf<String>()
        wanted.forEach {
            if (!hasPermission(it)) {
                result += it
            }
        }
        return result
    }

    private fun hasPermission(permission: String): Boolean {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this@MapsActivity)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ALL_PERMISSIONS_RESULT -> {
                for (perms in permissionsToRequest!!) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms)
                    }
                }
                if (permissionsRejected.size > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(
                        permissionsRejected[0]
                    )
                ) {
                    showMessageOKCancel(
                        "These permissions are required for Evie to function."
                    ) { _, _ ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(
                                permissionsRejected.toTypedArray(),
                                ALL_PERMISSIONS_RESULT
                            )
                        }
                    }
                    return
                }
            }
        }
    }

    private fun onMarkerClick(marker: Marker): Boolean {
        val tag = marker.tag as Charger
        Log.d(TAG, "Processing click on marker ID: $tag.ID")
        startActivity(
            Intent(applicationContext, ChargerActivity::class.java).putExtra(
                "charger",
                tag
            )
        )
        return true
    }

    override fun updateFilters(filtersData: FiltersData) {
        Log.d(TAG, "Updating filters to: $filtersData")
        filters = filtersData
        btnFilters.visibility = View.VISIBLE
        svChargers.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction().hide(filterFragment).show(mapFragment)
            .commit()
        getChargers() // update chargers on map
    }

}
