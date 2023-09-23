package com.mpa.evie.api.google.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class LatLng(
    @SerializedName("lat") var lat: Double? = null,
    @SerializedName("lng") var lng: Double? = null
) : Serializable
