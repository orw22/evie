package com.mpa.evie.api.google.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Leg(
    @SerializedName("distance") var distance: Distance? = null,
    @SerializedName("duration") var duration: Duration? = null,
    @SerializedName("start_address") var startAddress: String? = null,
    @SerializedName("end_address") var endAddress: String? = null
) : Serializable
