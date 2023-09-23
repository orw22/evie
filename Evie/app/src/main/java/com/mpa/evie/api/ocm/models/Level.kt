package com.mpa.evie.api.ocm.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Level(
    @SerializedName("Comments") var Comments: String? = null,
    @SerializedName("IsFastChargeCapable") var IsFastChargeCapable: Boolean? = null,
    @SerializedName("ID") var ID: Int? = null,
    @SerializedName("Title") var Title: String? = null
) : Serializable
