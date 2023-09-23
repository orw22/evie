package com.mpa.evie.api.ocm.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CurrentType(
    @SerializedName("Description") var Description: String? = null,
    @SerializedName("ID") var ID: Int? = null,
    @SerializedName("Title") var Title: String? = null
) : Serializable
