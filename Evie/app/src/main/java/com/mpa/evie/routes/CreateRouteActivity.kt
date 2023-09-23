package com.mpa.evie.routes

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.slider.Slider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.maps.android.PolyUtil
import com.mpa.evie.BuildConfig
import com.mpa.evie.BuildConfig.MAPS_API_KEY
import com.mpa.evie.BuildConfig.OCM_API_KEY
import com.mpa.evie.MapsActivity
import com.mpa.evie.R
import com.mpa.evie.api.google.GoogleDirectionsAPI
import com.mpa.evie.api.google.models.DirectionsResponse
import com.mpa.evie.api.google.models.Route
import com.mpa.evie.api.ocm.OpenChargeMapAPI
import com.mpa.evie.api.ocm.models.Charger
import com.mpa.evie.models.User
import com.mpa.evie.utils.haversineDistance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class CreateRouteActivity : AppCompatActivity() {

    private companion object {
        const val TAG = "CreateRouteActivity"
        const val START_REQUEST_CODE = 1
        const val DESTINATION_REQUEST_CODE = 2
        const val WAYPOINT_REQUEST_CODE = 3
        const val DIRECTIONS_BASE_URL = "https://maps.googleapis.com/"
    }

    private lateinit var retrofit: Retrofit
    private lateinit var ocmApi: OpenChargeMapAPI
    private lateinit var directionsApi: GoogleDirectionsAPI

    private lateinit var sliderSOC: Slider
    private lateinit var cbSpeedSlow: MaterialCheckBox
    private lateinit var cbSpeedFast: MaterialCheckBox
    private lateinit var btnContinue: Button
    private lateinit var btnAddStart: Button
    private lateinit var btnAddDestination: Button
    private lateinit var btnAddWaypoint: Button
    private lateinit var tvStart: TextView
    private lateinit var tvWaypoint: TextView
    private lateinit var tvLabelStart: TextView
    private lateinit var tvLabelDestination: TextView
    private lateinit var tvDestination: TextView
    private lateinit var tvLabelChargeSpeeds: TextView

    private lateinit var btnEditStart: ImageButton
    private lateinit var btnEditDestination: ImageButton
    private lateinit var btnEditWaypoint: ImageButton
    private lateinit var btnDeleteWaypoint: ImageButton

    private var start: Place? = null
    private var destination: Place? = null
    private var waypoint: Place? = null

    private var user: User? = null
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

    private fun openAutocompleteIntent(requestCode: Int) {
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(this)
        startActivityForResult(intent, requestCode)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_route)

        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY, Locale.UK)
        }

        retrofit = Retrofit.Builder()
            .baseUrl(MapsActivity.OCM_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        ocmApi = retrofit.create(OpenChargeMapAPI::class.java)

        retrofit = Retrofit.Builder()
            .baseUrl(DIRECTIONS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        directionsApi = retrofit.create(GoogleDirectionsAPI::class.java)

        sliderSOC = findViewById(R.id.sliderSOC)
        cbSpeedSlow = findViewById(R.id.cbSpeedSlow)
        cbSpeedFast = findViewById(R.id.cbSpeedFast)
        btnAddDestination = findViewById(R.id.btnAddDestination)
        btnAddWaypoint = findViewById(R.id.btnAddWaypoint)
        btnAddStart = findViewById(R.id.btnAddStart)
        btnContinue = findViewById(R.id.btnContinue)
        tvStart = findViewById(R.id.tvStart)
        tvWaypoint = findViewById(R.id.tvWaypoint)
        tvDestination = findViewById(R.id.tvDestination)
        tvLabelStart = findViewById(R.id.tvLabelStart)
        tvLabelDestination = findViewById(R.id.tvLabelDestination)
        tvLabelChargeSpeeds = findViewById(R.id.tvLabelChargeSpeeds)
        btnEditStart = findViewById(R.id.btnEditStart)
        btnEditDestination = findViewById(R.id.btnEditDestination)
        btnEditWaypoint = findViewById(R.id.btnEditWaypoint)
        btnDeleteWaypoint = findViewById(R.id.btnDeleteWaypoint)

        btnAddStart.setOnClickListener {
            openAutocompleteIntent(START_REQUEST_CODE)
        }
        btnEditStart.setOnClickListener {
            openAutocompleteIntent(START_REQUEST_CODE)
        }
        btnAddDestination.setOnClickListener {
            openAutocompleteIntent(DESTINATION_REQUEST_CODE)
        }
        btnEditDestination.setOnClickListener {
            openAutocompleteIntent(DESTINATION_REQUEST_CODE)
        }
        btnAddWaypoint.setOnClickListener {
            openAutocompleteIntent(WAYPOINT_REQUEST_CODE)
        }
        btnEditWaypoint.setOnClickListener {
            openAutocompleteIntent(WAYPOINT_REQUEST_CODE)
        }
        btnDeleteWaypoint.setOnClickListener {
            waypoint = null
            btnAddWaypoint.visibility = View.VISIBLE
            tvWaypoint.visibility = View.GONE
            btnEditWaypoint.visibility = View.GONE
            btnDeleteWaypoint.visibility = View.GONE
        }

        btnContinue.setOnClickListener {
            if (start != null && destination != null) {
                generateRoute(
                    start!!,
                    destination!!,
                    waypoint,
                    sliderSOC.value.toInt(),
                    cbSpeedSlow.isChecked,
                    cbSpeedFast.isChecked,
                )
            } else if (start == null && destination != null) {
                tvLabelStart.requestFocus()
                tvLabelStart.setError("Start point is required")
            } else if (destination == null && start != null) {
                tvLabelDestination.requestFocus()
                tvLabelDestination.setError("Destination is required")
            } else if (!cbSpeedFast.isChecked && !cbSpeedSlow.isChecked) {
                tvLabelChargeSpeeds.requestFocus()
                tvLabelChargeSpeeds.setError("Charge speed is required")
            }
        }

        val query = db.collection("users").document(auth.currentUser!!.uid)
        query.get().addOnSuccessListener { document ->
            if (document != null) {
                user = document.toObject<User>()
            } else {
                Log.d(TAG, "User not found (Firestore)")
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "Get user failed with ", exception)
        }
    }

    private fun generateRoute(
        start: Place,
        destination: Place,
        waypoint: Place?,
        soc: Int,
        speedSlow: Boolean,
        speedFast: Boolean
    ) {
//        STEPS:
//        1. get bounds from directions response
//        2. find available chargers within those bounds
//        3. get current range of car from SoC
//        4. if range covers whole distance, [return]
//        4. decode polyline to get list of latlng points
//        5. divide range by total distance and translate to equivalent index of decoded latlngs
//        6. pick charger closest to latlng value at this index
//        7. take away distance traveled from total distance, adjust latlng list and repeat from [3]

        val range: Int =
            soc * (user?.car?.range ?: 300) // should be var if > 1 charge stop is added
        var linePoints: List<LatLng>
        var chargerList: List<Charger?> = listOf()
        var chargeStop: com.mpa.evie.models.Place? = null
        var route: Route?
        var totalDistance: Double?

        directionsApi.getDirections(
            "" + (start.latLng?.latitude ?: "") + "," + (start.latLng?.longitude ?: ""),
            "" + (destination.latLng?.latitude ?: "") + "," + (destination.latLng?.longitude ?: ""),
            MAPS_API_KEY,
            when (waypoint) {
                null -> ""
                else -> "" + (waypoint.latLng?.latitude
                    ?: "") + ", " + (waypoint.latLng?.longitude ?: "")
            },
        )
            ?.enqueue(object : Callback<DirectionsResponse?> {
                override fun onResponse(
                    call: Call<DirectionsResponse?>,
                    response: Response<DirectionsResponse?>
                ) {
                    route = response.body()?.routes?.get(0)

                    linePoints = PolyUtil.decode(route!!.overviewPolyline?.points)
                    totalDistance =
                        route!!.legs.get(0).distance?.text?.replace(",", "")?.split(" ")?.get(0)
                            ?.toDouble()

                    if (range > (totalDistance ?: 0.0)
                    ) {
                        next(
                            com.mpa.evie.models.Route(
                                null,
                                com.mpa.evie.models.Place(
                                    start.name,
                                    start.latLng?.latitude,
                                    start.latLng?.longitude
                                ),
                                com.mpa.evie.models.Place(
                                    destination.name,
                                    destination.latLng?.latitude,
                                    destination.latLng?.longitude
                                ),
                                distance = totalDistance?.toInt(),
                                duration = route?.legs?.get(0)?.duration?.value,
                                chargeStops = listOf(chargeStop),
                                waypoints = when (waypoint) {
                                    null -> arrayListOf()
                                    else -> arrayListOf(
                                        com.mpa.evie.models.Place(
                                            waypoint.name,
                                            waypoint.latLng?.latitude,
                                            waypoint.latLng?.longitude
                                        )
                                    )
                                },
                                bounds = route?.bounds,
                                encodedPolyline = route?.overviewPolyline?.points
                            )
                        )
                        return
                    } else {
                        val bounds = route!!.bounds
                        val boundingBox = listOf(
                            Pair(bounds!!.southwest?.lat, bounds.northeast?.lng),
                            Pair(bounds.northeast?.lat, bounds.southwest?.lng)
                        ).toString().replace("[", "").replace("]", "").replace(" ", "")

                        val connectorIDs = user?.car?.connectors?.joinToString()?.replace(" ", "")
                        val operatorIDs =
                            user?.networks?.map { it.ocmID }?.joinToString()?.replace(" ", "")

                        ocmApi.getChargers(
                            OCM_API_KEY,
                            boundingBox,
                            connectiontypeid = connectorIDs ?: "",
                            operatorid = operatorIDs ?: ""
                        )
                            ?.enqueue(object : Callback<List<Charger?>?> {
                                override fun onResponse(
                                    call: Call<List<Charger?>?>,
                                    response: Response<List<Charger?>?>
                                ) {
                                    chargerList = response.body()!!
                                    if (speedSlow && !speedFast) {
                                        chargerList = chargerList.filter {
                                            !it!!.Connections.map { m -> m.Level!!.IsFastChargeCapable }
                                                .contains(true)
                                        }
                                    } else if (!speedSlow && speedFast) {
                                        chargerList = chargerList.filter {
                                            it!!.Connections.map { m -> m.Level!!.IsFastChargeCapable }
                                                .contains(true)
                                        }
                                    }
                                }

                                override fun onFailure(call: Call<List<Charger?>?>, t: Throwable) {
                                    Log.e(TAG, t.message ?: "")
                                    Toast.makeText(
                                        this@CreateRouteActivity,
                                        "Something went wrong",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })

                        var div = range / (totalDistance ?: 2.0)
                        div *= linePoints.size
                        var idx: Int = div.toInt() - 1
                        if (idx < 0) {
                            idx = 0
                        }
                        val point: LatLng = linePoints.get(idx)
                        chargeStop = nearestCharger(chargerList, point)

                        next(
                            com.mpa.evie.models.Route(
                                null,
                                com.mpa.evie.models.Place(
                                    start.name,
                                    start.latLng?.latitude,
                                    start.latLng?.longitude
                                ),
                                com.mpa.evie.models.Place(
                                    destination.name,
                                    destination.latLng?.latitude,
                                    destination.latLng?.longitude
                                ),
                                distance = totalDistance?.toInt(),
                                duration = route?.legs?.get(0)?.duration?.value,
                                chargeStops = listOf(chargeStop),
                                waypoints = when (waypoint) {
                                    null -> arrayListOf()
                                    else -> arrayListOf(
                                        com.mpa.evie.models.Place(
                                            waypoint.name,
                                            waypoint.latLng?.latitude,
                                            waypoint.latLng?.longitude
                                        )
                                    )
                                },
                                bounds = route?.bounds,
                                encodedPolyline = route?.overviewPolyline?.points
                            )
                        )
                        return
                    }
                }

                override fun onFailure(call: Call<DirectionsResponse?>, t: Throwable) {
                    Log.e(TAG, t.message ?: "")
                }
            })
    }

    private fun next(route: com.mpa.evie.models.Route) {
        startActivity(
            Intent(this, RouteMapActivity::class.java)
                .putExtra("route", route)
        )
    }

    private fun nearestCharger(
        chargerList: List<Charger?>,
        point: LatLng
    ): com.mpa.evie.models.Place? {
        var minDist = Double.MAX_VALUE
        var nearestCharger: com.mpa.evie.models.Place? = null
        chargerList.forEach {
            if (it?.AddressInfo?.Latitude != null && it.AddressInfo?.Longitude != null) {
                val d = haversineDistance(
                    LatLng(
                        it.AddressInfo?.Latitude!!,
                        it.AddressInfo?.Longitude!!
                    ), point
                )
                if (d < minDist) {
                    nearestCharger = com.mpa.evie.models.Place(
                        it.AddressInfo?.Title,
                        it.AddressInfo?.Latitude,
                        it.AddressInfo?.Longitude,
                        it.ID
                    )
                    minDist = d
                }
            }
        }
        return nearestCharger
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == START_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    start = data?.let { Autocomplete.getPlaceFromIntent(data) }
                    if (start != null) {
                        btnAddStart.visibility = View.INVISIBLE
                        tvStart.text = start!!.name
                        tvStart.visibility = View.VISIBLE
                        btnEditStart.visibility = View.VISIBLE
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Log.i(TAG, status.statusMessage ?: "")
                    }
                }
            }
            return
        } else if (requestCode == DESTINATION_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    destination = data?.let { Autocomplete.getPlaceFromIntent(it) }
                    if (destination != null) {
                        btnAddDestination.visibility = View.INVISIBLE
                        tvDestination.text = destination!!.name
                        tvDestination.visibility = View.VISIBLE
                        btnEditDestination.visibility = View.VISIBLE
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Log.i(TAG, status.statusMessage ?: "")
                    }
                }
            }
            return
        } else {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    waypoint = data?.let { Autocomplete.getPlaceFromIntent(it) }
                    if (waypoint != null) {
                        btnAddWaypoint.visibility = View.INVISIBLE
                        tvWaypoint.text = waypoint!!.name
                        tvWaypoint.visibility = View.VISIBLE
                        btnEditWaypoint.visibility = View.VISIBLE
                        btnDeleteWaypoint.visibility = View.VISIBLE
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Log.i(TAG, status.statusMessage ?: "")
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}