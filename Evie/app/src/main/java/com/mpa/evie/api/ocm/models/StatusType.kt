package com.mpa.evie.api.ocm.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class StatusType(

    @SerializedName("IsOperational") var IsOperational: Boolean? = null,
    @SerializedName("IsUserSelectable") var IsUserSelectable: Boolean? = null,
    @SerializedName("ID") var ID: Int? = null,
    @SerializedName("Title") var Title: String? = null
) : Serializable
