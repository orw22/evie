package com.mpa.evie.routes

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.PolyUtil
import com.mpa.evie.R
import com.mpa.evie.databinding.ActivityRouteMapBinding
import com.mpa.evie.utils.vectorToBitmap


class RouteMapActivity : AppCompatActivity(), OnMapReadyCallback {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private var route: com.mpa.evie.models.Route? = null

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityRouteMapBinding
    private lateinit var line: Polyline
    private lateinit var btnBack: FloatingActionButton

    private lateinit var btnSaveRoute: Button

    private companion object {
        const val TAG = "RouteMapActivity"
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getSupportActionBar()?.hide()
        binding = ActivityRouteMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val query = db.collection("users").document(auth.currentUser!!.uid)

        btnSaveRoute = findViewById(R.id.btnSaveRoute)
        btnBack = findViewById(R.id.btnBack)
        btnSaveRoute.setOnClickListener {
            val builder = AlertDialog.Builder(this).setTitle(R.string.route_map_save_route)
                .setMessage(R.string.route_map_route_name)

            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            builder.setPositiveButton(
                R.string.save
            ) { _, _ ->
                route?.name = input.text.toString()

                query.update("routes", FieldValue.arrayUnion(route)) // update user routes
                Toast.makeText(this@RouteMapActivity, "Route saved", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, RoutesActivity::class.java))
                finish()
            }
            builder.setNegativeButton(
                R.string.cancel
            ) { dialog, _ -> dialog.cancel() }

            builder.show()
        }

        btnBack.setOnClickListener {
            finish()
        }

        route = intent.extras!!.getSerializable("route") as com.mpa.evie.models.Route

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("NewApi")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLoadedCallback {
            line = mMap.addPolyline(
                PolylineOptions()
                    .addAll(
                        PolyUtil.decode(route?.encodedPolyline ?: "")
                    )
                    .width(12f)
                    .color(resources.getColor(R.color.dark_blue, theme))
            )

//            start marker
            mMap.addMarker(
                MarkerOptions().position(
                    LatLng(
                        route?.origin?.lat ?: 0.0,
                        route?.origin?.lng ?: 0.0
                    )
                ).title(route?.origin?.name!!)
                    .icon(
                        vectorToBitmap(
                            ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.ic_map_start,
                                null
                            ), resources.getColor(R.color.orange, null)
                        )
                    )
            )
//            finish marker
            mMap.addMarker(
                MarkerOptions().position(
                    LatLng(
                        route?.destination?.lat ?: 0.0,
                        route?.destination?.lng ?: 0.0
                    )
                ).title(route?.destination?.name!!)
                    .icon(
                        vectorToBitmap(
                            ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.ic_map_finish,
                                null
                            ), resources.getColor(R.color.orange, null)
                        )
                    )
            )

            if (!route?.waypoints.isNullOrEmpty()) {
//                waypoint marker
                mMap.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            route?.waypoints?.get(0)?.lat ?: 0.0,
                            route?.waypoints?.get(0)?.lng ?: 0.0
                        )
                    ).title(route?.destination?.name!!)
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
            }

            mMap.moveCamera(
                CameraUpdateFactory.newLatLngBounds(
                    LatLngBounds(
                        LatLng(
                            route?.bounds?.southwest?.lat ?: 0.0,
                            route?.bounds?.southwest?.lng ?: 0.0,
                        ),
                        LatLng(
                            route?.bounds?.northeast?.lat ?: 0.0,
                            route?.bounds?.northeast?.lng ?: 0.0,
                        )
                    ), 200
                )
            )
        }
    }

}
