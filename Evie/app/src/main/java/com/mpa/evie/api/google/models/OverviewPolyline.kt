package com.mpa.evie.api.google.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class OverviewPolyline(
    @SerializedName("points") var points: String? = null
) : Serializable