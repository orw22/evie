package com.mpa.evie.api.google.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Distance(
    @SerializedName("text") var text: String? = null,
    @SerializedName("value") var value: Int? = null
) : Serializable