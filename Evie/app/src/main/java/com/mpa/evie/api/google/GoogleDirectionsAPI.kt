package com.mpa.evie.api.google

import com.mpa.evie.api.google.models.DirectionsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleDirectionsAPI {

    @GET("maps/api/directions/json")
    fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") key: String,
        @Query("waypoints") waypoints: String = "", // in reverse order, latlng coordinates separated by |
        @Query("units") units: String = "imperial"
    ): Call<DirectionsResponse?>?
}