package com.mpa.evie.models

import com.mpa.evie.api.google.models.Bounds
import java.io.Serializable

data class Place(
    val name: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val chargePointId: Int? = null
) : Serializable

data class Route(
    var name: String? = null,
    val origin: Place? = null,
    val destination: Place? = null,
    val distance: Int? = null,
    val duration: Int? = null, // in minutes
    val chargeStops: List<Place?> = arrayListOf(),
    val waypoints: ArrayList<Place?> = arrayListOf(),
    val encodedPolyline: String? = null,
    val bounds: Bounds? = null,
) : Serializable
