package com.mpa.evie.api.google.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Bounds(
    @SerializedName("northeast" ) var northeast : LatLng? = LatLng(),
    @SerializedName("southwest" ) var southwest : LatLng? = LatLng()
) : Serializable
