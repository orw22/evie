package com.mpa.evie.api.google.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DirectionsResponse(
    @SerializedName("routes") var routes: ArrayList<Route> = arrayListOf(),
) : Serializable
