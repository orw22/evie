package com.mpa.evie.utils

import com.google.android.gms.maps.model.LatLng

private const val RADIUS = 3958.756 // earth radius in miles
private const val CD_DIVISOR = 46.469 // great circle distance divisor

fun haversineDistance(a: LatLng, b: LatLng): Double {
    return 2.0 * RADIUS * Math.asin(
        Math.sqrt(
            Math.pow(
                Math.sin((a.latitude - b.latitude) / 2),
                2.toDouble()
            ) + (Math.cos(a.latitude) * Math.cos(b.latitude) *
                    Math.pow(Math.sin((a.longitude - b.longitude) / 2), 2.toDouble()))
        )
    ) / CD_DIVISOR
}