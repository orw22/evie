package com.mpa.evie.api.google.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Route(
    @SerializedName("bounds") var bounds: Bounds? = Bounds(),
    @SerializedName("legs") var legs: ArrayList<Leg> = arrayListOf(),
    @SerializedName("overview_polyline") var overviewPolyline: OverviewPolyline? = OverviewPolyline(),
) : Serializable
